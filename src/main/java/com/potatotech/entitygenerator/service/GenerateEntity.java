package com.potatotech.entitygenerator.service;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;

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
                var entity = configureFileEntity(mod,packageName,item,item.getEntityName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
        GenerateUtils.generateSql(entities);
    }

    private static String configureFileEntity(String mod, String packageName, Entities entity, String fileName){

        String fields = getFields(entity);
        return mod.replace("<<tableName>>",getTableName(entity))
                .replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<entityFields>>",fields);
    }




    private static String getFields(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String comments = Common.setComments(item.getComment());
            String anotations = setMetadata(item).concat(setRelationsShip(item));
            String fieldType = FieldsMapper.getFieldTypeEntity(item.getFieldProperties().getFieldType());
            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("private %s %s;\n    ",fieldType,item.getFieldName());
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

    private static String setMetadata(EntityFields entity) {
        var metadata = "";
        if(entity.getMetadata() != null){
            if(entity.getMetadata().isKey()){
                metadata += "\n    @Id";
                metadata += "\n    @GeneratedValue(strategy = GenerationType.UUID)";
            }
            if(!entity.getMetadata().isNullable()){
                metadata += "\n    @Column(nullable = false, name = \""+splitByUppercase(entity.getFieldName())+"\")";
            } else {
                metadata += "\n    @Column(name = \""+splitByUppercase(entity.getFieldName())+"\")";
            }
        }else {
            metadata += "\n    @Column(name = \""+splitByUppercase(entity.getFieldName())+"\")";
        }
        if(entity.getRelationShips() != null){
            metadata = metadata.replace("@Column","@JoinColumn");
        }
        return metadata;
    }
}
