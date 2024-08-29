package com.potatotech.entitygenerator.service.common;



import com.potatotech.entitygenerator.enuns.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.potatotech.entitygenerator.service.common.Common.firstCharacterUpperCase;
import static com.potatotech.entitygenerator.service.common.Common.properties;

public class FieldsMapper {

    private static Map<String,String> typeFields = new LinkedHashMap<>();
    private static Map<String,String> typeFieldsDatabase = new LinkedHashMap<>();
    public static Logger logger = LogManager.getLogger(FieldsMapper.class);

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
        if(properties.getLanguage() == Language.JAVA){
            typeFields.put("uuid","UUID");
            typeFields.put("string","String");
            typeFields.put("password","String");
            typeFields.put("datetime","LocalDateTime");
            typeFields.put("date","LocalDate");
        }
        else if(properties.getLanguage() == Language.DOTNET){

            typeFields.put("uuid","Guid");
            typeFields.put("string","string");
            typeFields.put("password","string");
            typeFields.put("datetime","DateTime");
            typeFields.put("date","DateTime");
        }

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
