package com.gonthera.cli.service.java.hexagonal;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.generatedJavaImport;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.properties;
import static com.gonthera.cli.service.common.Common.setComments;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;

public final class GenerateDomain {

    private GenerateDomain() {
    }

    public static void generateDomains(List<Entities> entities, String packageName, Path packagePath) {
        String template = loadWxsd("domain");
        entities.forEach(entity -> {
            try {
                String fileName = stringFormaterJava(entity.getEntityName(), "", packagePath.toString());
                Files.write(
                        Path.of(fileName),
                        configure(template, packageName, entity).getBytes(),
                        StandardOpenOption.CREATE
                );
            } catch (IOException ex) {
                throw new IllegalStateException("Could not generate domain " + entity.getEntityName(), ex);
            }
        });
    }

    private static String configure(String template, String packageName, Entities entity) {
        String entityName = firstCharacterUpperCase(entity.getEntityName());
        return template
                .replace("<<packageName>>", packageName.concat("_gen.domain.model"))
                .replace("<<enumImports>>", generatedJavaImport(
                        packageName,
                        "enums",
                        properties.getEnums() != null && !properties.getEnums().isEmpty()
                ))
                .replace("<<entityName>>", entityName)
                .replace("<<entityFields>>", fields(entity))
                .replace("<<constructorParameters>>", constructorParameters(entity))
                .replace("<<constructorAssignments>>", constructorAssignments(entity))
                .replace("<<accessors>>", accessors(entity));
    }

    private static String fields(Entities entity) {
        return entity.getEntityFields().stream()
                .map(field -> setComments(field.getComment()) + "\n    private " + type(field) + " " + field.getFieldName() + ";")
                .collect(Collectors.joining("\n    "));
    }

    private static String constructorParameters(Entities entity) {
        return entity.getEntityFields().stream()
                .map(field -> type(field) + " " + field.getFieldName())
                .collect(Collectors.joining(", "));
    }

    private static String constructorAssignments(Entities entity) {
        return entity.getEntityFields().stream()
                .map(field -> "this." + field.getFieldName() + " = " + field.getFieldName() + ";")
                .collect(Collectors.joining("\n        "));
    }

    private static String accessors(Entities entity) {
        return entity.getEntityFields().stream()
                .map(GenerateDomain::accessors)
                .collect(Collectors.joining("\n\n    "));
    }

    private static String accessors(EntityFields field) {
        String fieldName = field.getFieldName();
        String methodName = firstCharacterUpperCase(fieldName);
        String type = type(field);
        return "public " + type + " get" + methodName + "() {\n"
                + "        return " + fieldName + ";\n"
                + "    }\n\n"
                + "    public void set" + methodName + "(" + type + " " + fieldName + ") {\n"
                + "        this." + fieldName + " = " + fieldName + ";\n"
                + "    }";
    }

    private static String type(EntityFields field) {
        String type = FieldsMapper.getFieldTypeDomain(field.getFieldProperties().getFieldType());
        return field.isList() ? "List<" + type + ">" : type;
    }
}
