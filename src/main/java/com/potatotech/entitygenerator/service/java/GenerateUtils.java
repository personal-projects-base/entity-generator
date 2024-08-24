package com.potatotech.entitygenerator.service.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.potatotech.entitygenerator.service.common.Common.*;


public class GenerateUtils {

    protected static void generateHandler(String packageName, Path packagePath){
        generateRequestData( packageName, packagePath);
        generateResponseData(packageName, packagePath);
        generateEspecificationFilter(packageName, packagePath);
        String mod = loadWxsd("handlerbase");
        try{
            String fileName = stringFormaterJava("HandlerBase","", packagePath.toString());
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
            String fileName = stringFormaterJava("RestConfig","", packagePath.toString());
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
            String fileName = stringFormaterJava("RequestData","", packagePath.toString());
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
            String fileName = stringFormaterJava("ResponseData","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateEspecificationFilter(String packageName, Path packagePath){

        String mod = loadWxsd("especificationfilter");
        try{
            String fileName = stringFormaterJava("SpecificationFilter","", packagePath.toString());
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
