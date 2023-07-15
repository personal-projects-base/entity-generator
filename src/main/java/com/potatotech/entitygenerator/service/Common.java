package com.potatotech.entitygenerator.service;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.EntityFields;
import com.potatotech.entitygenerator.model.Properties;

import java.io.*;
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

        var path = System.getProperty("user.dir");
        File arquivo = new File(String.format("%s/properties.json", path));
        try {
            InputStream inputStream = new FileInputStream(arquivo);
            return new Gson().fromJson(convertInputStreamToString(inputStream), Properties.class);
        }catch (IOException ex){
            FieldsMapper.log.info(ex);
        }
        return null;
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

    protected static String setComments(String comments) {
        return String.format("\n    /**%s**/",comments);
    }
}
