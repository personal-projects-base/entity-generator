package com.gonthera.cli.service.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.Enums;
import com.gonthera.cli.model.Messaging;
import com.gonthera.cli.model.Properties;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Common {

    public static Path resourcePath = null;
    public static Properties properties = null;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 20;
    private static final String PROJECT_FILE_NAME = "project.json";
    private static final String LEGACY_PROJECT_FILE_NAME = "properties.json";
    private static final String GONTHERA_DIRECTORY_NAME = ".gonthera";

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
        File gontheraDirectory = new File(path, GONTHERA_DIRECTORY_NAME);

        if (gontheraDirectory.isDirectory()) {
            return loadSeparatedProject(gontheraDirectory);
        }

        File projectFile = new File(path, PROJECT_FILE_NAME);
        File legacyProjectFile = new File(path, LEGACY_PROJECT_FILE_NAME);
        File inputFile = projectFile.isFile() ? projectFile : legacyProjectFile;

        if (!inputFile.isFile()) {
            throw new RuntimeException(String.format(
                    "Project configuration not found. Expected %s or %s in %s",
                    PROJECT_FILE_NAME,
                    LEGACY_PROJECT_FILE_NAME,
                    path
            ));
        }

        return readJson(inputFile, Properties.class);
    }

    private static Properties loadSeparatedProject(File gontheraDirectory) {
        File projectFile = new File(gontheraDirectory, PROJECT_FILE_NAME);
        if (!projectFile.isFile()) {
            throw new RuntimeException(String.format(
                    "Project configuration not found. Expected %s in %s",
                    PROJECT_FILE_NAME,
                    gontheraDirectory.getAbsolutePath()
            ));
        }

        Properties project = readJson(projectFile, Properties.class);
        Type entitiesType = new TypeToken<List<Entities>>() { }.getType();
        Type endpointsType = new TypeToken<List<Endpoints>>() { }.getType();
        Type enumsType = new TypeToken<List<Enums>>() { }.getType();

        project.setEntities(readOptionalList(new File(gontheraDirectory, "entities.json"), entitiesType, project.getEntities()));
        project.setEndpoints(readOptionalList(new File(gontheraDirectory, "endpoints.json"), endpointsType, project.getEndpoints()));
        project.setEnums(readOptionalList(new File(gontheraDirectory, "enums.json"), enumsType, project.getEnums()));

        File messagingFile = new File(gontheraDirectory, "messaging.json");
        if (messagingFile.isFile()) {
            project.setMessaging(readJson(messagingFile, Messaging.class));
        }
        return project;
    }

    private static <T> List<T> readOptionalList(File file, Type type, List<T> projectValues) {
        if (file.isFile()) {
            return readJson(file, type);
        }
        return projectValues == null ? Collections.emptyList() : projectValues;
    }

    private static <T> T readJson(File file, Type type) {
        try {
            InputStream inputStream = new FileInputStream(file);
            return new Gson().fromJson(convertInputStreamToString(inputStream), type);
        } catch (IOException ex) {
            throw new RuntimeException(String.format("Unable to read %s", file.getAbsolutePath()), ex);
        }
    }

    public static String loadWxsd(String fileName){

        ClassLoader classLoader = Common.class.getClassLoader();
        InputStream inputStream = null;
        if(properties.getLanguage() == Language.JAVA)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/java/%s.mxsd", fileName));
        if(properties.getLanguage() == Language.DOTNET)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/dotnet/%s.mxsd", fileName));
        if(properties.getLanguage() == Language.NODE)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/node/%s.mxsd", fileName));

        if(inputStream == null)
            inputStream = classLoader.getResourceAsStream(String.format("xsd/sql/%s.mxsd", fileName));


        return convertInputStreamToString(inputStream);
    }

    public static String loadPath(){
        return Paths.get(System.getProperty("user.dir")).toAbsolutePath().toString();
    }

    public static String generatedJavaImport(String packageName, String subpackage, boolean enabled) {
        return enabled ? String.format("import %s_gen.%s.*;", packageName, subpackage) : "";
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

    public static String firstCharacterLowerCase(String fileName){
        return fileName.substring(0,1).toLowerCase() + fileName.substring(1);
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
        if(input.equals(""))
            return "";
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
