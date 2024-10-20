package com.potatotech.entitygenerator.service.java;

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
                var entityforeignKey = properties.getEntities().stream().filter(e -> e.getEntityName().equals(item.getFieldName())).findFirst().get();
                var loadFieldRelationShip = entityforeignKey.getEntityFields().stream().filter(e -> e.getFieldProperties().getFieldType().equals(entity.getEntityName())).findFirst().orElse(null);

                if(loadFieldRelationShip != null && loadFieldRelationShip.getRelationShips() != null && !loadFieldRelationShip.getRelationShips().isBidirectional()){
                    fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                    field = String.format("\n           entity.set%s(%sDtoConverter.toEntity(dto.%s));",firstCharacterUpperCase(item.getFieldName()),item.getFieldName(),item.getFieldName());
                }
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
                var op = fieldType == "boolean" ? "is" : "get";
                field = String.format("\n           dto.%s = entity.%s%s();",item.getFieldName(),op,firstCharacterUpperCase(item.getFieldName()));
            }
            else{
                addDependencies(fieldType);

                var entityforeignKey = properties.getEntities().stream().filter(e -> e.getEntityName().equals(item.getFieldName())).findFirst().get();
                var loadFieldRelationShip = entityforeignKey.getEntityFields().stream().filter(e -> e.getFieldProperties().getFieldType().equals(entity.getEntityName())).findFirst().orElse(null);

                if(loadFieldRelationShip != null && loadFieldRelationShip.getRelationShips() != null && !loadFieldRelationShip.getRelationShips().isBidirectional()){
                    fieldType = fieldType.replace("Entity","").replace("DTO", "").toLowerCase();
                    field = String.format("\n           dto.%s = %sDtoConverter.toDTO(entity.get%s());",item.getFieldName(),item.getFieldName(),firstCharacterUpperCase(item.getFieldName()));
                }

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
            var dependency = String.format("%s@Autowired%s@Lazy%s%sDTOConverter %sDtoConverter;","\n    ","\n    ","\n    ",firstCharacterUpperCase(item), firstCharacterLowerCase(item));

            tempField += dependency;
            fields.set(tempField);
        });

        return fields.get();
    }
}
