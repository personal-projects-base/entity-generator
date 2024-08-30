package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;


public class GenerateDTOConverter {


    private static List<String> dependencies = new ArrayList<>();

    protected static void generateDTOConverter(List<Entities> entities,String packageName, Path packagePath){

        String mod = loadWxsd("dtoconverter");
        entities.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEntityName(),"DTOConverter", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileDTO(mod,packageName,item,item.getEntityName());
                dependencies.clear();
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }


    private static String configureFileDTO(String mod, String packageName, Entities entity, String fileName){

        String fieldsEntity = getFieldsEntity(entity);
        String fieldsDTO = getFieldsDTO(entity);
        return mod.replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<projetcName>>",packageName)
                .replace("<<nameSpaceName>>",packageName.concat("_Gen"))
                .replace("<<simpleFieldsDTO>>",fieldsDTO)
                .replace("<<dependencies>>",getDependencies())
                .replace("<<simpleFieldsEntity>>",fieldsEntity);

    }


    private static String getFieldsEntity(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());
            String field = "";
            if(!fieldType.contains("Entity")){
                field = String.format("\n               entity.%s = dto.%s;",firstCharacterUpperCase(item.getFieldName()),item.getFieldName());
            }
            else{
                var entityforeignKey = properties.getEntities().stream().filter(e -> e.getEntityName().equals(item.getFieldName())).findFirst().get();
                var loadFieldKey = entityforeignKey.getEntityFields().stream().filter(e -> e.getMetadata().isKey()).findFirst().get();
                var fieldName = firstCharacterUpperCase(item.getFieldName()).concat("Id");
                var fieldFk = String.format("entity.%s = dto.%s != null ? dto.%s.%s : null;",firstCharacterUpperCase(fieldName),item.getFieldName(),item.getFieldName(),loadFieldKey.getFieldName());

                fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                field = String.format("\n               %s",fieldFk);
                field += String.format("\n               entity.%s = %sDtoConverter.ToEntity(dto.%s);",firstCharacterUpperCase(item.getFieldName()),fieldType,item.getFieldName());

            }

            tempField += field;
            fields.set(tempField);

        });
        return fields.get();
    }

    private static String getFieldsDTO(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());
            String field = "";
            if(!fieldType.contains("Entity")){
                field = String.format("\n               dto.%s = entity.%s;",item.getFieldName(),firstCharacterUpperCase(item.getFieldName()));
            }
            else{
                addDependencies(fieldType);
                fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                field = String.format("\n               dto.%s = %sDtoConverter.ToDTO(entity.%s);",item.getFieldName(),fieldType,firstCharacterUpperCase(item.getFieldName()));
            }

            tempField += field;
            fields.set(tempField);
        });
        return fields.get();
    }


    private static void addDependencies(String className){
        dependencies.add(className);
    }

    private static String getDependencies(){

        AtomicReference<String> fields = new AtomicReference<>("");

        dependencies.forEach(item -> {
            var tempField = fields.get();

            item = item.replace("Entity","").replace("DTO", "");
            var dependency = String.format("%s%s%sDTOConverter %sDtoConverter = new();","","\n        ",firstCharacterUpperCase(item),item.toLowerCase());

            tempField += dependency;
            fields.set(tempField);
        });

        return fields.get();
    }
}
