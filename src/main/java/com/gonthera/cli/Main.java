package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;

import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.dotNet.GenerateDotNet;
import com.gonthera.cli.service.java.GenerateJava;
import com.gonthera.cli.service.node.GenerateNode;
import com.gonthera.cli.service.common.ProjectValidator;
import com.gonthera.cli.service.common.ConfigurationFileValidator;

import java.nio.file.Paths;


import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.common.Common.loadProperties;

public class Main {

    public static void main(String[] args) {
        System.out.println("Carregando metadata");
        boolean validateOnly = args.length > 0 && "--validate".equals(args[0]);
        if (validateOnly) {
            ConfigurationFileValidator.validate(Paths.get(System.getProperty("user.dir")));
        } else {
            ConfigurationFileValidator.validateForGeneration(Paths.get(System.getProperty("user.dir")));
        }
        Common.properties = loadProperties();
        ProjectValidator.validate(Common.properties);
        ProjectValidator.warnings(Common.properties).forEach(warning -> System.out.println("WARNING: " + warning));

        if (validateOnly) {
            System.out.println("Gonthera project is valid");
            return;
        }

        if(Common.properties.getLanguage() == null){
            throw new RuntimeException("Language not defined");
        }
        if(Common.properties.getLanguage() == Language.JAVA){
            GenerateJava.generateSource(Common.properties);
        }
        if(Common.properties.getLanguage() == Language.DOTNET){
            loadPath();
            GenerateDotNet.generateSource(Common.properties);
        }
        if(Common.properties.getLanguage() == Language.NODE){
            GenerateNode.generateSource(Common.properties);
        }
    }
}
