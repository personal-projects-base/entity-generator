package com.gonthera.cli.service.java.mvc;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;

public class GenerateService {

    public static void generateServices(List<Entities> entities, String packageName, Path packagePath) {
        String template = loadWxsd("service");
        entities.forEach(entity -> {
            if (entity.isOnlyDTO()) return;
            try {
                String fileName = stringFormaterJava(entity.getEntityName(), "Service", packagePath.toString());
                Files.write(
                        Path.of(fileName),
                        configureFile(template, packageName, entity).getBytes(),
                        StandardOpenOption.CREATE
                );
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static String configureFile(String template, String packageName, Entities entity) {
        var keyField = entity.getEntityFields().stream().filter(field -> field.getMetadata().isKey()).findFirst().orElseThrow();
        String keyType = FieldsMapper.getFieldTypeEntity(keyField.getFieldProperties().getFieldType());
        return template
                .replace("<<serviceAnnotation>>", entity.isServiceAbstract() ? "" : "@Service")
                .replace("<<abstract>>", entity.isServiceAbstract() ? "abstract " : "")
                .replace("<<entityName>>", firstCharacterUpperCase(entity.getEntityName()))
                .replace("<<keyType>>", keyType)
                .replace("<<keySetter>>", firstCharacterUpperCase(keyField.getFieldName()))
                .replace("<<rootPackage>>", packageName.concat("_gen"))
                .replace("<<packageName>>", packageName.concat("_gen.services"));
    }
}
