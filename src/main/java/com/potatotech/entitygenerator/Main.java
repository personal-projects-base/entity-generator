package com.potatotech.entitygenerator;

import com.potatotech.entitygenerator.enuns.Language;

import com.potatotech.entitygenerator.service.common.Common;
import com.potatotech.entitygenerator.service.dotNet.GenerateDotNet;
import com.potatotech.entitygenerator.service.java.GenerateJava;


import static com.potatotech.entitygenerator.service.common.Common.loadPath;
import static com.potatotech.entitygenerator.service.common.Common.loadProperties;

public class Main {

    public static void main(String[] args) {
        System.out.println("Carregando metadata");
        Common.properties = loadProperties();

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
    }
}
