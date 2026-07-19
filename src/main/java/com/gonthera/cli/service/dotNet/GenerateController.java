package com.gonthera.cli.service.dotNet;

import com.gonthera.cli.model.Entities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.common.Common.stringFormaterJava;
import static com.gonthera.cli.service.common.GenerateCommon.configureFileEntity;

public class GenerateController {

    protected static void generateControllers(List<Entities> entities, String packageName, Path packagePath) {
        String template = loadWxsd("controller");
        entities.forEach(entity -> {
            if (!entity.isGenerateDefaultControllers()) return;
            try {
                String fileName = stringFormaterJava(
                        firstCharacterUpperCase(entity.getEntityName()),
                        "Controller",
                        packagePath.toString()
                );
                String content = configureFileEntity(template, packageName, entity, entity.getEntityName(), "")
                        .replace("<<abstract>>", entity.isControllerAbstract() ? "abstract " : "");
                Files.write(
                        Path.of(fileName),
                        content.getBytes(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
                System.out.println("@GenerateData");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }
}
