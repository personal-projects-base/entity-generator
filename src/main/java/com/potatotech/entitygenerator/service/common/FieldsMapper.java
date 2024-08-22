package com.potatotech.entitygenerator.service.common;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.potatotech.entitygenerator.service.common.Common.firstCharacterUpperCase;

public class FieldsMapper {

    private static Map<String,String> typeFields = new LinkedHashMap<>();
    private static Map<String,String> typeFieldsDatabase = new LinkedHashMap<>();
    public static Log log = new SystemStreamLog();

    public static String getFieldTypeEntity(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("Entity"));
    }

    public static String getFieldTypeDto(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("DTO"));
    }

    private static void setFieldTypesMap(){
        typeFields.put("uuid","UUID");
        typeFields.put("password","String");
        typeFields.put("string","String");
        typeFields.put("datetime","LocalDateTime");
        typeFields.put("date","LocalDate");
        typeFields.put("int","int");
        typeFields.put("integer","int");
        typeFields.put("long","Long");
        typeFields.put("decimal","Double");
        typeFields.put("double","Double");
    }


    public static String getFieldTypeDb(String type){
        if(typeFieldsDatabase.isEmpty()){
            setFieldTypesDbMap();
        }
        return typeFieldsDatabase.getOrDefault(type,"uuid");
    }

    private static void setFieldTypesDbMap(){

        typeFieldsDatabase.put("uuid","uuid");
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
