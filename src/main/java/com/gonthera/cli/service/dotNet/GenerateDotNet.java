package com.gonthera.cli.service.dotNet;

import com.google.gson.Gson;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.GenerateCommon;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.common.GenerateResources.generateResources;
import static com.gonthera.cli.service.dotNet.GenerateContext.generateDbContext;
import static com.gonthera.cli.service.dotNet.GenerateContext.generateScoped;
import static com.gonthera.cli.service.dotNet.GenerateDTO.generateDTO;
import static com.gonthera.cli.service.dotNet.GenerateEntity.generateEntity;
import static com.gonthera.cli.service.dotNet.GenerateController.generateControllers;
import static com.gonthera.cli.service.dotNet.GenerateMessaging.generateMessaging;
import static com.gonthera.cli.service.dotNet.GenerateRepository.generateIRepositories;
import static com.gonthera.cli.service.dotNet.GenerateRepository.generateRepositories;
import static com.gonthera.cli.service.java.GenerateEnum.generateEnum;


public class GenerateDotNet {

    private static Path packagePath = null;

    public static void generateSource(Properties prop){
        // Limpa os arquivos gerados anteriomente
        dropAndCreateDir(prop.getMainPackage());

        // gera a classe das entidaeds
        generateEntity(prop.getEntities(),prop.getMainPackage(),directory("Entities"));

        // gera as classes de DTO
        generateDTO(prop.getEntities(),prop.getMainPackage(),directory("Dtos"));

        // Gera requestData e outputData
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Common"), "requestdata", "RequestData");
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Common"), "responsedata", "ResponseData");

        // Gera IBaseRepository
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Repositories"), "ibaserepository", "IBaseRepository");

        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Common"), "dynamicfilter", "DynamicFilter");

        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Common"), "criteriaextension", "ExpressionExtensions");

        // Gera DynamicSchemaModelCacheKeyFactory
        GenerateCommon.generateFileCommon(prop.getMainPackage(),directory("Data"), "dynamicreplaceschemafactory", "DynamicSchemaModelCacheKeyFactory");

        // Gera as classes Enumeration
        generateEnum(prop.getEnums(),prop.getMainPackage(),directory("Enums"));

        //Generate dbContext
        generateDbContext(prop.getEntities(),prop.getMainPackage(),directory("Data"));

        //Gera o scoped
        generateScoped(prop.getEntities(),prop.getMainPackage(),directory("Common"));

        // gera os repositories
        generateIRepositories(prop.getEntities(),prop.getMainPackage(),directory("Repositories"));
        generateRepositories(prop.getEntities(),prop.getMainPackage(),directory("Repositories"));

        // Gera os controllers CRUD
        generateControllers(prop.getEntities(),prop.getMainPackage(),directory("Controllers"));

        //Gera os DTOConverter
        GenerateDTOConverter.generateDTOConverter(prop.getEntities(),prop.getMainPackage(),directory("Converters"));

        //Gera abstrações de mensageria RabbitMQ
        generateMessaging(prop.getMessaging() == null ? null : prop.getMessaging().getRabbitMq(), prop.getMainPackage(), packagePath);

        //Gera as primitivas
        GenerateEndpoint.generateEndpoint(prop.getEndpoints(),prop.getMainPackage(),directory("Endpoints"));

        // faz uma copia da properties_dot.json para a pasta static
        generateMetadata(prop);

        generateResources(prop.getEntities(),prop.getEndpoints());
    }

    private static Path directory(String name) {
        Path directory = packagePath.resolve(name);
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create .NET generated directory " + directory, ex);
        }
        return directory;
    }

    private static void dropAndCreateDir(String packageName){
        String path = loadPath();
        String pack = packageName.replace(".","/");

        packagePath = Paths.get(String.format("%s/%s_gen",path,pack));
        Common.resourcePath = Paths.get(String.format("%s/static",path));
        try {
            dropFiles(packagePath);
            Files.deleteIfExists(packagePath);
            Files.createDirectories(packagePath);
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static void generateMetadata(Properties prop){

        try{
            String fileName = String.format("%s/properties.json", Common.resourcePath.toString());
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
