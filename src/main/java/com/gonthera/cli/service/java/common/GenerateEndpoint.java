package com.gonthera.cli.service.java.common;

import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Metadata;
import com.gonthera.cli.model.Parameters;
import com.gonthera.cli.model.*;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.gonthera.cli.service.common.Common.*;
import static com.gonthera.cli.service.common.Validators.validRequestOrResponseData;


public class GenerateEndpoint {

    public static void generateEndpoint(List<Endpoints> endpoints, String packageName, Path packagePath){

        String mod = loadWxsd("endpoint");
        endpoints.forEach(item -> {
            try{
                String fileName = stringFormaterJava(item.getMethodName(),"", packagePath.toString());
                generateInputOutput(item.getMetadata().getInput(),item.getMethodName(),packagePath, packageName, "Input");
                generateInputOutput(item.getMetadata().getOutput(),item.getMethodName(),packagePath, packageName,"Output");
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
        return mod.replace("<<rootPackage>>",packageName.concat("_gen"))
                .replace("<<dtoImports>>", generatedJavaImport(packageName, "dtos", properties.getEntities() != null && !properties.getEntities().isEmpty()))
                .replace("<<enumImports>>", generatedJavaImport(packageName, "enums", properties.getEnums() != null && !properties.getEnums().isEmpty()))
                .replace("<<packageName>>",packageName.concat("_gen.endpoints"))
                .replace("<<ifAnonimous>>",anonimous[0])
                .replace("<<className>>",firstCharacterUpperCase(fileName))
                .replace("<<methodName>>",fileName)
                .replace("<<isAnonimous>>",anonimous[1])
                .replace("<<input>>",setParameterInput(endpoints,fileName))
                .replace("<<output>>",setParameterOutput(endpoints,fileName))
                .replace("<<httpMethod>>",firstCharacterUpperCase(endpoints.getHttpMethod().toLowerCase()));
    }

    private static String setParameterInput(Endpoints endpoints, String fileName){
        if(!validRequestOrResponseData(endpoints.getMetadata().getInput())){

            var request = setRequest(endpoints);
            if(request.contains("RequestBody")){
                request =  request.concat(firstCharacterUpperCase(fileName)+ "Input input");
            } else {
                request = setRequestParam(endpoints);
            }
            return request;
        } else {
            // Aqui é caso não possua nenhum parametro de entrada, ai seta o objeto RequestData como padrão
            //(DOCUMENTAR)
            return setRequest(endpoints).concat("RequestData input");
        }
    }

    private static String setRequestParam(Endpoints endpoints) {
        var reference = new AtomicReference<String>("");
        endpoints.getMetadata().getInput().forEach(e -> {
            var fieldType = FieldsMapper.getFieldTypeEntity(e.getParameterType());

            var temp = String.format("@RequestParam(value=\"%s\",required=false) %s %s,",
                    e.getParameterName(),
                   fieldType,
                    e.getParameterName()
            );
            temp += reference.get();
            reference.set(String.format("\n        %s",temp));
        });
        
        if(!reference.get().equals("")){
            return reference.get().substring(0, reference.get().lastIndexOf(",")).concat("\n    ");
        } else {
            return reference.get();
        }

    }

    private static String setParameterOutput(Endpoints endpoints, String fileName){
        if(!validRequestOrResponseData(endpoints.getMetadata().getInput())){
            return !endpoints.getMetadata().getOutput().isEmpty() ? firstCharacterUpperCase(fileName)+ "Output" : "Void";
        } else {
            return !endpoints.getMetadata().getOutput().isEmpty() ?  "ResponseData" : "?";
        }
    }

    private static String setRequest(Endpoints endpoints){
        return endpoints.getHttpMethod().equals("GET") ? "@RequestParam " : "@RequestBody ";
    }

    private static String[] isAnonimous(Metadata metadata) {
        if(metadata.isAnonymous()){
            var anotation = String.format(
                    "import %s_gen.authorization.stereotype.Anonymous;",
                    properties.getMainPackage()
            );
            var importa = "@Anonymous";
            return new String[]{anotation, importa};
        }
        return new String[]{"", ""};
    }


    private static void generateInputOutput(List<Parameters> parameters, String className, Path packagePath, String packageName, String type) throws IOException {
        System.out.println("iniciado o input");

        if(validRequestOrResponseData(parameters)){
            return;
        }
        var strFields = loadFields(parameters);
        if(strFields.isEmpty()){
            return;
        }
        strFields = loadModel(type,packageName,className, strFields);

        String fileName = stringFormaterJava(className,type, packagePath.toString());
        var path = Path.of(fileName);
        Files.write(path, strFields.getBytes(), StandardOpenOption.CREATE);

    }




    private static String loadModel(String type, String packageName,String className, String fields ) {

        String mod = loadWxsd("dtorequest");

        return mod.replace("<<entityName>>",firstCharacterUpperCase(className))
                .replace("<<rootPackage>>",packageName.concat("_gen"))
                .replace("<<dtoImports>>", generatedJavaImport(packageName, "dtos", properties.getEntities() != null && !properties.getEntities().isEmpty()))
                .replace("<<enumImports>>", generatedJavaImport(packageName, "enums", properties.getEnums() != null && !properties.getEnums().isEmpty()))
                .replace("<<packageName>>",packageName.concat("_gen.endpoints"))
                .replace("<<entityFields>>",fields)
                .replace("<<operation>>",type);

    }


    private static String loadFields(List<Parameters> parameters) {
        AtomicReference<String> fields = new AtomicReference<>("");
        parameters.forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeDto(item.getParameterType());

            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("\n    public %s %s;",fieldType,item.getParameterName());
            tempField += field;
            fields.set(tempField);

        });

        return fields.get();
    }

}
