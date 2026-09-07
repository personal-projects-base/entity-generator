package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Entities;
import com.gonthera.cli.service.node.database.NodeDatabaseDialect;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.prismaDelegateName;
import static com.gonthera.cli.service.node.NodeCommon.primaryKey;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateRepository {

    public static void generateRepositories(List<Entities> entities, Path packagePath, NodeDatabaseDialect dialect) {
        Path repositoryPath = packagePath.resolve("repositories");
        entities.forEach(entity -> {
            if (!entity.isOnlyDTO()) {
                try {
                    writeFile(repositoryPath.resolve(fileName(entity.getEntityName()).concat(".repository.ts")), content(entity, dialect));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static String content(Entities entity, NodeDatabaseDialect dialect) {
        String entityName = className(entity.getEntityName());
        String delegate = prismaDelegateName(entity);

        return loadWxsd("repository")
                .replace("<<entityName>>", entityName)
                .replace("<<entityConfigName>>", entity.getEntityName())
                .replace("<<entityFileName>>", fileName(entity.getEntityName()))
                .replace("<<keyField>>", primaryKey(entity).getFieldName())
                .replace("<<prismaDelegate>>", delegate)
                .replace("<<transactionOptions>>", dialect.repositoryTransactionOptions());
    }
}
