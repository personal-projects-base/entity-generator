package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.GenerateCommon.configureFileEntity;

public class GenerateHandler {


    protected static void generateHandler(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("handlerbase");
        entities.forEach(item -> {
            try{
                if(item.isGenerateDefaultHandlers()){
                    String fileName = stringFormaterJava(firstCharacterUpperCase(item.getEntityName()),"Handler", packagePath.toString());
                    var path = Path.of(fileName);
                    var entity = configureFileEntity(mod,packageName,item,item.getEntityName(),"");
                    Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }

    protected static void generateHandlerImpl(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("handlerimpl");
        entities.forEach(item -> {
            try{
                if(item.isGenerateDefaultHandlers()){
                    String fileName = stringFormaterJava(firstCharacterUpperCase(item.getEntityName()),"HandlerImpl", packagePath.toString());
                    var path = Path.of(fileName);
                    var entity = configureFileEntity(mod,packageName,item,item.getEntityName(),"");
                    Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }
}
