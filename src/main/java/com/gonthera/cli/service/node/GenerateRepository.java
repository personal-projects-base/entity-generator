package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Entities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.prismaDelegateName;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateRepository {

    public static void generateRepositories(List<Entities> entities, Path packagePath) {
        Path repositoryPath = packagePath.resolve("repositories");
        entities.forEach(entity -> {
            if (!entity.isOnlyDTO()) {
                try {
                    writeFile(repositoryPath.resolve(fileName(entity.getEntityName()).concat(".repository.ts")), content(entity));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static String content(Entities entity) {
        String entityName = className(entity.getEntityName());
        String delegate = prismaDelegateName(entity);

        return loadWxsd("repository")
                .replace("<<entityName>>", entityName)
                .replace("<<entityFileName>>", fileName(entity.getEntityName()))
                .replace("<<prismaDelegate>>", delegate);
    }
}
