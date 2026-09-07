package com.gonthera.cli.service.node;

import com.google.gson.Gson;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;

import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.common.GenerateResources.generateResources;
import static com.gonthera.cli.service.common.GenerateSQL.generateSql;
import static com.gonthera.cli.service.node.GenerateEndpoint.generateCrudControllers;
import static com.gonthera.cli.service.node.GenerateEndpoint.generateEndpoints;
import static com.gonthera.cli.service.node.GenerateEnum.generateEnums;
import static com.gonthera.cli.service.node.GenerateMessaging.generateMessaging;
import static com.gonthera.cli.service.node.GenerateModel.generateModels;
import static com.gonthera.cli.service.node.GenerateOpenApi.generate;
import static com.gonthera.cli.service.node.GeneratePrisma.generatePrismaSchema;
import static com.gonthera.cli.service.node.GenerateRepository.generateRepositories;

public class GenerateNode {

    private static Path packagePath = null;

    public static void generateSource(Properties prop) {
        GeneratePrisma.validateCollectionRelations(prop.getEntities());
        dropAndCreateDir();
        GenerateDatabaseConfiguration.generate(packagePath);
        GenerateCrudSupport.generate(prop, packagePath);
        generateModels(prop.getEntities(), packagePath);
        generateEnums(prop.getEnums(), packagePath);
        generateRepositories(prop.getEntities(), packagePath);
        generateCrudControllers(prop.getEntities(), packagePath);
        generateEndpoints(prop.getEndpoints(), packagePath);
        generate(prop, packagePath);
        generateMessaging(prop.getMessaging() == null ? null : prop.getMessaging().getRabbitMq(), packagePath);
        generatePrismaSchema(prop.getEntities(), prop.getEnums());
        generateSql(prop.getEntities());
        generateMetadata(prop);
        generateResources(prop.getEntities(), prop.getEndpoints());
    }

    private static void dropAndCreateDir() {
        String path = loadPath();
        packagePath = Paths.get(String.format("%s/src/generated", path));
        Common.resourcePath = Paths.get(String.format("%s/src/generated/static", path));
        try {
            dropFiles(packagePath);
            Files.deleteIfExists(packagePath);
            Files.createDirectories(packagePath);
            Files.createDirectories(Common.resourcePath);
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to prepare Node output directory", ex);
        }
    }

    private static void generateMetadata(Properties prop) {
        try {
            String fileName = String.format("%s/properties.json", Common.resourcePath.toString());
            Common.dropFile(fileName);
            var path = Path.of(fileName);
            var entity = new Gson().toJson(prop);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void dropFiles(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path directory, IOException ex) throws IOException {
                    if (ex != null) throw ex;
                    Files.deleteIfExists(directory);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to clean Node output directory", ex);
        }
    }
}
