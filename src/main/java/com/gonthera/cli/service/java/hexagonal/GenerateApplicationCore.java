package com.gonthera.cli.service.java.hexagonal;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;

public final class GenerateApplicationCore {

    private GenerateApplicationCore() {
    }

    public static void generate(
            List<Entities> entities,
            String packageName,
            Path inputPortsPath,
            Path outputPortsPath,
            Path servicesPath
    ) {
        String inputPortTemplate = loadWxsd("hexagonalinputport");
        String outputPortTemplate = loadWxsd("hexagonaloutputport");
        String applicationServiceTemplate = loadWxsd("hexagonalapplicationservice");

        entities.stream()
                .filter(entity -> !entity.isOnlyDTO())
                .forEach(entity -> {
                    generateFile(
                            inputPortsPath,
                            entity,
                            "UseCase",
                            configure(
                                    inputPortTemplate,
                                    packageName.concat("_gen.domain.ports.in"),
                                    packageName.concat("_gen"),
                                    entity
                            )
                    );
                    generateFile(
                            outputPortsPath,
                            entity,
                            "RepositoryPort",
                            configure(
                                    outputPortTemplate,
                                    packageName.concat("_gen.domain.ports.out"),
                                    packageName.concat("_gen"),
                                    entity
                            )
                    );
                    generateFile(
                            servicesPath,
                            entity,
                            "ApplicationService",
                            configure(
                                    applicationServiceTemplate,
                                    packageName.concat("_gen.application.services"),
                                    packageName.concat("_gen"),
                                    entity
                            ).replace("<<abstract>>", entity.isServiceAbstract() ? "abstract " : "")
                    );
                });
    }

    private static String configure(String template, String generatedPackage, String rootPackage, Entities entity) {
        EntityFields key = entity.getEntityFields().stream()
                .filter(field -> field.getMetadata().isKey())
                .findFirst()
                .orElseThrow();
        String entityName = firstCharacterUpperCase(entity.getEntityName());
        String keyType = FieldsMapper.getFieldTypeDomain(key.getFieldProperties().getFieldType());

        return template
                .replace("<<packageName>>", generatedPackage)
                .replace("<<rootPackage>>", rootPackage)
                .replace("<<entityName>>", entityName)
                .replace("<<keyType>>", keyType)
                .replace("<<keySetter>>", firstCharacterUpperCase(key.getFieldName()));
    }

    private static void generateFile(Path path, Entities entity, String suffix, String content) {
        try {
            String fileName = stringFormaterJava(entity.getEntityName(), suffix, path.toString());
            Files.write(Path.of(fileName), content.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not generate " + suffix + " for " + entity.getEntityName(),
                    ex
            );
        }
    }
}
