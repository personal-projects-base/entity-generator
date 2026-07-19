package com.gonthera.cli.service.java;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.*;

public class GenerateController {

    protected static void generateControllers(List<Entities> entities, String packageName, Path packagePath){

        String mod = loadWxsd("controllerentities");
        entities.forEach(item -> {
            try{
                if(item.isGenerateDefaultControllers() && !item.isOnlyDTO()){
                    String fileName = stringFormaterJava(item.getEntityName(),"Controller", packagePath.toString());
                    var path = Path.of(fileName);
                    var entity = configureFile(mod,packageName,item,item.getEntityName());
                    Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                }
            }catch (IOException ex){
                ex.printStackTrace();
            }
        });
    }

    private static String configureFile(String mod,String packageName, Entities entity,String fileName){

        var fieldTypeIdentity = entity.getEntityFields().stream().filter(item -> item.getMetadata().isKey()).findFirst().orElse(null);
        var typeField = fieldTypeIdentity.getFieldProperties().getFieldType();
        var fieldType = FieldsMapper.getFieldTypeEntity(typeField);
        return mod.replace("<<entityName>>",firstCharacterUpperCase(fileName))
                .replace("<<abstract>>",(entity.isControllerAbstract() ? "abstract " : ""))
                .replace("<<entityNameLowerCase>>",fileName)
                .replace("<<keyType>>", fieldType)
                .replace("<<rootPackage>>",packageName.concat("_gen"))
                .replace("<<packageName>>",packageName.concat("_gen.controllers"));
    }
}
