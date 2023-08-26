package com.potatotech.entitygenerator.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.potatotech.entitygenerator.service.Common.*;


public class GenerateUtils {

    protected static void generateHandler(String packageName, Path packagePath){
        generateRequestData( packageName, packagePath);
        generateResponseData(packageName, packagePath);
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

    private static String configureFileEntity(String mod, String packageName){
        return mod.replace("<<packageName>>",packageName.concat("_gen"));
    }
}
