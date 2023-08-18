package com.potatotech.entitygenerator.service;


import com.google.gson.Gson;
import com.potatotech.entitygenerator.model.Properties;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static com.potatotech.entitygenerator.service.Common.*;
import static com.potatotech.entitygenerator.service.GenerateDTO.generateDTO;
import static com.potatotech.entitygenerator.service.GenerateDTOConverter.generateDTOConverter;
import static com.potatotech.entitygenerator.service.GenerateEndpoint.generateEndpoint;
import static com.potatotech.entitygenerator.service.GenerateEntity.generateEntity;
import static com.potatotech.entitygenerator.service.GenerateHandler.generateHandlerEntities;
import static com.potatotech.entitygenerator.service.GenerateUtils.generateHandler;
import static com.potatotech.entitygenerator.service.GenerateRepositories.generateRepositoryes;
import static com.potatotech.entitygenerator.service.GenerateUtils.generateRestConfig;

@Mojo(name="generate-sources")
public class GenerateSource extends AbstractMojo {


    private static Path packagePath = null;
    private static Path resourcePath = null;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Iniciando geração de fontes");
        generateSource();
    }

    public void generateSource(){
        getLog().info("Carregando metadata");
        var prop = loadProperties();

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
        }
    }

}
