package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.Common.*;


public class GenerateDTOConverter {


    protected static void generateDTOConverter(List<Entities> entities,String packageName, Path packagePath){

        String mod = loadWxsd("dtoconverter");
        entities.forEach(item -> {
            try{
                String fileName = stringFormater(item.getEntityName(),"DTOConverter", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileDTO(mod,packageName,item,item.getEntityName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }


    private static String configureFileDTO(String mod, String packageName, Entities entity, String fileName){

        String fields = getDTO(entity);
        String fieldsEntity = getFieldsEntity(entity);
        String fieldsDTO = getFieldsDTO(entity);
        return mod.replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<entityFields>>",fields)
                .replace("<<entityFieldsDTO>>",fieldsDTO)
                .replace("<<entityFieldsEntity>>",fieldsEntity);
    }


    private static String getDTO(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldType(item.getFieldProperties().getFieldType());
            //fieldType = fieldType.replace("Entity","DTO");
            if(!fieldType.contains("Entity")){
                if(item.isList()){
                    fieldType = String.format("List<%s>",fieldType);
                }
                String field = String.format("%s %s, ",fieldType,item.getFieldName());
                tempField += field;
                fields.set(tempField);
            }

        });
        return fields.get().substring(0, fields.get().length() -2);
    }

    private static String getFieldsEntity(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldType(item.getFieldProperties().getFieldType());
            if(!fieldType.contains("Entity")){
                String field = String.format("entity.get%s(), ",firstCharacterUpperCase(item.getFieldName()));
                tempField += field;
                fields.set(tempField);
            }

        });
        return fields.get().substring(0, fields.get().length() -2);
    }

    private static String getFieldsDTO(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldType(item.getFieldProperties().getFieldType());
            if(!fieldType.contains("Entity")){
                String field = String.format("dto.%s(), ",item.getFieldName());
                tempField += field;
            } else {
                tempField += "null, ";
            }

            fields.set(tempField);
        });
        return fields.get().substring(0, fields.get().length() -2);
    }
}
