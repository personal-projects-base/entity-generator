package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Endpoints;
import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.Common.*;


public class GenerateEndpoint {

    protected static void generateEndpoint(List<Endpoints> endpoints, String packageName, Path packagePath){

        String mod = loadWxsd("endpoint");
        endpoints.forEach(item -> {
            try{
                String fileName = stringFormater(item.getMethodName(),"Handler", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileEntity(mod,packageName,item,item.getMethodName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }

    private static String configureFileEntity(String mod, String packageName, Endpoints endpoints, String fileName){

        return mod.replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<ifAnonimous>>",null)
                .replace("<<className>>",firstCharacterUpperCase(fileName))
                .replace("<<methodName>>",fileName)
                .replace("<<isAnonimous>>",null)
                .replace("<<input>>",null)
                .replace("<<output>>",null)
                .replace("<<httpMethod>>",null);
    }

}
