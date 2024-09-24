package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;
import com.potatotech.entitygenerator.service.common.Common;
import com.potatotech.entitygenerator.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.GenerateCommon.configureFileEntity;
import static com.potatotech.entitygenerator.service.common.GenerateSQL.generateSql;

public class GenerateEntity {

    protected static void generateEntity(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("entity");
        entities.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEntityName(),"Entity", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileEntity(mod,packageName,item,item.getEntityName(),getFields(item));
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                System.out.println("@GenerateData");
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
        generateSql(entities);
    }



    private static String getFields(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String comments = Common.setComments(item.getComment());
            String anotations = setMetadata(item, entity);
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());
            var fieldIdentity = "";[[[[[[[[[ok]]]]]]]]]
            var containVirtual = "";
            if(fieldType.contains("Entity")){
                containVirtual = "virtual";
                if(!item.getRelationShips().isBidirectional()){
                    fieldIdentity = loadRelationship(item, entity);
                }píphjk
            }
            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("    public %s %s %s { get; set; }\n    ",containVirtual,fieldType,firstCharacterUpperCase(item.getFieldName()));
            tempField += comments.concat(anotations).concat(fieldIdentity).concat("\n    ").concat(field);
            fields.set(tempField);
        });
        return fields.get();
    }

    private static String loadRelationship(EntityFields field, Entities entity) {

        var entityforeignKey = properties.getEntities().stream().filter(e -> e.getEntityName().equals(field.getFieldName())).findFirst().get();

        var loadFieldKey = entityforeignKey.getEntityFields().stream().filter(e -> e.getMetadata().isKey()).findFirst().get();
        var fieldType = FieldsMapper.getFieldTypeEntity(loadFieldKey.getFieldProperties().getFieldType());
        var fieldName = firstCharacterUpperCase(field.getFieldName().concat("Id"));

        return String.format("    \n        public %s %s { get; set; }\n    ", fieldType, firstCharacterUpperCase(fieldName));
    }


    private static String setMetadata(EntityFields field, Entities entity) {
        var metadata = "";
        if(field.getMetadata() != null && field.getRelationShips() == null){
            if(field.getMetadata().isKey()) {
                metadata += "\n        [Key]";
                //if(field.getFieldProperties().getFieldType().equals("uuid"))
                    metadata += "\n        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]";
            }
            if(!field.getMetadata().isNullable()){
                metadata += "\n        [Required]";
                metadata += "\n        [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
            } else {
                metadata += "\n        [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
            }
        }else {
            if(field.getRelationShips() == null || !field.getRelationShips().isBidirectional()){
                metadata += "\n        [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
            }

        }

        if(field.getRelationShips() != null && !field.getRelationShips().isBidirectional()) {
            metadata += "\n        [ForeignKey(name:\""+firstCharacterUpperCase(field.getFieldName())+"\")]";
        }
        return metadata;
    }

}
