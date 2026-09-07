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
        validateCollectionRelations(entities);
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

        if ("OneToOne".equalsIgnoreCase(relationShip)) {
            appendOneToOneField(fields, owner, field, relatedEntity);
            return;
        }

        if ("OneToMany".equalsIgnoreCase(relationShip) || "ManyToMany".equalsIgnoreCase(relationShip)) {
            if (!field.isList()) throw relationError(owner, field, "must have list=true");
            if ("OneToMany".equalsIgnoreCase(relationShip) && !field.getRelationShips().isBidirectional()) {
                throw relationError(owner, field, "OneToMany must be inverse; declare a ManyToOne owner on the related entity");
            }
            String name;
            if (field.getRelationShips().isBidirectional()) {
                String expected = "OneToMany".equalsIgnoreCase(relationShip) ? "ManyToOne" : "ManyToMany";
                EntityFields owningField = resolveCollectionOwner(owner, field, relatedEntity, expected);
                name = relationName(relatedEntity, owningField, owner);
            } else {
                requireInverse(owner, field, relatedEntity, "ManyToMany");
                name = relationName(owner, field, relatedEntity);
            }
            fields.append(String.format("  %s %s[] @relation(\"%s\")%n", field.getFieldName(), relatedType, name));
            return;
        }

        if (!"ManyToOne".equalsIgnoreCase(relationShip)) {
            throw relationError(owner, field, "unsupported or missing relationShip");
        }
        if (field.isList() || field.getRelationShips().isBidirectional()) {
            throw relationError(owner, field, "ManyToOne must have list=false and bidirectional=false");
        }
        requireInverse(owner, field, relatedEntity, "OneToMany");
        EntityFields pkField = primaryKey(relatedEntity);
        String scalarName = field.getFieldName() + "Id";
        if (owner.getEntityFields().stream().anyMatch(candidate -> scalarName.equals(candidate.getFieldName()))) {
            throw relationError(owner, field, "foreign key conflicts with configured field " + scalarName);
        }
        fields.append(String.format("  %s %s%s @relation(\"%s\", fields: [%s], references: [%s])%n",
                field.getFieldName(), relatedType, nullable(field), relationName(owner, field, relatedEntity),
                scalarName, pkField.getFieldName()));
        String nativeType = "uuid".equals(pkField.getFieldProperties().getFieldType()) ? " @db.Uuid"
                : "date".equals(pkField.getFieldProperties().getFieldType()) ? " @db.Date" : "";
        fields.append(String.format("  %s %s%s%s @map(\"%s\")%n", scalarName,
                scalarPrismaType(pkField.getFieldProperties().getFieldType()), nullable(field), nativeType,
                splitByUppercase(field.getFieldName())));
    }

    private static String mappedBy(Entities inverseEntity, EntityFields inverse) {
        String value = inverse.getRelationShips().getMappedBy();
        return value == null || value.isEmpty() ? inverseEntity.getEntityName() : value;
    }

    private static EntityFields resolveCollectionOwner(Entities inverseEntity, EntityFields inverse,
                                                        Entities relatedEntity, String expected) {
        List<EntityFields> matches = relatedEntity.getEntityFields().stream()
                .filter(candidate -> mappedBy(inverseEntity, inverse).equals(candidate.getFieldName()))
                .filter(candidate -> inverseEntity.getEntityName().equals(candidate.getFieldProperties().getFieldType()))
                .filter(candidate -> candidate.getRelationShips() != null
                        && expected.equalsIgnoreCase(candidate.getRelationShips().getRelationShip())
                        && !candidate.getRelationShips().isBidirectional())
                .collect(Collectors.toList());
        if (matches.size() != 1) {
            throw relationError(inverseEntity, inverse, "invalid mappedBy -> " + relatedEntity.getEntityName()
                    + "." + mappedBy(inverseEntity, inverse) + "; expected one " + expected + " owner");
        }
        return matches.get(0);
    }

    private static void requireInverse(Entities owner, EntityFields field, Entities relatedEntity, String expected) {
        long count = relatedEntity.getEntityFields().stream()
                .filter(candidate -> owner.getEntityName().equals(candidate.getFieldProperties().getFieldType()))
                .filter(candidate -> candidate.getRelationShips() != null
                        && expected.equalsIgnoreCase(candidate.getRelationShips().getRelationShip())
                        && candidate.getRelationShips().isBidirectional()
                        && field.getFieldName().equals(mappedBy(relatedEntity, candidate)))
                .count();
        if (count != 1) throw relationError(owner, field, "expected exactly one " + expected
                + " inverse with mappedBy=" + field.getFieldName() + "; found " + count);
    }

    private static IllegalArgumentException relationError(Entities entity, EntityFields field, String message) {
        return new IllegalArgumentException("Invalid Node relation " + entity.getEntityName() + "."
                + field.getFieldName() + ": " + message);
    }

    public static void validateCollectionRelations(List<Entities> entities) {
        if (entities == null) return;
        for (Entities entity : entities) {
            long keyCount = entity.getEntityFields().stream()
                    .filter(field -> field.getMetadata() != null && field.getMetadata().isKey())
                    .count();
            if (keyCount != 1) {
                throw new IllegalArgumentException("Invalid Node primary key " + entity.getEntityName()
                        + ": expected exactly one key; found " + keyCount);
            }
            EntityFields key = primaryKey(entity);
            if (key.isList() || findEntity(entities, key.getFieldProperties().getFieldType()) != null) {
                throw new IllegalArgumentException("Invalid Node primary key " + entity.getEntityName() + "."
                        + key.getFieldName() + ": keys must be scalar fields");
            }
            for (EntityFields field : entity.getEntityFields()) {
                if (field.getRelationShips() == null) continue;
                String kind = field.getRelationShips().getRelationShip();
                if (!"OneToMany".equalsIgnoreCase(kind) && !"ManyToOne".equalsIgnoreCase(kind)
                        && !"ManyToMany".equalsIgnoreCase(kind)
                        && !("OneToOne".equalsIgnoreCase(kind)
                            && entity.getEntityName().equals(field.getFieldProperties().getFieldType()))) continue;
                Entities related = findEntity(entities, field.getFieldProperties().getFieldType());
                if (related == null) throw relationError(entity, field, "related entity does not exist");
                appendRelationField(new StringBuilder(), entity, field, related);
            }
        }
    }

    private static void appendOneToOneField(StringBuilder fields, Entities entity, EntityFields field, Entities relatedEntity) {
        if (field.isList()) {
            throw new IllegalArgumentException("Node OneToOne cannot be a list: " + entity.getEntityName() + "." + field.getFieldName());
        }

        if (field.getRelationShips().isBidirectional()) {
            // Preserve the Java generator's mappedBy fallback to the inverse entity name.
            String mappedBy = field.getRelationShips().getMappedBy();
            String ownerFieldName = mappedBy == null || mappedBy.isEmpty() ? entity.getEntityName() : mappedBy;
            EntityFields ownerField = relatedEntity.getEntityFields().stream()
                    .filter(candidate -> ownerFieldName.equals(candidate.getFieldName()))
                    .filter(candidate -> entity.getEntityName().equals(candidate.getFieldProperties().getFieldType()))
                    .filter(candidate -> candidate.getRelationShips() != null
                            && "OneToOne".equalsIgnoreCase(candidate.getRelationShips().getRelationShip())
                            && !candidate.getRelationShips().isBidirectional() && !candidate.isList())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Node OneToOne mappedBy: "
                            + entity.getEntityName() + "." + field.getFieldName() + " -> "
                            + relatedEntity.getEntityName() + "." + ownerFieldName));
            // Prisma requires the side without a foreign key to be optional.
            fields.append(String.format("  %s %s? @relation(\"%s\")%n",
                    field.getFieldName(), className(relatedEntity.getEntityName()),
                    relationName(relatedEntity, ownerField, entity)));
            return;
        }

        if (entity.getEntityName().equals(relatedEntity.getEntityName())) {
            requireInverse(entity, field, relatedEntity, "OneToOne");
        }
        EntityFields key = primaryKey(relatedEntity);
        String scalarName = field.getFieldName() + "Id";
        if (entity.getEntityFields().stream().anyMatch(candidate -> scalarName.equals(candidate.getFieldName()))) {
            throw new IllegalArgumentException("Node OneToOne foreign key conflicts with configured field: "
                    + entity.getEntityName() + "." + scalarName);
        }
        fields.append(String.format("  %s %s%s @relation(\"%s\", fields: [%s], references: [%s])%n",
                field.getFieldName(), className(relatedEntity.getEntityName()), nullable(field),
                relationName(entity, field, relatedEntity), scalarName, key.getFieldName()));
        String nativeType = "uuid".equals(key.getFieldProperties().getFieldType()) ? " @db.Uuid"
                : "date".equals(key.getFieldProperties().getFieldType()) ? " @db.Date" : "";
        fields.append(String.format("  %s %s%s @unique%s @map(\"%s\")%n",
                scalarName, scalarPrismaType(key.getFieldProperties().getFieldType()), nullable(field),
                nativeType, splitByUppercase(field.getFieldName())));
    }

    private static String relationName(Entities owner, EntityFields field, Entities relatedEntity) {
        return String.format("%s_%s_%s", className(owner.getEntityName()),
                className(relatedEntity.getEntityName()), field.getFieldName());
    }

    private static String prismaType(EntityFields field) {
        String type = field.getFieldProperties().getFieldType();
        String prismaType = scalarPrismaType(type);
        if (isEnum(type)) {
            prismaType = NodeCommon.typeName(type);
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
