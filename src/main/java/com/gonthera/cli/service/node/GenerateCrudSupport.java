package com.gonthera.cli.service.node;

import com.google.gson.Gson;
import com.gonthera.cli.model.Properties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public final class GenerateCrudSupport {
    private GenerateCrudSupport() {}

    public static void generate(Properties project, Path packagePath) {
        try {
            NodeCommon.writeFile(packagePath.resolve("common/contracts.ts"), loadWxsd("crudcontracts"));
            NodeCommon.writeFile(packagePath.resolve("common/entity-metadata.ts"),
                    loadWxsd("crudmetadata").replace("<<entities>>", new Gson().toJson(project.getEntities()))
                            .replace("<<enums>>", new Gson().toJson(project.getEnums())));
            NodeCommon.writeFile(packagePath.resolve("common/query.ts"), loadWxsd("crudquery"));
            NodeCommon.writeFile(packagePath.resolve("converters/entity-converter.ts"), loadWxsd("entityconverter"));
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to generate Node CRUD support", ex);
        }
    }
}
