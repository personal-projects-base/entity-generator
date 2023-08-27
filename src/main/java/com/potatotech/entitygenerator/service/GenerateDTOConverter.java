package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.Common.*;


public class GenerateDTOConverter {


    private static List<String> dependencies = new ArrayList<>();

    protected static void generateDTOConverter(List<Entities> entities,String packageName, Path packagePath){

        String mod = loadWxsd("dtoconverter");
        entities.forEach(item -> {
            try{
                String fileName = stringFormater(item.getEntityName(),"DTOConverter", packagePath.toString());
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
                .replace("<<packageName>>",packageName.concat("_gen"))
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
                field = String.format("\n           entity.set%s(dto.%s);",firstCharacterUpperCase(item.getFieldName()),item.getFieldName());
            }
            else{
                fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                field = String.format("\n           entity.set%s(%sDtoConverter.toEntity(dto.%s));",firstCharacterUpperCase(item.getFieldName()),fieldType,item.getFieldName());
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
                field = String.format("\n           dto.%s = entity.get%s();",item.getFieldName(),firstCharacterUpperCase(item.getFieldName()));
            }
            else{
                addDependencies(fieldType);
                fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                field = String.format("\n           dto.%s = %sDtoConverter.toDTO(entity.get%s());",item.getFieldName(),fieldType,firstCharacterUpperCase(item.getFieldName()));
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
            var dependency = String.format("%s@Autowired%s%sDTOConverter %sDtoConverter;","\n    ","\n    ",firstCharacterUpperCase(item),item.toLowerCase());

            tempField += dependency;
            fields.set(tempField);
        });

        return fields.get();
    }
}
