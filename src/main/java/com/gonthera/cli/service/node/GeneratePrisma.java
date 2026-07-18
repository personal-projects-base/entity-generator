package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.model.Enums;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.gonthera.cli.service.common.Common.getTableName;
import static com.gonthera.cli.service.common.Common.loadPath;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.splitByUppercase;
import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.isEnum;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;

public class GeneratePrisma {

    public static void generatePrismaSchema(List<Entities> entities, List<Enums> enums) {
        try {
            Path schemaPath = Path.of(loadPath()).resolve("prisma").resolve("schema.prisma");
            String schema = loadWxsd("prismaschema")
                    .replace("<<enums>>", generateEnums(enums))
                    .replace("<<models>>", generateModels(entities));
            writeFile(schemaPath, schema);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String generateEnums(List<Enums> enums) {
        if (enums == null || enums.isEmpty()) {
            return "";
        }

        return enums.stream()
                .map(enumeration -> String.format("enum %s {%n%s%n}%n",
                        className(enumeration.getEnumName()),
                        enumeration.getValues().stream().map(value -> "  " + value).collect(Collectors.joining("\n"))
                ))
                .collect(Collectors.joining("\n"));
    }

    private static String generateModels(List<Entities> entities) {
        if (entities == null || entities.isEmpty()) {
            return "";
        }

        return entities.stream()
                .map(entity -> generateModel(entity, entities))
                .collect(Collectors.joining("\n"));
    }

    private static String generateModel(Entities entity, List<Entities> entities) {
        StringBuilder fields = new StringBuilder();

        entity.getEntityFields().forEach(field -> appendField(fields, entity, field, entities));

        return String.format("model %s {%n%s%n  @@map(\"%s\")%n}%n",
                className(entity.getEntityName()),
                fields,
                splitByUppercase(getTableName(entity))
        );
    }

    private static void appendField(StringBuilder fields, Entities owner, EntityFields field, List<Entities> entities) {
        Entities relatedEntity = findEntity(entities, field.getFieldProperties().getFieldType());
        if (relatedEntity != null) {
            appendRelationField(fields, owner, field, relatedEntity);
            return;
        }

        fields.append(String.format("  %s %s%s%n",
                field.getFieldName(),
                prismaType(field),
                attributes(field)
        ));
    }

    private static void appendRelationField(StringBuilder fields, Entities owner, EntityFields field, Entities relatedEntity) {
        String relationShip = field.getRelationShips() == null ? "" : field.getRelationShips().getRelationShip();
        String relatedType = className(relatedEntity.getEntityName());

        if ("OneToMany".equalsIgnoreCase(relationShip) || "ManyToMany".equalsIgnoreCase(relationShip) || field.isList()) {
            fields.append(String.format("  %s %s[]%n", field.getFieldName(), relatedType));
            return;
        }

        if (field.getRelationShips() != null && field.getRelationShips().isBidirectional()) {
            fields.append(String.format("  %s %s%s%n", field.getFieldName(), relatedType, nullable(field)));
            return;
        }

        EntityFields pkField = primaryKey(relatedEntity);
        String relationScalarName = field.getFieldName() + "Id";
        String relationName = String.format("%s_%s_%s", className(owner.getEntityName()), className(relatedEntity.getEntityName()), field.getFieldName());

        fields.append(String.format("  %s %s%s @relation(\"%s\", fields: [%s], references: [%s])%n",
                field.getFieldName(),
                relatedType,
                nullable(field),
                relationName,
                relationScalarName,
                pkField.getFieldName()
        ));
        fields.append(String.format("  %s %s%s @map(\"%s\")%n",
                relationScalarName,
                scalarPrismaType(pkField.getFieldProperties().getFieldType()),
                nullable(field),
                splitByUppercase(field.getFieldName())
        ));
    }

    private static String prismaType(EntityFields field) {
        String type = field.getFieldProperties().getFieldType();
        String prismaType = scalarPrismaType(type);
        if (isEnum(type)) {
            prismaType = className(type);
        }
        if (field.isList()) {
            return prismaType + "[]";
        }
        return prismaType + nullable(field);
    }

    private static String scalarPrismaType(String type) {
        switch (type) {
            case "uuid":
            case "string":
            case "password":
                return "String";
            case "datetime":
            case "date":
                return "DateTime";
            case "int":
            case "integer":
                return "Int";
            case "long":
                return "BigInt";
            case "decimal":
            case "double":
                return "Float";
            case "boolean":
                return "Boolean";
            case "byte":
            case "byte[]":
            case "inputStream":
                return "Bytes";
            case "map":
                return "Json";
            default:
                return "Json";
        }
    }

    private static String attributes(EntityFields field) {
        StringBuilder attributes = new StringBuilder();
        if (field.getMetadata() != null && field.getMetadata().isKey()) {
            attributes.append(" @id");
            if ("uuid".equals(field.getFieldProperties().getFieldType())) {
                attributes.append(" @default(uuid())");
            }
            if ("int".equals(field.getFieldProperties().getFieldType()) || "integer".equals(field.getFieldProperties().getFieldType())) {
                attributes.append(" @default(autoincrement())");
            }
        }

        String mappedField = splitByUppercase(field.getFieldName());
        if (!field.getFieldName().equals(mappedField)) {
            attributes.append(String.format(" @map(\"%s\")", mappedField));
        }

        if ("uuid".equals(field.getFieldProperties().getFieldType())) {
            attributes.append(" @db.Uuid");
        }
        if ("date".equals(field.getFieldProperties().getFieldType())) {
            attributes.append(" @db.Date");
        }

        return attributes.toString();
    }

    private static String nullable(EntityFields field) {
        if (field.getMetadata() != null && !field.getMetadata().isNullable()) {
            return "";
        }
        if (field.getMetadata() != null && field.getMetadata().isKey()) {
            return "";
        }
        return "?";
    }

    private static Entities findEntity(List<Entities> entities, String entityName) {
        return entities.stream().filter(entity -> entity.getEntityName().equals(entityName)).findFirst().orElse(null);
    }

    private static EntityFields primaryKey(Entities entity) {
        return entity.getEntityFields().stream()
                .filter(field -> field.getMetadata() != null && field.getMetadata().isKey())
                .findFirst()
                .orElse(entity.getEntityFields().get(0));
    }
}
