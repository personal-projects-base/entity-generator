package com.potatotech.entitygenerator.service;

import com.potatotech.entitygenerator.model.*;

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
                generateInput(item.getMetadata().getInput(),item.getMethodName(),packagePath);
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
                .replace("<<input>>","")
                .replace("<<output>>","")
                .replace("<<httpMethod>>",firstCharacterUpperCase(endpoints.getHttpMethod().toLowerCase()));
    }

    private static String[] isAnonimous(Metadata metadata) {
        if(metadata.isAnonymous()){
            var anotation = "import com.potatotech.authenticate.stereotype.Anonymous;";
            var importa = "@Anonymous";
            return new String[]{anotation, importa};
        }
        return new String[]{"", ""};
    }


    private static void generateInput(List<Parameters> parameters, String className,Path packagePath) throws IOException {
        System.out.println("iniciado o inpout");

        AtomicReference<String> fields = new AtomicReference<>("");
        parameters.forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldType(item.getParameterType());

            if(!fieldType.contains("Entity")){
                if(item.isList()){
                    fieldType = String.format("List<%s>",fieldType);
                }
                String field = String.format("%s %s, ",fieldType,item.getParameterName());
                tempField += field;
                fields.set(tempField);
            }
        });

        var strFields = fields.get().substring(0, fields.get().length() -2);

        String fileName = stringFormater(className,"Input", packagePath.toString());
        var path = Path.of(fileName);
        Files.write(path, strFields.getBytes(), StandardOpenOption.CREATE);

    }

    private static String generateInput(Entities entity) {
        AtomicReference<String> fields = new AtomicReference<>("");
        entity.getEntityFields().forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldType(item.getFieldProperties().getFieldType());

            //fieldType = fieldType.replace("Entity","DTO");
            if(!fieldType.contains("Entity")){
                if(item.isList()){
                    fieldType = String.format("List<%s>",fieldType);
                }
                String field = String.format("%s %s, ",fieldType,item.getFieldName());
                tempField += field;
                fields.set(tempField);
            }
        });
        return fields.get().substring(0, fields.get().length() -2);
    }

}
