package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.Common.*;


public class GenerateEntity {

    protected static void generateEntity(List<Entities> entities,String packageName, Path packagePath){

        String mod = loadWxsd("entity");
        entities.forEach(item -> {
            try{
                String fileName = stringFormater(item.getEntityName(),"Entity", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFile(mod,packageName,item,item.getEntityName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }

    private static String configureFile(String mod,String packageName, Entities entity,String fileName){

        String fields = getFields(entity);
        var model = mod.replace("<<tableName>>",entity.getTableName())
                .replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<importEntities>>",packageName.concat("_gen.*"))
                .replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<entityFields>>",fields);


        return model;
    }

    private static String getFields(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String anotations = "@Id";
            String field = String.format("private %s %s;\n    ",FieldsMapper.getFieldType(item.getFieldProperties().getFieldType()),item.getFieldName());
            tempField += anotations.concat("\n    ").concat(field);
            fields.set(tempField);
        });
        return fields.get();
    }




}
