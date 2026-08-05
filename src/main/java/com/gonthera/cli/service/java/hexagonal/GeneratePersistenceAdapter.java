package com.gonthera.cli.service.java.hexagonal;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.service.common.FieldsMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterLowerCase;
import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;

public final class GeneratePersistenceAdapter {

    private GeneratePersistenceAdapter() {
    }

    public static void generatePersistenceAdapters(
            List<Entities> entities,
            String packageName,
            Path repositoriesPath,
            Path adaptersPath,
            Path configurationPath
    ) {
        String repositoryTemplate = loadWxsd("hexagonaljparepository");
        String adapterTemplate = loadWxsd("hexagonalpersistenceadapter");
        String configurationTemplate = loadWxsd("hexagonalapplicationconfiguration");

        entities.stream()
                .filter(entity -> !entity.isOnlyDTO())
                .forEach(entity -> {
                    write(
                            repositoriesPath,
                            entity,
                            "JpaRepository",
                            configure(repositoryTemplate, packageName, entity)
                                    .replace(
                                            "<<packageName>>",
                                            packageName.concat("_gen.infrastructure.adapters.out.persistence.repositories")
                                    )
                    );
                    write(
                            adaptersPath,
                            entity,
                            "PersistenceAdapter",
                            configure(adapterTemplate, packageName, entity)
                                    .replace(
                                            "<<packageName>>",
                                            packageName.concat("_gen.infrastructure.adapters.out.persistence")
                                    )
                    );
                    if (!entity.isServiceAbstract()) {
                        write(
                                configurationPath,
                                entity,
                                "ApplicationConfiguration",
                                configure(configurationTemplate, packageName, entity)
                                        .replace(
                                                "<<packageName>>",
                                                packageName.concat("_gen.infrastructure.configuration")
                                        )
                        );
                    }
                });
    }

    private static String configure(String template, String packageName, Entities entity) {
        EntityFields key = entity.getEntityFields().stream()
                .filter(field -> field.getMetadata().isKey())
                .findFirst()
                .orElseThrow();
        String entityName = firstCharacterUpperCase(entity.getEntityName());
        return template
                .replace("<<rootPackage>>", packageName.concat("_gen"))
                .replace("<<entityName>>", entityName)
                .replace("<<entityNameLower>>", firstCharacterLowerCase(entityName))
                .replace("<<keyType>>", FieldsMapper.getFieldTypeDomain(key.getFieldProperties().getFieldType()));
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
