package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.Endpoints;
import com.potatotech.entitygenerator.model.Parameters;
import com.potatotech.entitygenerator.service.common.FieldsMapper;
import com.potatotech.entitygenerator.service.common.GenerateCommon;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.potatotech.entitygenerator.service.common.Common.*;
import static com.potatotech.entitygenerator.service.common.Validators.validRequestOrResponseData;

public class GenerateEndpoint {

    protected static void generateEndpoint(List<Endpoints> endpoints, String packageName, Path packagePath){
        String mod = loadWxsd("endpoint");
        String primitive = loadWxsd("primitive");


        // Gera as classes de input e output
        endpoints.forEach(endpoint -> {
            try {
                generateInputOutput(endpoint.getMetadata().getInput(),endpoint.getMethodName(),packagePath, packageName, "Input");
                generateInputOutput(endpoint.getMetadata().getOutput(),endpoint.getMethodName(),packagePath, packageName,"Output");
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        });

        var group = endpoints.stream().collect(Collectors.groupingBy(Endpoints::getGrouper));

        // isola as classes que não possui agrupamento
        var isolatedEndpoints = group.entrySet().stream().filter(e -> e.getKey().equals("")).findFirst().orElse(null);
        var groupEndpoints = group.entrySet().stream().filter(e -> !e.getKey().equals("")).collect(Collectors.toList());


        if(isolatedEndpoints != null){
            isolatedEndpoints.getValue().forEach(item -> {
                try{
                    String fileName = stringFormaterJava(item.getMethodName().concat("Primitive"),"", packagePath.toString());
                    var path = Path.of(fileName);
                    var primitives = configureMethods(item,item.getMethodName(), primitive);
                    var entity = configureFileEntity(mod,packageName,primitives);
                    entity = entity.replace("<<className>>",firstCharacterUpperCase(item.getMethodName()).concat("Primitive"));
                    Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                }catch (IOException ex){
                    ex.printStackTrace();
                }
            });
        }

        if(groupEndpoints != null){
            groupEndpoints.forEach(item -> {
                try{
                    String fileName = stringFormaterJava(firstCharacterUpperCase(item.getKey()).concat("Primitive"),"", packagePath.toString());
                    var path = Path.of(fileName);
                    AtomicReference<String> primitives = new AtomicReference<>("");
                    item.getValue().forEach(value -> {
                        primitives.set(primitives.get().concat("\n").concat(configureMethods(value,value.getMethodName(), primitive)));
                    });

                    var entity = configureFileEntity(mod,packageName, primitives.get());
                    entity = entity.replace("<<className>>",firstCharacterUpperCase(item.getKey()).concat("Primitive"));
                    Files.write(path, entity.getBytes(), StandardOpenOption.CREATE);
                }catch (IOException ex){
                    ex.printStackTrace();
                }

            });
        }


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

    private static String loadFields(List<Parameters> parameters) {
        AtomicReference<String> fields = new AtomicReference<>("");
        parameters.forEach(item -> {
            String tempField = fields.get();
            String fieldType = FieldsMapper.getFieldTypeDto(item.getParameterType());

            if(item.isList()){
                fieldType = String.format("List<%s>",fieldType);
            }
            String field = String.format("\n        public %s %s { get; set; }",fieldType,item.getParameterName());
            tempField += field;
            fields.set(tempField);

        });

        return fields.get();
    }

    private static String loadModel(String type, String packageName,String className, String fields ) {

        String mod = loadWxsd("dtorequest");

        return mod.replace("<<nameSpaceName>>",packageName.concat("_Gen"))
                .replace("<<projetcName>>",packageName)
                .replace("<<entityName>>",firstCharacterUpperCase(className))
                .replace("<<entityFields>>",fields)
                .replace("<<operation>>",type);

    }

    private static String configureFileEntity(String mod,String packageName, String primitives){
        return GenerateCommon.configureFileEntity(mod, packageName,null,"","").replace("<<primitives>>",primitives);
    }

    private static String configureMethods(Endpoints endpoints, String fileName, String primitive){
        var anonymous = "";
        if(endpoints.getMetadata().isAnonymous()){
            anonymous = "[AllowAnonymous]";
        }
        return primitive
                .replace("<<isAnonimous>>",anonymous)
                .replace("<<routeName>>",firstCharacterLowerCase(fileName))
                .replace("<<methodName>>",firstCharacterUpperCase(fileName))
                .replace("<<input>>",setParameterInput(endpoints,fileName))
                .replace("<<output>>",setParameterOutput(endpoints,fileName))
                .replace("<<httpMethod>>",firstCharacterUpperCase(endpoints.getHttpMethod().toUpperCase()));
    }

    private static String setParameterInput(Endpoints endpoints, String fileName){
        if(!validRequestOrResponseData(endpoints.getMetadata().getInput())){
            return setRequest(endpoints).concat(firstCharacterUpperCase(fileName)+ "Input input");
        } else {
            return setRequest(endpoints).concat("RequestData input");
        }
    }

    private static String setParameterOutput(Endpoints endpoints, String fileName){
        if(!validRequestOrResponseData(endpoints.getMetadata().getInput())){
            return !endpoints.getMetadata().getOutput().isEmpty() ? firstCharacterUpperCase(fileName)+ "Output" : "void";
        } else {
            return !endpoints.getMetadata().getOutput().isEmpty() ?  "ResponseData" : "?";
        }
    }

    private static String setRequest(Endpoints endpoints){
        return endpoints.getMethodName().equals("GET") ? "[FromQuery] " : "[FromBody] ";
    }
}
