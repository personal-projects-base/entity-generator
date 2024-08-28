package com.potatotech.entitygenerator.service.common;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.enuns.Language;
import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.Properties;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Common {

    public static Path resourcePath = null;
    public static Properties properties = null;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 20;

    public static String convertInputStreamToString(InputStream inputStream){
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

    public static Properties loadProperties(){

        var path = System.getProperty("user.dir");
        File arquivo = new File(String.format("%s/properties.json", path));
        try {
            InputStream inputStream = new FileInputStream(arquivo);
            return new Gson().fromJson(convertInputStreamToString(inputStream), Properties.class);
        }catch (IOException ex){
            FieldsMapper.logger.info(ex);
        }
        return null;
    }

    public static String loadWxsd(String fileName){

        ClassLoader classLoader = Common.class.getClassLoader();
        InputStream inputStream = null;
        if(properties.getLanguage() == Language.JAVA)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/java/%s.mxsd", fileName));
        if(properties.getLanguage() == Language.DOTNET)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/dotnet/%s.mxsd", fileName));

        if(inputStream == null)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/sql/%s.mxsd", fileName));


        return convertInputStreamToString(inputStream);
    }

    public static String loadPath(){
        Path currentPath = Paths.get("");
        return currentPath.toAbsolutePath().toString();
    }

    public static String stringFormaterJava(String entityName, String entity, String packagePath) {
        entityName = firstCharacterUpperCase(entityName);
        var output = "";
        if(properties.getLanguage() == Language.JAVA)
            output = String.format("%s/%s%s.java",packagePath,entityName,entity);
        if(properties.getLanguage() == Language.DOTNET)
            output = String.format("%s/%s%s.cs",packagePath,entityName,entity);

        return output;
    }

    public static String firstCharacterUpperCase(String fileName){
        return fileName.substring(0,1).toUpperCase() + fileName.substring(1);
    }

    public static String setComments(String comments) {
        var output = "";
        if(properties.getLanguage() == Language.JAVA)
            output = String.format("\n    /**%s**/",comments);
        if(properties.getLanguage() == Language.DOTNET)
            output = String.format("\n        /**%s**/",comments);

        return output;
    }


    public static String splitByUppercase(String input) {
        List<String> words = new ArrayList<>();

        StringBuilder currentWord = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (Character.isUpperCase(currentChar) && currentWord.length() > 0) {
                words.add(currentWord.toString());
                currentWord.setLength(0);
            }

            currentWord.append(currentChar);
        }

        if (currentWord.length() > 0) {
            words.add(currentWord.toString());
        }

        AtomicReference<String> name = new AtomicReference<>("");

        words.forEach(w -> {
            var tempName = name.get();
            tempName += String.format("%s_",w.toLowerCase());
            name.set(tempName);
        });
        return name.get().substring(0, name.get().length() - 1);
    }

    public static String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder randomString = new StringBuilder();

        for (int i = 0; i < LENGTH; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            char randomChar = CHARACTERS.charAt(randomIndex);
            randomString.append(randomChar);
        }

        return randomString.toString();
    }

    public static String getTableName(Entities entity){
        if(entity.getTableName() == null){
            return splitByUppercase(entity.getEntityName());
        }
        else {
            return entity.getTableName();
        }
    }

    public static String getPkEntity(Entities entity){
        AtomicReference<String> pkField = new AtomicReference<>("");
        entity.getEntityFields().forEach(fields -> {
            var tempTableField = pkField.get();
            if(fields.getMetadata() != null && fields.getMetadata().isKey()){
                var fieldTable = String.format("%s",splitByUppercase(fields.getFieldName()));
                tempTableField += fieldTable;
                pkField.set(tempTableField);
            }
        });
        return pkField.get();
    }

    public static void dropFile(String file){
        try{
            File localFile = new File(file);
            if(localFile.exists()){
                if (localFile.delete()){
                    System.out.println("delete file " +file);
                }
            }
        } catch (Exception ex){
            System.err.println("failure delete file " +file);
        }
    }
}
