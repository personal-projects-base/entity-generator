package com.potatotech.entitygenerator.service;


import com.potatotech.entitygenerator.enuns.Language;
import com.potatotech.entitygenerator.service.common.Common;
import com.potatotech.entitygenerator.service.java.GenerateJava;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;


import java.nio.file.*;


import static com.potatotech.entitygenerator.service.common.Common.*;


@Mojo(name="generate-sources", defaultPhase = LifecyclePhase.NONE)
public class GenerateSource extends AbstractMojo {






    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Iniciando geração de fontes");
        generateSource();
    }

    public void generateSource() throws MojoExecutionException {
        getLog().info("Carregando metadata");
        Common.properties = loadProperties();

        if(Common.properties.getLanguage() == null){
            throw new MojoExecutionException("Language not defined");
        }
        if(Common.properties.getLanguage() == Language.JAVA){
            GenerateJava.generateSource(Common.properties);
        }
        if(Common.properties.getLanguage() == Language.DOTNET){
            System.out.println("Aqui será gerado os codigos C#" + loadPath());
        }

    }

}
