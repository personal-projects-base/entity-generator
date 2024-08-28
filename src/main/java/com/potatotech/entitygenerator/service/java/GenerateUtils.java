package com.potatotech.entitygenerator.service.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.GenerateCommon.*;


public class GenerateUtils {

    protected static void generateHandler(String packageName, Path packagePath){

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



}
