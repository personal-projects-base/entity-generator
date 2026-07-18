package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.model.Parameters;
import com.gonthera.cli.service.common.Common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterLowerCase;
import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;

public class NodeCommon {

    public static void writeFile(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.write(file, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static String className(String name) {
        return firstCharacterUpperCase(name);
    }

    public static String varName(String name) {
        return firstCharacterLowerCase(name);
    }

    public static String fileName(String name) {
        return firstCharacterLowerCase(name);
    }

    public static String prismaModelName(Entities entity) {
        return className(entity.getEntityName());
    }

    public static String prismaDelegateName(Entities entity) {
        return varName(entity.getEntityName());
    }

    public static String typeName(String type) {
        if (Common.properties != null && Common.properties.getEnums() != null && Common.properties.getEnums().stream().anyMatch(e -> e.getEnumName().equalsIgnoreCase(type))) {
            return className(type);
        }
        switch (type) {
            case "uuid":
            case "string":
            case "password":
            case "datetime":
            case "date":
                return "string";
            case "int":
            case "integer":
            case "long":
            case "decimal":
            case "double":
                return "number";
            case "boolean":
                return "boolean";
            case "byte":
            case "byte[]":
            case "inputStream":
                return "Buffer";
            case "map":
                return "Record<string, unknown>";
            default:
                return className(type) + "DTO";
        }
    }

    public static String fieldType(EntityFields field) {
        String type = typeName(field.getFieldProperties().getFieldType());
        if (field.isList()) {
            return String.format("%s[]", type);
        }
        return type;
    }

    public static String parameterType(Parameters parameter) {
        String type = typeName(parameter.getParameterType());
        if (parameter.isList()) {
            return String.format("%s[]", type);
        }
        return type;
    }

    public static String importsForEntityReferences(Entities entity) {
        StringBuilder imports = new StringBuilder();
        entity.getEntityFields().forEach(field -> {
            String type = field.getFieldProperties().getFieldType();
            if (isEnum(type)) {
                imports.append(String.format("import { %s } from '../enums/%s.enum';%n", className(type), fileName(type)));
            } else if (!isPrimitive(type)) {
                imports.append(String.format("import type { %sDTO } from './%s.model';%n", className(type), fileName(type)));
            }
        });
        return imports.toString();
    }

    public static boolean isEnum(String type) {
        return Common.properties != null && Common.properties.getEnums() != null && Common.properties.getEnums().stream().anyMatch(e -> e.getEnumName().equalsIgnoreCase(type));
    }

    public static boolean isPrimitive(String type) {
        return List.of("uuid", "string", "password", "datetime", "date", "int", "integer", "long", "decimal", "double", "boolean", "byte", "byte[]", "inputStream", "map").contains(type);
    }
}
