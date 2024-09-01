package com.potatotech.entitygenerator.service.common;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;
import com.potatotech.entitygenerator.service.GenerateSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.Common.generateRandomString;

public class GenerateSQL {

    public static void generateSql(List<Entities> entities){

        try{
            String fileName = String.format("%s/postgree.sql", Common.resourcePath);
            Common.dropFile(fileName);
            var path = Path.of(fileName);
            var sql = generateSqlData(entities);
            Files.write(path, sql.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static String generateSqlData(List<Entities> entities) {

        var tableModel = loadWxsd("sql_table");
        var pkModel = loadWxsd("sql_pk");

        AtomicReference<String> tableFields = new AtomicReference<>("");
        AtomicReference<String> pkFields = new AtomicReference<>("");
        entities.forEach(item -> {
            var tempTable = tableFields.get();
            var tempPk = pkFields.get();
            var table = generateTable(tableModel,item);
            var pks = generatePk(pkModel,item);
            tempTable += table;
            tempPk += pks;
            tableFields.set(tempTable);
            pkFields.set(tempPk);
        });

        var fileSql = "--Entities\n\n"
                .concat(tableFields.get())
                .concat("-- PKs\n\n").concat(pkFields.get())
                .concat("-- Fks\n\n").concat(generateFk(entities))
                .concat("--RelationShips\n\n").concat(generateTablesRelationShipsManyToMany(entities));

        tableFields.set(fileSql);

        return tableFields.get();
    }

    private static String generateTable(String tableModel, Entities entities){

        AtomicReference<String> tableFields = new AtomicReference<>("");
        entities.getEntityFields().forEach(fields -> {
            if(fields.getRelationShips() == null || !fields.getRelationShips().getRelationShip().equalsIgnoreCase("ManyToMany")){
                var tempTableField = tableFields.get();
                String fieldTypeEntity = FieldsMapper.getFieldTypeDb(fields.getFieldProperties().getFieldType(),fields);
                if(fields.getRelationShips() == null || !fields.getRelationShips().isBidirectional()){
                    var fieldTable = String.format("\n  %s %s,",splitByUppercase(fields.getFieldName()),fieldTypeEntity);
                    tempTableField += fieldTable;
                    tableFields.set(tempTableField);
                }
            }
        });

        var fields = tableFields.get().substring(0, tableFields.get().length() - 1);

        return tableModel.replace("<<fields>>",fields)
                .replace("<<tableName>>",splitByUppercase(getTableName(entities)));
    }

    private static String generatePk(String pkModel, Entities entities){

        AtomicReference<String> pkFields = new AtomicReference<>("");
        entities.getEntityFields().forEach(fields -> {
            var tempTableField = pkFields.get();
            if(fields.getMetadata() != null && fields.getMetadata().isKey()){
                var fieldTable = String.format("%s",splitByUppercase(fields.getFieldName()));
                tempTableField += fieldTable;
                pkFields.set(tempTableField);
            }
        });

        return pkModel.replace("<<fieldKey>>",pkFields.get())
                .replace("<<idPk>>","ok_".concat(generateRandomString()))
                .replace("<<tableName>>",splitByUppercase(getTableName(entities)));
    }

    private static String generateFk(List<Entities> entities){
        var fkModel = loadWxsd("sql_fk");

        final var entityReference = entities;

        AtomicReference<String> fkFields = new AtomicReference<>("");
        entities.forEach(item -> {
            item.getEntityFields().forEach(field -> {
                if(field.getRelationShips() != null && !field.getRelationShips().isBidirectional()){

                    var entity = entityReference.stream().filter(obj -> obj.getEntityName().equals(field.getFieldProperties().getFieldType())).findFirst();

                    var fieldReference = Optional.of(new EntityFields());
                    if(entity.isPresent()){
                        fieldReference = entity.get().getEntityFields().stream().filter(obj -> obj.getMetadata().isKey()).findFirst();
                    }

                    var tempFk = fkModel.replace("<<tableName>> ",splitByUppercase(getTableName(item)))
                            .replace("<<field>>",splitByUppercase(field.getFieldName()))
                            .replace("<<tableReference>>", splitByUppercase(getTableName(getEntity(entities, field.getFieldProperties().getFieldType()))))
                            .replace("<<fieldReference>>",splitByUppercase(fieldReference.get().getFieldName()))
                            .replace("<<idFk>> ","fk_".concat(generateRandomString()));
                    fkFields.set(fkFields.get().concat(tempFk));
                }
            });
        });

        return fkFields.get();
    }

    private static Entities getEntity(List<Entities> entities, String entity){
        return entities.stream().filter(item -> item.getEntityName().equals(entity)).findFirst().orElse(null);
    }

    private static String generateTablesRelationShipsManyToMany(List<Entities> entities) {

        AtomicReference<String> tablesRelationShips = new AtomicReference<>("");
        var model = loadWxsd("sql_table_relation_ships");
        entities.forEach(entity -> {
            var fieldRelationShps = entity.getEntityFields().stream().filter(field -> field.getRelationShips() != null && field.getRelationShips().getRelationShip().equalsIgnoreCase("ManyToMany"));
            fieldRelationShps.forEach(fieldRelationShip -> {
                var tempTableField = tablesRelationShips.get();
                var modelTable = generateTableRelation(fieldRelationShip, entity, model,entities);
                tempTableField += modelTable.concat("\n");
                tablesRelationShips.set(tempTableField);
            });
        });

        return tablesRelationShips.get();
    }

    private static String generateTableRelation(EntityFields field, Entities entity, String model, List<Entities> entities){

        var joinTable = "";

        var entityRelation = getEntity(entities,field.getFieldProperties().getFieldType());

        var name = splitByUppercase(entity.getEntityName()).concat("_")
                .concat(splitByUppercase(field.getFieldProperties()
                        .getFieldType()));

        var joinColumn = splitByUppercase(entity.getEntityName()).concat("_id");
        var inverseJoinColumn = splitByUppercase(field.getFieldProperties().getFieldType()).concat("_id");

        joinTable = model.replaceAll("<<tableName>>",name)
                .replaceAll("<<joinColumns>>",joinColumn)
                .replaceAll("<<inverseJoinColumns>>",inverseJoinColumn)
                .replaceAll("<<fieldInverseColumnId>>",getPkEntity(entityRelation))
                .replaceAll("<<fieldColumnId>>",getPkEntity(entity))
                .replaceAll("<<entityJoinColumns>>",getTableName(entityRelation))
                .replaceAll("<<entityInverseJoinColumns>>", getTableName(entity));

        return joinTable.trim();
    }
}
