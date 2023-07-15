package com.potatotech.entitygenerator;

import com.potatotech.entitygenerator.service.GenerateSource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="generate-sources")
public class Main extends AbstractMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Iniciando geração de fontes");
        GenerateSource.generateSource();
    }
}
