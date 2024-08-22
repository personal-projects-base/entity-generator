package com.potatotech.entitygenerator.service.java;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.Properties;
import com.potatotech.entitygenerator.service.GenerateSource;
import com.potatotech.entitygenerator.service.common.Common;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


import static com.potatotech.entitygenerator.service.common.Common.loadPath;
import static com.potatotech.entitygenerator.service.java.GenerateDTO.generateDTO;
import static com.potatotech.entitygenerator.service.java.GenerateDTOConverter.generateDTOConverter;
import static com.potatotech.entitygenerator.service.java.GenerateEndpoint.generateEndpoint;
import static com.potatotech.entitygenerator.service.java.GenerateEntity.generateEntity;
import static com.potatotech.entitygenerator.service.java.GenerateHandler.generateHandlerEntities;
import static com.potatotech.entitygenerator.service.java.GenerateRepositories.generateRepositoryes;
import static com.potatotech.entitygenerator.service.java.GenerateResources.generateResources;
import static com.potatotech.entitygenerator.service.java.GenerateUtils.generateHandler;
import static com.potatotech.entitygenerator.service.java.GenerateUtils.generateRestConfig;

public class GenerateJava {

    private static Path packagePath = null;

    public static void generateSource(Properties prop) {
        // Limpa os arquivos gerados anteriomente
        dropAndCreateDir(prop.getMainPackage());

        // gera a classe das entidaeds
        generateEntity(prop.getEntities(),prop.getMainPackage(),packagePath);
        // Gera as classes DTO
        generateDTO(prop.getEntities(),prop.getMainPackage(),packagePath);
        // Gera as classes DTO
        generateDTOConverter(prop.getEntities(),prop.getMainPackage(),packagePath);
        // Gera o HandlerBase
        generateHandler(prop.getMainPackage(),packagePath);
        // Gera Handlers de crud
        generateHandlerEntities(prop.getEntities(),prop.getMainPackage(),packagePath);
        // Gera o RestConfig
        generateRestConfig(prop.getMainPackage(),packagePath);
        // Gera os endpoints
        generateEndpoint(prop.getEndpoints(),prop.getMainPackage(),packagePath);

        // gera os repositories
        generateRepositoryes(prop.getEntities(),prop.getMainPackage(),packagePath);

        // faz uma copia da properties.json para a pasta static
        generateMetadata(prop);

        generateResources(prop.getEntities(),prop.getEndpoints());
    }

    private static void dropAndCreateDir(String packageName){

        String path = loadPath();
        String pack = packageName.replace(".","/");

        packagePath = Paths.get(String.format("%s/src/main/java/%s_gen",path,pack));
        GenerateSource.resourcePath = Paths.get(String.format("%s/src/main/resources",path));
        try {
            dropFiles(packagePath);
            Files.deleteIfExists(packagePath);
            Files.createDirectories(packagePath);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateMetadata(Properties prop) {
        try{
            String fileName = String.format("%s/properties.json",GenerateSource.resourcePath.toString());
            Common.dropFile(fileName);
            var path = Path.of(fileName);
            var entity = new Gson().toJson(prop);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
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
        }
    }
}
