package com.gonthera.cli.service.java.hexagonal;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.generatedJavaImport;
import static com.gonthera.cli.service.common.Common.getTableName;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.properties;
import static com.gonthera.cli.service.common.Common.setComments;
import static com.gonthera.cli.service.common.Common.splitByUppercase;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;

public final class GeneratePersistenceModel {

    private GeneratePersistenceModel() {
    }

    public static void generatePersistenceModels(
            List<Entities> entities,
            String packageName,
            Path entitiesPath,
            Path mappersPath
    ) {
        String entityTemplate = loadWxsd("hexagonalpersistenceentity");
        String mapperTemplate = loadWxsd("hexagonalpersistencemapper");
        entities.stream()
                .filter(entity -> !entity.isOnlyDTO())
                .forEach(entity -> {
                    write(
                            entitiesPath,
                            entity,
                            "JpaEntity",
                            configureEntity(entityTemplate, packageName, entity)
                    );
                    write(
                            mappersPath,
                            entity,
                            "PersistenceMapper",
                            configureMapper(mapperTemplate, packageName, entity)
                    );
                });
    }

    private static String configureEntity(String template, String packageName, Entities entity) {
        return template
                .replace("<<packageName>>", packageName.concat("_gen.infrastructure.adapters.out.persistence.entities"))
                .replace("<<enumImports>>", generatedJavaImport(
                        packageName,
                        "enums",
                        properties.getEnums() != null && !properties.getEnums().isEmpty()
                ))
                .replace("<<tableName>>", Common.splitByUppercase(getTableName(entity)))
                .replace("<<entityName>>", firstCharacterUpperCase(entity.getEntityName()))
                .replace("<<entityFields>>", fields(entity));
    }

    private static String configureMapper(String template, String packageName, Entities entity) {
        return template
                .replace("<<packageName>>", packageName.concat("_gen.infrastructure.adapters.out.persistence.mappers"))
                .replace("<<rootPackage>>", packageName.concat("_gen"))
                .replace("<<entityName>>", firstCharacterUpperCase(entity.getEntityName()))
                .replace("<<toDomainArguments>>", arguments(entity, true))
                .replace("<<toEntityArguments>>", arguments(entity, false));
    }

    private static String fields(Entities entity) {
        return entity.getEntityFields().stream()
                .map(field -> setComments(field.getComment())
                        + metadata(field, entity)
                        + "\n    private "
                        + persistenceType(field)
                        + " "
                        + field.getFieldName()
                        + ";")
                .collect(Collectors.joining("\n    "));
    }

    private static String metadata(EntityFields field, Entities entity) {
        String metadata = "";
        if (field.getRelationShips() == null) {
            if (field.getMetadata().isKey()) {
                metadata += "\n    @Id";
                metadata += field.getFieldProperties().getFieldType().equals("uuid")
                        ? "\n    @GeneratedValue(strategy = GenerationType.UUID)"
                        : "\n    @GeneratedValue(strategy = GenerationType.IDENTITY)";
            }
            metadata += "\n    @Column("
                    + (field.getMetadata().isNullable() ? "" : "nullable = false, ")
                    + "name = \"" + splitByUppercase(field.getFieldName()) + "\")";
            return metadata;
        }

        if ("ManyToMany".equalsIgnoreCase(field.getRelationShips().getRelationShip())) {
            String tableName = splitByUppercase(entity.getEntityName())
                    + "_" + splitByUppercase(field.getFieldProperties().getFieldType());
            metadata += "\n    @JoinTable(name = \"" + tableName + "\", "
                    + "joinColumns = @JoinColumn(name = \"" + splitByUppercase(entity.getEntityName()) + "_id\"), "
                    + "inverseJoinColumns = @JoinColumn(name = \""
                    + splitByUppercase(field.getFieldProperties().getFieldType()) + "_id\"))";
        } else if (!field.getRelationShips().isBidirectional()) {
            metadata += "\n    @JoinColumn(name = \"" + splitByUppercase(field.getFieldName()) + "\")";
        }

        String relationship = field.getRelationShips().getRelationShip();
        if (field.getRelationShips().isBidirectional()) {
            String mappedBy = field.getRelationShips().getMappedBy();
            metadata += "\n    @" + relationship + "(mappedBy = \"" + mappedBy
                    + "\", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType."
                    + field.getRelationShips().getFetchType() + ")";
        } else {
            metadata += "\n    @" + relationship + "(fetch = FetchType."
                    + field.getRelationShips().getFetchType() + ")";
        }
        return metadata;
    }

    private static String persistenceType(EntityFields field) {
        String type = FieldsMapper.getFieldTypePersistence(field.getFieldProperties().getFieldType());
        return field.isList() ? "List<" + type + ">" : type;
    }

    private static String arguments(Entities entity, boolean toDomain) {
        return entity.getEntityFields().stream()
                .map(field -> conversion(field, toDomain))
                .collect(Collectors.joining(", "));
    }

    private static String conversion(EntityFields field, boolean toDomain) {
        String source = toDomain ? "entity" : "domain";
        String getter = source + ".get" + firstCharacterUpperCase(field.getFieldName()) + "()";
        if (!isEntityType(field)) {
            return getter;
        }

        String mapper = "new "
                + firstCharacterUpperCase(field.getFieldProperties().getFieldType())
                + "PersistenceMapper()";
        String operation = toDomain ? "toDomain" : "toEntity";
        if (field.isList()) {
            return getter + " == null ? null : " + getter
                    + ".stream().map(item -> " + mapper + "." + operation
                    + "(item)).collect(Collectors.toList())";
        }
        return mapper + "." + operation + "(" + getter + ")";
    }

    private static boolean isEntityType(EntityFields field) {
        String type = field.getFieldProperties().getFieldType();
        return properties.getEntities() != null
                && properties.getEntities().stream()
                .anyMatch(entity -> entity.getEntityName().equalsIgnoreCase(type));
    }

    private static void write(Path path, Entities entity, String suffix, String content) {
        try {
            String fileName = stringFormaterJava(entity.getEntityName(), suffix, path.toString());
            Files.write(Path.of(fileName), content.getBytes(), StandardOpenOption.CREATE);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not generate persistence " + suffix, ex);
        }
    }
}
