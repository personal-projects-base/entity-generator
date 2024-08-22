package com.potatotech.entitygenerator.service;


import com.potatotech.entitygenerator.enuns.Language;
import com.potatotech.entitygenerator.service.java.GenerateJava;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;


import java.nio.file.*;


import static com.potatotech.entitygenerator.service.common.Common.*;


@Mojo(name="generate-sources")
public class GenerateSource extends AbstractMojo {



    public static Path resourcePath = null;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Iniciando geração de fontes");
        generateSource();
    }

    public void generateSource() throws MojoExecutionException {
        getLog().info("Carregando metadata");
        var prop = loadProperties();

        if(prop.getLanguage() == null){
            throw new MojoExecutionException("Language not defined");
        }
        if(prop.getLanguage() == Language.JAVA){
            GenerateJava.generateSource(prop);
        }

    }

}
