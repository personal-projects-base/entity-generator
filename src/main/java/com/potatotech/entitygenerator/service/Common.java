package com.potatotech.entitygenerator.service;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.Properties;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Common {

    protected static String convertInputStreamToString(InputStream inputStream){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            reader.close();
            return stringBuilder.toString();
        } catch (Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    protected static Properties loadProperties(){

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream("static/properties.json");

        return new Gson().fromJson(convertInputStreamToString(inputStream), Properties.class);
    }

    protected static String loadWxsd(String fileName){

        ClassLoader classLoader = Common.class.getClassLoader();

        InputStream inputStream = classLoader.getResourceAsStream(String.format("xsd/%s.mxsd", fileName));

        return convertInputStreamToString(inputStream);
    }

    protected static String loadPath(){
        Path currentPath = Paths.get("");
        return currentPath.toAbsolutePath().toString();
    }

    protected static String stringFormater(String entityName, String entity, String packagePath) {
        entityName = firstCharacterUpperCase(entityName);
        return String.format("%s/%s%s.java",packagePath,entityName,entity);
    }

    protected static String firstCharacterUpperCase(String fileName){
        return fileName.substring(0,1).toUpperCase() + fileName.substring(1);
    }
}
