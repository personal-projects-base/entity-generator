package com.potatotech.entitygenerator.service.common;

import com.potatotech.entitygenerator.enuns.Language;
import com.potatotech.entitygenerator.model.Entities;

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
            var entity = configureFileEntity(mod,packageName, null,"","");
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    public static String configureFileEntity(String mod, String packageName, Entities entity, String fileName, String fields){

        var tableName = (entity == null ? "" : getTableName(entity));
        var entityName = (fileName.equals("")  ? "" : firstCharacterUpperCase(fileName));
        var entityLowerName = (fileName.equals("")  ? "" : firstCharacterLowerCase(fileName));

         var ret = mod.replace("<<entityName>>",entityName)
                    .replace("<<entityFields>>",fields)
                    .replace("<<entity>>",entityLowerName)
                    .replace("<<tableName>>",tableName)
                    .replace("<<projetcName>>",packageName);


        if(properties.getLanguage() == Language.JAVA){
            ret = ret.replace("<<packageName>>",packageName.concat("_gen"));
        }
        else if(properties.getLanguage() == Language.DOTNET){
            ret = ret.replace("<<nameSpaceName>>",packageName.concat("_Gen"));
        }

        return ret;
    }

}
