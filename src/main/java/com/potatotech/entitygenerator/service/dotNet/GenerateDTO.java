package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;

public class GenerateDTO {


    protected static void generateDTO(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("dto");
        entities.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEntityName(),"DTO", packagePath.toString());
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
        String fieldsNotTypes = getDTONotTypes(entity);
        return mod.replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<projetcName>>",packageName)
                .replace("<<nameSpaceName>>",packageName.concat("_Gen"))
                .replace("<<entityFields>>",fields)
                .replace("<<entityFieldsNoType>>",fieldsNotTypes);
    }


    private static String getDTO(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());

            fieldType = fieldType.replace("Entity","DTO");
            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("\n        public %s %s { get; set; } ",fieldType,item.getFieldName());
            tempField += field;
            fields.set(tempField);

        });
        return fields.get();
    }

    private static String getDTONotTypes(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());

            String field = String.format("%s %s, ",fieldType,item.getFieldName());
            tempField += field;
            fields.set(tempField);

        });
        return fields.get().substring(0, fields.get().length() -2);
    }

}
