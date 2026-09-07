package com.gonthera.cli.service.node.database;

import com.gonthera.cli.enuns.DatabaseProvider;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;

import java.util.List;
import java.nio.file.Path;

public interface NodeDatabaseDialect {
    DatabaseProvider provider();

    String prismaTemplate();

    String scalarAttributes(EntityFields field, String mappedField);

    String relationScalarAttributes(EntityFields relatedKey, boolean nullable, boolean unique, String mappedField);

    String ownerRelationActions();

    String manyToManyFields(String fieldName, String relatedType, String relationName,
                            EntityFields relatedKey, String mappedField);

    String modelIndexes(Entities entity, List<Entities> entities);

    String repositoryTransactionOptions();

    String scalarNullPredicate();

    boolean shortCircuitRequiredNulls();

    void generateDatabaseArtifacts(List<Entities> entities, Path resourcePath);

    void validatePrimaryKey(Entities entity, EntityFields key);

    void validateManyToManyField(Entities entity, EntityFields field);
}
