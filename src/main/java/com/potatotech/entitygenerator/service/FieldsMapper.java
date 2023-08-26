package com.potatotech.entitygenerator.service;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.potatotech.entitygenerator.service.Common.firstCharacterUpperCase;

public class FieldsMapper {

    private static Map<String,String> typeFields = new LinkedHashMap<>();
    public static Log log = new SystemStreamLog();

    protected static String getFieldTypeEntity(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("Entity"));
    }

    protected static String getFieldTypeDto(String type){
        if(typeFields.isEmpty()){
            setFieldTypesMap();
        }
        return typeFields.getOrDefault(type,firstCharacterUpperCase(type).concat("Dto"));
    }

    private static void setFieldTypesMap(){
        typeFields.put("uuid","UUID");
        typeFields.put("string","String");
        typeFields.put("datetime","LocalDate");
        typeFields.put("date","Date");
        typeFields.put("int","int");
        typeFields.put("integer","int");
        typeFields.put("long","Long");
        typeFields.put("decimal","Double");
        typeFields.put("double","Double");
    }


}
