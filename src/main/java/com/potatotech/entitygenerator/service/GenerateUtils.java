package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.Common.*;
import static com.potatotech.entitygenerator.service.Common.getTableName;


public class GenerateUtils {

    protected static void generateHandler(String packageName, Path packagePath){
        generateRequestData( packageName, packagePath);
        generateResponseData(packageName, packagePath);
        generateEspecificationFilter(packageName, packagePath);
        String mod = loadWxsd("handlerbase");
        try{
            String fileName = stringFormater("HandlerBase","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    protected static void generateRestConfig(String packageName, Path packagePath){
        generateRequestData( packageName, packagePath);
        String mod = loadWxsd("restconfig");
        try{
            String fileName = stringFormater("RestConfig","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateRequestData(String packageName, Path packagePath){

        String mod = loadWxsd("requestdata");
        try{
            String fileName = stringFormater("RequestData","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateResponseData(String packageName, Path packagePath){

        String mod = loadWxsd("responsedata");
        try{
            String fileName = stringFormater("ResponseData","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateEspecificationFilter(String packageName, Path packagePath){

        String mod = loadWxsd("especificationfilter");
        try{
            String fileName = stringFormater("SpecificationFilter","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static String configureFileEntity(String mod, String packageName){
        return mod.replace("<<packageName>>",packageName.concat("_gen"));
    }

    protected static void generateSql(List<Entities> entities){

        try{

            String fileName = String.format("%s/postgree.sql",GenerateSource.resourcePath);
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
            var table = getTable(tableModel,item);
            var pks = getPk(pkModel,item);
            tempTable += table;
            tempPk += pks;
            tableFields.set(tempTable);
            pkFields.set(tempPk);
        });



        tableFields.set("--Entities\n\n".concat(tableFields.get()).concat("-- PKs\n\n").concat(pkFields.get()).concat("-- Fks\n\n").concat(getFk(entities)));

        return tableFields.get();
    }

    private static String getTable(String tableModel, Entities entities){

        AtomicReference<String> tableFields = new AtomicReference<>("");
        entities.getEntityFields().forEach(fields -> {
            var tempTableField = tableFields.get();
            String fieldTypeEntity = FieldsMapper.getFieldTypeDb(fields.getFieldProperties().getFieldType());
            var fieldTable = String.format("\n  %s %s,",splitByUppercase(fields.getFieldName()),fieldTypeEntity);
            tempTableField += fieldTable;
            tableFields.set(tempTableField);

        });

        var fields = tableFields.get().substring(0, tableFields.get().length() - 1);

        return tableModel.replace("<<fields>>",fields)
                .replace("<<tableName>>",getTableName(entities));
    }

    private static String getPk(String pkModel, Entities entities){

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
                .replace("<<tableName>>",getTableName(entities));
    }

    private static String getFk(List<Entities> entities){
        var fkModel = loadWxsd("sql_fk");

        final var entityReference = entities;

        AtomicReference<String> fkFields = new AtomicReference<>("");
        entities.forEach(item -> {
            item.getEntityFields().forEach(field -> {
                if(field.getRelationShips() != null){

                    var entity = entityReference.stream().filter(obj -> obj.getEntityName().equals(field.getFieldProperties().getFieldType())).findFirst();

                    var fieldReference = Optional.of(new EntityFields());
                    if(entity.isPresent()){
                        fieldReference = entity.get().getEntityFields().stream().filter(obj -> obj.getMetadata().isKey()).findFirst();
                    }

                    var tempFk = fkModel.replace("<<tableName>> ",getTableName(item))
                            .replace("<<field>>",splitByUppercase(field.getFieldName()))
                            .replace("<<tableReference>>", getTableName(getEntity(entities, field.getFieldProperties().getFieldType())))
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


}
