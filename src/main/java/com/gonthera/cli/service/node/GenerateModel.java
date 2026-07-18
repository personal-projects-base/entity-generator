package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Entities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.fieldType;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.importsForEntityReferences;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateModel {

    public static void generateModels(List<Entities> entities, Path packagePath) {
        Path modelPath = packagePath.resolve("models");
        entities.forEach(entity -> {
            try {
                writeFile(modelPath.resolve(fileName(entity.getEntityName()).concat(".model.ts")), content(entity));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static String content(Entities entity) {
        StringBuilder fields = new StringBuilder();
        entity.getEntityFields().forEach(field -> {
            String optional = field.getMetadata() != null && field.getMetadata().isNullable() ? "?" : "";
            fields.append(String.format("  %s%s: %s;%n", field.getFieldName(), optional, fieldType(field)));
        });

        return loadWxsd("model")
                .replace("<<imports>>", importsForEntityReferences(entity))
                .replace("<<entityName>>", className(entity.getEntityName()))
                .replace("<<entityFields>>", fields.toString());
    }
}
