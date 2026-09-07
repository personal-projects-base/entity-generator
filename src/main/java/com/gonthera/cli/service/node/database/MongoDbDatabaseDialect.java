package com.gonthera.cli.service.node.database;

import com.gonthera.cli.enuns.DatabaseProvider;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;

import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.UncheckedIOException;

import static com.gonthera.cli.service.common.Common.splitByUppercase;

public final class MongoDbDatabaseDialect implements NodeDatabaseDialect {
    @Override
    public DatabaseProvider provider() {
        return DatabaseProvider.MONGODB;
    }

    @Override
    public String prismaTemplate() {
        return "prismaschema-mongodb";
    }

    @Override
    public String scalarAttributes(EntityFields field, String mappedField) {
        if (field.getMetadata() != null && field.getMetadata().isKey()) {
            return " @id @default(uuid()) @map(\"_id\")";
        }
        return field.getFieldName().equals(mappedField) ? "" : String.format(" @map(\"%s\")", mappedField);
    }

    @Override
    public String relationScalarAttributes(EntityFields relatedKey, boolean nullable, boolean unique, String mappedField) {
        return (nullable ? "?" : "") + (unique ? " @unique" : "")
                + String.format(" @map(\"%s\")", mappedField);
    }

    @Override
    public String ownerRelationActions() {
        return ", onDelete: NoAction, onUpdate: NoAction";
    }

    @Override
    public String manyToManyFields(String fieldName, String relatedType, String relationName,
                                   EntityFields relatedKey, String mappedField) {
        String ids = fieldName + "Ids";
        String scalarType = prismaScalarType(relatedKey.getFieldProperties().getFieldType());
        return String.format("  %s %s[] @relation(\"%s\", fields: [%s], references: [%s])%n"
                        + "  %s %s[] @map(\"%s_ids\")%n",
                fieldName, relatedType, relationName, ids, relatedKey.getFieldName(),
                ids, scalarType, mappedField);
    }

    @Override
    public String modelIndexes(Entities entity, List<Entities> entities) {
        StringBuilder indexes = new StringBuilder();
        for (EntityFields field : entity.getEntityFields()) {
            if (field.getRelationShips() == null || field.getRelationShips().isBidirectional()) continue;
            String kind = field.getRelationShips().getRelationShip();
            if ("ManyToOne".equalsIgnoreCase(kind)) {
                indexes.append(String.format("  @@index([%sId])%n", field.getFieldName()));
            }
        }
        return indexes.toString();
    }

    @Override
    public String repositoryTransactionOptions() {
        return "";
    }

    @Override
    public String scalarNullPredicate() {
        return "return wrap(fields.slice(0, -1), operator === 'isNull'\n"
                + "            ? { OR: [{ [leaf.fieldName]: null }, { [leaf.fieldName]: { isSet: false } }] }\n"
                + "            : { AND: [{ [leaf.fieldName]: { not: null } }, { [leaf.fieldName]: { isSet: true } }] });";
    }

    @Override
    public boolean shortCircuitRequiredNulls() {
        return false;
    }

    @Override
    public void generateDatabaseArtifacts(List<Entities> entities, Path resourcePath) {
        try {
            Files.deleteIfExists(resourcePath.resolve("postgree.sql"));
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to remove PostgreSQL artifact for MongoDB project", ex);
        }
    }

    @Override
    public void validatePrimaryKey(Entities entity, EntityFields key) {
        if (!"uuid".equals(key.getFieldProperties().getFieldType())) {
            throw new IllegalArgumentException("Invalid Node MongoDB primary key " + entity.getEntityName() + "."
                    + key.getFieldName() + ": fieldType must be uuid");
        }
    }

    @Override
    public void validateManyToManyField(Entities entity, EntityFields field) {
        String scalarName = field.getFieldName() + "Ids";
        if (entity.getEntityFields().stream().anyMatch(candidate -> scalarName.equals(candidate.getFieldName()))) {
            throw new IllegalArgumentException("Invalid Node relation " + entity.getEntityName() + "."
                    + field.getFieldName() + ": relation identifiers conflict with configured field " + scalarName);
        }
    }

    private String prismaScalarType(String type) {
        if ("uuid".equals(type) || "string".equals(type) || "password".equals(type)) return "String";
        if ("int".equals(type) || "integer".equals(type)) return "Int";
        if ("long".equals(type)) return "BigInt";
        return "String";
    }
}
