package com.potatotech.entitygenerator.service.common;

import com.potatotech.entitygenerator.enuns.Language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.potatotech.entitygenerator.service.common.Common.*;

public class GenerateCommon {



    public static void generateFileCommon(String packageName, Path packagePath, String loadWxsd, String entityName){

        String mod = loadWxsd(loadWxsd);
        try{
            String fileName = stringFormaterJava(entityName,"", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    public static void generateResponseData(String packageName, Path packagePath){

        String mod = loadWxsd("responsedata");
        try{
            String fileName = stringFormaterJava("ResponseData","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = configureFileEntity(mod,packageName);
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    public static String configureFileEntity(String mod, String packageName){
        if(properties.getLanguage() == Language.JAVA)
            return mod.replace("<<packageName>>",packageName.concat("_gen"));
        else if(properties.getLanguage() == Language.DOTNET){
            return mod.replace("<<projetcName>>",packageName)
                    .replace("<<nameSpaceName>>",packageName.concat("_Gen"));
        }
        return null;
    }

}
