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
import static com.potatotech.entitygenerator.service.common.Common.firstCharacterUpperCase;
import static com.potatotech.entitygenerator.service.common.GenerateSQL.generateSql;

public class GenerateEntity {

    protected static void generateEntity(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("entity");
        entities.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEntityName(),"Entity", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileEntity(mod,packageName,item,item.getEntityName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
        generateSql(entities);
    }

    private static String configureFileEntity(String mod, String packageName, Entities entity, String fileName){

        String fields = getFields(entity);
        return mod.replace("<<tableName>>",getTableName(entity))
                .replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<projetcName>>",packageName)
                .replace("<<nameSpaceName>>",packageName.concat("_Gen"))
                .replace("<<entityFields>>",fields);
    }

    private static String getFields(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String comments = Common.setComments(item.getComment());
            String anotations = setMetadata(item, entity).concat(setRelationsShip(item));
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());
            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("private %s %s { get; set; }\n    ",fieldType,item.getFieldName());
            tempField += comments.concat(anotations).concat("\n    ").concat(field);
            fields.set(tempField);
        });
        return fields.get();
    }

    private static String setRelationsShip(EntityFields entity) {

        var metadata = "";
        if(entity.getRelationShips() != null){
            metadata += String.format("\n    @%s(fetch = FetchType.%s)",entity.getRelationShips().getRelationShip(),entity.getRelationShips().getFetchType());
        }

        return metadata;
    }

    private static String setMetadata(EntityFields field, Entities entity) {
        var metadata = "";
        if(field.getMetadata() != null && field.getRelationShips() == null){
            if(field.getMetadata().isKey()){
                metadata += "\n    [Key]";
                if(field.getFieldProperties().getFieldType().equals("uuid"))
                    metadata += "\n    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]";
            }
            if(!field.getMetadata().isNullable()){
                metadata += "\n    [Required]";
                metadata += "\n    [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
            } else {
                metadata += "\n    [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
            }
        }else {
            metadata += "\n    [Column(name:\""+splitByUppercase(field.getFieldName())+"\")]";
        }

//        if(field.getRelationShips() != null){
//            if(field.getRelationShips().getRelationShip().equalsIgnoreCase("ManyToMany")){
//                var joinTable = setJointTable(field, entity);
//                metadata += "\n    ".concat(joinTable);
//            }else {
//                metadata += "\n    @JoinColumn(name = \""+splitByUppercase(field.getFieldName())+"\")";
//            }
//
//        }
        return metadata;
    }


    private static String setJointTable(EntityFields field, Entities entity){

        var joinTable = "";

        var name = splitByUppercase(entity.getEntityName()).concat("_")
                .concat(splitByUppercase(field.getFieldProperties()
                        .getFieldType()));

        var joinColumn = splitByUppercase(entity.getEntityName()).concat("_id");
        var inverseJoinColumn = splitByUppercase(field.getFieldProperties().getFieldType()).concat("_id");

        var model = loadWxsd("jointable");

        joinTable = model.replace("<<unionTableName>>",name)
                .replace("<<joinColumn>>",joinColumn)
                .replace("<<inverseJoinColumn>>", inverseJoinColumn);

        return joinTable.trim();
    }

}
