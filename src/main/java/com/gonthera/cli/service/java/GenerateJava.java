package com.gonthera.cli.service.java;

import com.google.gson.Gson;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.GenerateCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.java.GenerateDTO.generateDTO;
import static com.gonthera.cli.service.java.GenerateDTOConverter.generateDTOConverter;
import static com.gonthera.cli.service.java.GenerateEndpoint.generateEndpoint;
import static com.gonthera.cli.service.java.GenerateEntity.generateEntity;
import static com.gonthera.cli.service.java.GenerateEnum.generateEnum;
import static com.gonthera.cli.service.java.GenerateController.generateControllers;
import static com.gonthera.cli.service.java.GenerateAuthorization.generateAuthorization;
import static com.gonthera.cli.service.java.GenerateMessaging.generateMessaging;
import static com.gonthera.cli.service.common.GenerateResources.generateResources;
import static com.gonthera.cli.service.java.GenerateRepositories.generateRepositories;
import static com.gonthera.cli.service.java.GenerateService.generateServices;

public class GenerateJava {

    public static Logger logger = LogManager.getLogger(GenerateJava.class);

    private static Path packagePath = null;

    public static void generateSource(Properties prop) {
        // Limpa os arquivos gerados anteriomente
        dropAndCreateDir(prop.getMainPackage());

        // gera a classe das entidaeds
        generateEntity(prop.getEntities(),prop.getMainPackage(),packagePath.resolve("entities"));
        // Gera as classes DTO
        generateDTO(prop.getEntities(),prop.getMainPackage(),packagePath.resolve("dtos"));
        // Gera as classes DTO
        generateDTOConverter(prop.getEntities(),prop.getMainPackage(),packagePath.resolve("converters"));
        // Gera a camada de serviços
        generateServices(prop.getEntities(), prop.getMainPackage(), packagePath.resolve("services"));
        // Gera o contrato CRUD dos controllers
        GenerateCommon.generateFileCommon(prop.getMainPackage(),packagePath.resolve("common"), "crudcontroller", "CrudController");
        // Gera Controllers de CRUD
        generateControllers(prop.getEntities(),prop.getMainPackage(),packagePath.resolve("controllers"));
        // Gera o RestConfig
        GenerateCommon.generateFileCommon(prop.getMainPackage(),packagePath.resolve("common"), "restconfig", "RestConfig");
        // Gera os componentes de autorização sem dependência de biblioteca externa
        generateAuthorization(prop.getMainPackage(), packagePath, prop.getAuthorization());
        // Gera abstrações de mensageria RabbitMQ
        generateMessaging(prop.getMessaging() == null ? null : prop.getMessaging().getRabbitMq(), prop.getMainPackage(), packagePath);
        // Gera especificação dos filtros
        GenerateCommon.generateFileCommon(prop.getMainPackage(),packagePath.resolve("common"), "especificationfilter", "SpecificationFilter");
        // Gera os endpoints
        generateEndpoint(prop.getEndpoints(),prop.getMainPackage(),packagePath.resolve("endpoints"));
        // Gera as classes Enumeration
        generateEnum(prop.getEnums(),prop.getMainPackage(),packagePath.resolve("enums"));

        // gera os repositories
        generateRepositories(prop.getEntities(),prop.getMainPackage(),packagePath.resolve("repositories"));

        // Gera requestData e outputData
        GenerateCommon.generateFileCommon(prop.getMainPackage(),packagePath.resolve("common"), "requestdata", "RequestData");
        GenerateCommon.generateFileCommon(prop.getMainPackage(),packagePath.resolve("common"), "responsedata", "ResponseData");

        // faz uma copia da properties_dot.json para a pasta static
        generateMetadata(prop);

        generateResources(prop.getEntities(),prop.getEndpoints());
    }

    private static void dropAndCreateDir(String packageName){

        String path = loadPath();
        String pack = packageName.replace(".","/");

        packagePath = Paths.get(String.format("%s/src/main/java/%s_gen",path,pack));
        Common.resourcePath = Paths.get(String.format("%s/src/main/resources",path));
        try {
            dropFiles(packagePath);
            Files.deleteIfExists(packagePath);
            Files.createDirectories(packagePath);
            Files.createDirectories(packagePath.resolve("entities"));
            Files.createDirectories(packagePath.resolve("dtos"));
            Files.createDirectories(packagePath.resolve("converters"));
            Files.createDirectories(packagePath.resolve("repositories"));
            Files.createDirectories(packagePath.resolve("services"));
            Files.createDirectories(packagePath.resolve("controllers"));
            Files.createDirectories(packagePath.resolve("endpoints"));
            Files.createDirectories(packagePath.resolve("enums"));
            Files.createDirectories(packagePath.resolve("common"));
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateMetadata(Properties prop) {
        try{
            String fileName = String.format("%s/properties.json",Common.resourcePath.toString());
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

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc != null) throw exc;
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }catch (IOException ex){
        }
    }
}
