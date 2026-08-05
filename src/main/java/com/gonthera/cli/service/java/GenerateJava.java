package com.gonthera.cli.service.java;

import com.google.gson.Gson;
import com.gonthera.cli.enuns.Architecture;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.GenerateCommon;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;


import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.common.GenerateSQL.generateSql;
import static com.gonthera.cli.service.java.common.GenerateDTO.generateDTO;
import static com.gonthera.cli.service.java.mvc.GenerateDTOConverter.generateDTOConverter;
import static com.gonthera.cli.service.java.common.GenerateEndpoint.generateEndpoint;
import static com.gonthera.cli.service.java.mvc.GenerateEntity.generateEntity;
import static com.gonthera.cli.service.java.common.GenerateEnum.generateEnum;
import static com.gonthera.cli.service.java.mvc.GenerateController.generateControllers;
import static com.gonthera.cli.service.java.common.GenerateAuthorization.generateAuthorization;
import static com.gonthera.cli.service.java.common.GenerateAuthorization.generateInfrastructureAuthorization;
import static com.gonthera.cli.service.java.common.GenerateMessaging.generateMessaging;
import static com.gonthera.cli.service.common.GenerateResources.generateResources;
import static com.gonthera.cli.service.java.mvc.GenerateRepositories.generateRepositories;
import static com.gonthera.cli.service.java.mvc.GenerateService.generateServices;
import static com.gonthera.cli.service.java.hexagonal.GenerateDomain.generateDomains;
import static com.gonthera.cli.service.java.hexagonal.GenerateApplicationCore.generate;
import static com.gonthera.cli.service.java.hexagonal.GeneratePersistenceModel.generatePersistenceModels;
import static com.gonthera.cli.service.java.hexagonal.GeneratePersistenceAdapter.generatePersistenceAdapters;

public class GenerateJava {

    public static Logger logger = LogManager.getLogger(GenerateJava.class);

    private static Path packagePath = null;

    public static void generateSource(Properties prop) {
        // Limpa os arquivos gerados anteriomente
        dropAndCreateDir(prop.getMainPackage());

        if (prop.getArchitecture() == Architecture.HEXAGONAL) {
            generateHexagonal(prop);
        } else {
            generateMvc(prop);
        }

        // faz uma copia da configuração para a pasta de recursos
        generateMetadata(prop);
        generateResources(prop.getEntities(),prop.getEndpoints());
    }

    private static void generateHexagonal(Properties prop) {
        generateDomains(prop.getEntities(), prop.getMainPackage(), directory("domain/model"));
        generate(
                prop.getEntities(),
                prop.getMainPackage(),
                directory("domain/ports/in"),
                directory("domain/ports/out"),
                directory("application/services")
        );
        generatePersistenceModels(
                prop.getEntities(),
                prop.getMainPackage(),
                directory("infrastructure/adapters/out/persistence/entities"),
                directory("infrastructure/adapters/out/persistence/mappers")
        );
        generatePersistenceAdapters(
                prop.getEntities(),
                prop.getMainPackage(),
                directory("infrastructure/adapters/out/persistence/repositories"),
                directory("infrastructure/adapters/out/persistence"),
                directory("infrastructure/configuration")
        );
        generateInfrastructureAuthorization(prop.getMainPackage(), packagePath, prop.getAuthorization());
        generateEnum(prop.getEnums(), prop.getMainPackage(), directory("enums"));
        generateSql(prop.getEntities());
    }

    private static void generateMvc(Properties prop) {
        // gera a classe das entidaeds
        generateEntity(prop.getEntities(),prop.getMainPackage(),directory("entities"));
        // Gera as classes DTO
        generateDTO(prop.getEntities(),prop.getMainPackage(),directory("dtos"));
        // Gera as classes DTO
        generateDTOConverter(prop.getEntities(),prop.getMainPackage(),directory("converters"));
        // Gera a camada de serviços
        generateServices(prop.getEntities(), prop.getMainPackage(), directory("services"));
        // Gera o contrato CRUD dos controllers
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("common"), "crudcontroller", "CrudController");
        // Gera Controllers de CRUD
        generateControllers(prop.getEntities(),prop.getMainPackage(),directory("controllers"));
        // Gera o RestConfig
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("common"), "restconfig", "RestConfig");
        // Gera os componentes de autorização sem dependência de biblioteca externa
        generateAuthorization(prop.getMainPackage(), packagePath, prop.getAuthorization());
        // Gera abstrações de mensageria RabbitMQ
        generateMessaging(prop.getMessaging() == null ? null : prop.getMessaging().getRabbitMq(), prop.getMainPackage(), packagePath);
        // Gera especificação dos filtros
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("common"), "especificationfilter", "SpecificationFilter");
        // Gera os endpoints
        generateEndpoint(prop.getEndpoints(),prop.getMainPackage(),directory("endpoints"));
        // Gera as classes Enumeration
        generateEnum(prop.getEnums(),prop.getMainPackage(),directory("enums"));

        // gera os repositories
        generateRepositories(prop.getEntities(),prop.getMainPackage(),directory("repositories"));

        // Gera requestData e outputData
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("common"), "requestdata", "RequestData");
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("common"), "responsedata", "ResponseData");
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
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static Path directory(String name) {
        Path directory = packagePath.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create Java generated directory " + directory, ex);
        }
        return directory;
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
