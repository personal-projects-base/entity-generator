package com.gonthera.cli.service.node;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

import static com.gonthera.cli.service.common.Common.loadWxsd;

public final class GenerateDatabaseConfiguration {
    private GenerateDatabaseConfiguration() {
    }

    public static void generate(Path packagePath) {
        try {
            NodeCommon.writeFile(packagePath.resolve("configuration/database/database.config.ts"),
                    loadWxsd("databaseconfig"));
        } catch (IOException ex) {
            throw new UncheckedIOException("Unable to generate Node database configuration", ex);
        }
    }
}
