package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.GenerateCommon.configureFileEntity;

public class GenerateContext {

    protected static void generateDbContext(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("dbcontext");
        AtomicReference<String> entityAtomic = new AtomicReference<>("");

        entities.forEach(item -> {
            var entityName = firstCharacterUpperCase(item.getEntityName());
            entityAtomic.set(entityAtomic.get().concat(String.format("\n        public DbSet<%sEntity> %sContext { get; set; }",entityName,entityName)));
        });

        try{
            String fileName = stringFormaterJava("CustomDbContext","", packagePath.toString());
            var path = Path.of(fileName);
            var entity = mod.replace("<<dbSet>>", entityAtomic.get());
            entity = configureFileEntity(entity,packageName,null,"","");
            Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }
}
