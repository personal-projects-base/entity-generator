package com.gonthera.cli.service.common;



import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.EntityFields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.gonthera.cli.service.common.Common.*;

public class FieldsMapper {

    private static Map<String,String> typeFields = new LinkedHashMap<>();
    private static Map<String,String> typeFieldsDatabase = new LinkedHashMap<>();
    public static Logger logger = LogManager.getLogger(FieldsMapper.class);

    public static String getFieldTypeEntity(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        if(properties.getEnums() != null && properties.getEnums().stream().anyMatch(e -> e.enumName.equalsIgnoreCase(type))){
            return typeFields.getOrDefault(type,firstCharacterUpperCase(type));
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("Entity"));
    }

    public static String getFieldTypeDto(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        if(properties.getEnums() != null && properties.getEnums().stream().anyMatch(e -> e.enumName.equalsIgnoreCase(type))){
            return typeFields.getOrDefault(type,firstCharacterUpperCase(type));
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("DTO"));
    }

    private static void setFieldTypesMap(){
        if(properties.getLanguage() == Language.JAVA){
            typeFields.put("uuid","UUID");
            typeFields.put("string","String");
            typeFields.put("password","String");
            typeFields.put("datetime","LocalDateTime");
            typeFields.put("date","LocalDate");
            typeFields.put("int","Integer");
            typeFields.put("boolean","boolean");
            typeFields.put("byte","byte");
            typeFields.put("byte[]","byte[]");
            typeFields.put("inputStream","InputStream");
            typeFields.put("map","Map<String, Object>");
        }
        else if(properties.getLanguage() == Language.DOTNET){

            typeFields.put("uuid","Guid?");
            typeFields.put("string","string");
            typeFields.put("password","string");
            typeFields.put("datetime","DateTime");
            typeFields.put("date","DateTime");
            typeFields.put("int","int?");
            typeFields.put("boolean","bool");
        }


        typeFields.put("integer","int");
        typeFields.put("long","Long");
        typeFields.put("decimal","Double");
        typeFields.put("double","Double");
    }

    /**
     * Converte o tipo, para o tipo do banco de dados
     * @param type
     * @param fields
     * @return
     */
    public static String getFieldTypeDb(String type, EntityFields fields){
        var output = "";
        if(typeFieldsDatabase.isEmpty()){
            setFieldTypesDbMap();
        }

        var typePk = properties.getEntities().stream().filter(entity -> entity.getEntityName().equals(type)).findFirst().orElse(null);
        if(typePk != null){
            var field = typePk.getEntityFields().stream().filter(item -> item.getMetadata().isKey()).findFirst().orElse(null);
            if(field != null){
                output = typeFieldsDatabase.getOrDefault(type,typeFieldsDatabase.getOrDefault(field.getFieldProperties().getFieldType(),""));
            }

        }else {
            if(properties.getEnums() != null && properties.getEnums().stream().anyMatch(e -> e.enumName.equalsIgnoreCase(type))){
                output = typeFieldsDatabase.getOrDefault(type,"int");
            } else {
                output = typeFieldsDatabase.getOrDefault(type,"uuid");
            }

        }

        // configuração especial para postgres, será alterado quando implementado geração pra sql server
        if(output.equals("integer") && (fields.getMetadata() != null && fields.getMetadata().isKey())){
            output = "serial";
        }

        return output;
    }

    private static void setFieldTypesDbMap(){

        typeFieldsDatabase.put("uuid","uuid");
        typeFieldsDatabase.put("boolean","boolean");
        typeFieldsDatabase.put("password","varchar");
        typeFieldsDatabase.put("string","varchar");
        typeFieldsDatabase.put("datetime","timestamp");
        typeFieldsDatabase.put("date","date");
        typeFieldsDatabase.put("int","integer");
        typeFieldsDatabase.put("integer","integer");
        typeFieldsDatabase.put("long","integer");
        typeFieldsDatabase.put("decimal","numeric");
        typeFieldsDatabase.put("double","numeric");
    }


}
