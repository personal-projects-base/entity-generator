package com.potatotech.entitygenerator.service.java;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;


public class GenerateDTOConverter {


    private static Map<String, String> dependencies = new LinkedHashMap<>();

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

                var entityforeignKey = properties.getEntities().stream().filter(e -> e.getEntityName().equals(item.getFieldProperties().getFieldType())).findFirst().orElse(null);
                var loadFieldRelationShip = entityforeignKey != null ? entityforeignKey.getEntityFields().stream().filter(e -> e.getFieldProperties().getFieldType().equals(entity.getEntityName())).findFirst().orElse(null) : null;

                if(loadFieldRelationShip == null || loadFieldRelationShip.getRelationShips() != null && !loadFieldRelationShip.getRelationShips().isBidirectional()){
                    var dtoName = item.getFieldName();
                    field = String.format("\n           entity.set%s(%sDtoConverter.toEntity(dto.%s, null));",firstCharacterUpperCase(item.getFieldName()),dtoName,item.getFieldName());
                }

                if(item.getRelationShips() != null && item.getRelationShips().isBidirectional()){
                    var fieldName = entity.getEntityName();
                    if(item.getRelationShips().getMappedBy() != null && !item.getRelationShips().getMappedBy().isEmpty()){
                        fieldName = item.getRelationShips().getMappedBy();

                    }
                    if(item.isList())
                        field += String.format("\n           if (entity.get%s() != null) entity.get%s().forEach(e -> e.set%s(entity));", firstCharacterUpperCase(item.getFieldName()), firstCharacterUpperCase(item.getFieldName()), firstCharacterUpperCase(fieldName));
                    else
                        field += String.format("\n           if (entity.get%s() != null) entity.get%s().set%s(entity);", firstCharacterUpperCase(item.getFieldName()), firstCharacterUpperCase(item.getFieldName()), firstCharacterUpperCase(fieldName));
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
                var op = fieldType.equals("boolean") ? "is" : "get";
                field = String.format("\n           dto.%s = displayFields.contains(\"*\") || displayFields.contains(\"%s\") ? entity.%s%s() : null;"
                        ,item.getFieldName(),
                        item.getFieldName(),
                        op,
                        firstCharacterUpperCase(item.getFieldName())
                );
            }
            else{


                var entityforeignKey = properties.getEntities().stream().filter(e -> e
                            .getEntityName()
                            .equals(item.getFieldProperties().getFieldType()))
                        .findFirst().orElse(null);

                var loadFieldRelationShip = entityforeignKey != null ? entityforeignKey
                        .getEntityFields().stream().filter(e -> e
                                .getFieldProperties().getFieldType()
                                .equals(entity.getEntityName()))
                        .findFirst().orElse(null)
                        : null;


                if(entity.getEntityName().equals(item.getFieldProperties().getFieldType())){
                    addDependencies(entity.getEntityName(), item.getFieldName());

                } else {
                    addDependencies(item.getFieldName(), item.getFieldName());
                }


                if(loadFieldRelationShip == null || loadFieldRelationShip.getRelationShips() != null && !loadFieldRelationShip.getRelationShips().isBidirectional() && !item.getRelationShips().isReference()){
                    var fieldTypeName = item.getFieldName();
                    field = String.format("\n           dto.%s = displayFields.contains(\"*\") || displayFields.contains(\"%s\") ? %sDtoConverter.toDTO(entity.get%s(), SpecificationFilter.displayFieldsEntity(displayFields,\"%s\")) : null;",
                            item.getFieldName(),
                            item.getFieldName(),
                            fieldTypeName,
                            firstCharacterUpperCase(item.getFieldName()),
                            item.getFieldName()
                    );
                }
            }

            tempField += field;
            fields.set(tempField);
        });
        return fields.get();
    }


    private static void addDependencies(String className, String propertyName){
        dependencies.put(propertyName, className);
    }

    private static String getDependencies(){

        AtomicReference<String> fields = new AtomicReference<>("");

        dependencies.forEach((k, v) -> {
            var tempField = fields.get();

            var item =  v.replace("Entity","").replace("DTO", "");
            var dependency = String.format("%s@Autowired%s@Lazy%s%sDTOConverter %sDtoConverter;","\n    ","\n    ","\n    ",firstCharacterUpperCase(item), firstCharacterLowerCase(k));

            tempField += dependency;
            fields.set(tempField);
        });

        return fields.get();
    }
}
