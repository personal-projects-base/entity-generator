package com.potatotech.entitygenerator.service;


import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.Properties;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import static com.potatotech.entitygenerator.service.Common.*;
import static com.potatotech.entitygenerator.service.GenerateEntity.generateEntity;
import static com.potatotech.entitygenerator.service.GenerateRepositories.generateRepositoryes;

public class GenerateSource {


    private static Path packagePath = null;
    private static Path resourcePath = null;

    private static final Logger logger = Logger.getLogger(GenerateSource.class.getName());

    public static void generateSource(){

        FieldsMapper.log.info("Carregando metadata");
        var prop = loadProperties();

        dropAndCreateDir(prop.getMainPackage());

        // gera a classe das entidaeds
        generateEntity(prop.getEntities(),prop.getMainPackage(),packagePath);

        // gera os repositories
        generateRepositoryes(prop.getEntities(),prop.getMainPackage(),packagePath);

        // faz uma copia da properties.json para a pasta static
        generateMetadata(prop);

    }

    private static void generateMetadata(Properties prop) {
        try{
            String fileName = String.format("%s/properties.json",resourcePath.toString());
            var path = Path.of(fileName);
            var entity = new Gson().toJson(prop);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void dropAndCreateDir(String packageName){

        String path = loadPath();
        String pack = packageName.replace(".","/");

        packagePath = Paths.get(String.format("%s/src/main/java/%s_gen",path,pack));
        resourcePath = Paths.get(String.format("%s/src/main/resources",path));
        try {
            dropFiles(packagePath);
            Files.deleteIfExists(packagePath);
            Files.createDirectories(packagePath);
        } catch (IOException ex){
           ex.printStackTrace();
        }
    }

    private static void dropFiles(Path path){

        try{
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    System.out.println("delete file " +file.toAbsolutePath());
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

}
