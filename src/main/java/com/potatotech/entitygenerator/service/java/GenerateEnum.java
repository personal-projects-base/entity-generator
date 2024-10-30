package com.potatotech.entitygenerator.service.java;

import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.Enums;
import com.potatotech.entitygenerator.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.*;


public class GenerateEnum {


    protected static void generateEnum(List<Enums> enums, String packageName, Path packagePath){

        String mod = loadWxsd("enum");
        enums.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getEnumName(),"", packagePath.toString());
                var path = Path.of(fileName);
                var entity = configureFileEnum(mod,packageName,item,item.getEnumName());
                Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }


    private static String configureFileEnum(String mod, String packageName, Enums enumeration, String fileName){

        String fields = getEnum(enumeration);
        return mod.replace("<<enumNameName>>",firstCharacterUpperCase(fileName))
                .replace("<<packageName>>",packageName.concat("_gen"))
                .replace("<<enumFields>>",fields);
    }


    private static String getEnum(Enums enumeration) {
        AtomicReference<String> fields = new AtomicReference<>("");
        enumeration.getValues().forEach(item -> {
            String tempField = fields.get();
            tempField += String.format("\n    %s,",item);
            fields.set(tempField);

        });
        return fields.get().substring(0, fields.get().length() - 1);
    }
}
