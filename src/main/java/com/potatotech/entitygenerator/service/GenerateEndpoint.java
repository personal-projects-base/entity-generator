package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.Endpoints;
import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.EntityFields;
import com.potatotech.entitygenerator.model.Metadata;

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

        var anonimous = isAnonimous(endpoints.getMetadata());
        return mod.replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<ifAnonimous>>",anonimous[0])
                .replace("<<className>>",firstCharacterUpperCase(fileName))
                .replace("<<methodName>>",fileName)
                .replace("<<isAnonimous>>",anonimous[1])
                .replace("<<input>>",null)
                .replace("<<output>>",null)
                .replace("<<httpMethod>>",firstCharacterUpperCase(endpoints.getHttpMethod().toLowerCase()));
    }

    private static String[] isAnonimous(Metadata metadata) {
        if(metadata.isAnonymous()){
            var anotation = "import com.potatotech.authenticate.stereotype.Anonymous;";
            var importa = "@Anonymous";
            return new String[]{anotation, importa};
        }
        return new String[]{"", "importa"};
    }

}
