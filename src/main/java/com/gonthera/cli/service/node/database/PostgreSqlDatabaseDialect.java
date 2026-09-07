package com.gonthera.cli.service.node.database;

import com.gonthera.cli.enuns.DatabaseProvider;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;

import java.util.List;
import java.nio.file.Path;

import static com.gonthera.cli.service.common.GenerateSQL.generateSql;

public final class PostgreSqlDatabaseDialect implements NodeDatabaseDialect {
    @Override
    public DatabaseProvider provider() {
        return DatabaseProvider.POSTGRESQL;
    }

    @Override
    public String prismaTemplate() {
        return "prismaschema-postgresql";
    }

    @Override
    public String scalarAttributes(EntityFields field, String mappedField) {
        StringBuilder attributes = new StringBuilder();
        if (field.getMetadata() != null && field.getMetadata().isKey()) {
            attributes.append(" @id");
            String type = field.getFieldProperties().getFieldType();
            if ("uuid".equals(type)) attributes.append(" @default(uuid())");
            if ("int".equals(type) || "integer".equals(type)) attributes.append(" @default(autoincrement())");
        }
        if (!field.getFieldName().equals(mappedField)) {
            attributes.append(String.format(" @map(\"%s\")", mappedField));
        }
        attributes.append(nativeType(field.getFieldProperties().getFieldType()));
        return attributes.toString();
    }

    @Override
    public String relationScalarAttributes(EntityFields relatedKey, boolean nullable, boolean unique, String mappedField) {
        return (nullable ? "?" : "") + (unique ? " @unique" : "")
                + nativeType(relatedKey.getFieldProperties().getFieldType())
                + String.format(" @map(\"%s\")", mappedField);
    }

    @Override
    public String ownerRelationActions() {
        return "";
    }

    @Override
    public String manyToManyFields(String fieldName, String relatedType, String relationName,
                                   EntityFields relatedKey, String mappedField) {
        return String.format("  %s %s[] @relation(\"%s\")%n", fieldName, relatedType, relationName);
    }

    @Override
    public String modelIndexes(Entities entity, List<Entities> entities) {
        return "";
    }

    @Override
    public String repositoryTransactionOptions() {
        return ", { isolationLevel: Prisma.TransactionIsolationLevel.RepeatableRead }";
    }

    @Override
    public String scalarNullPredicate() {
        return "predicate = operator === 'isNull' ? null : { not: null };";
    }

    @Override
    public boolean shortCircuitRequiredNulls() {
        return true;
    }

    @Override
    public void generateDatabaseArtifacts(List<Entities> entities, Path resourcePath) {
        generateSql(entities);
    }

    @Override
    public void validatePrimaryKey(Entities entity, EntityFields key) {
        // PostgreSQL accepts every scalar key type already supported by the Node generator.
    }

    @Override
    public void validateManyToManyField(Entities entity, EntityFields field) {
        // PostgreSQL implicit relations do not create a scalar IDs field in the model.
    }

    private String nativeType(String type) {
        if ("uuid".equals(type)) return " @db.Uuid";
        if ("date".equals(type)) return " @db.Date";
        return "";
    }
}
