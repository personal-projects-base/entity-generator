package com.gonthera.cli.service;


import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.ConfigurationFileValidator;
import com.gonthera.cli.service.common.ProjectValidator;
import com.gonthera.cli.service.dotNet.GenerateDotNet;
import com.gonthera.cli.service.node.GenerateNode;
import com.gonthera.cli.service.java.GenerateJava;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.nio.file.Paths;


import static com.gonthera.cli.service.common.Common.*;


@Mojo(name="generate-sources", defaultPhase = LifecyclePhase.NONE)
public class GenerateSource extends AbstractMojo {






    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Iniciando geração de fontes");
        generateSource();
    }

    public void generateSource() throws MojoExecutionException {
        getLog().info("Carregando metadata");
        ConfigurationFileValidator.validateForGeneration(Paths.get(System.getProperty("user.dir")));
        Common.properties = loadProperties();
        ProjectValidator.validate(Common.properties);
        ProjectValidator.warnings(Common.properties).forEach(getLog()::warn);

        if(Common.properties.getLanguage() == null){
            throw new MojoExecutionException("Language not defined");
        }
        if(Common.properties.getLanguage() == Language.JAVA){
            GenerateJava.generateSource(Common.properties);
        }
        if(Common.properties.getLanguage() == Language.DOTNET){
            GenerateDotNet.generateSource(Common.properties);
        }
        if(Common.properties.getLanguage() == Language.NODE){
            GenerateNode.generateSource(Common.properties);
        }

    }

}
