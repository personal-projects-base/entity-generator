package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.potatotech.entitygenerator.service.common.Common.loadWxsd;
import static com.potatotech.entitygenerator.service.common.Common.stringFormaterJava;
import static com.potatotech.entitygenerator.service.common.GenerateCommon.configureFileEntity;

public class GenerateRepository {

    protected static void generateRepositories(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("repository");
        entities.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEntityName(),"Repository", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileEntity(mod,packageName,item,item.getEntityName(),"");
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }
}
