package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Enums;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateEnum {

    public static void generateEnums(List<Enums> enums, Path packagePath) {
        Path enumPath = packagePath.resolve("enums");
        enums.forEach(enumeration -> {
            try {
                writeFile(enumPath.resolve(fileName(enumeration.getEnumName()).concat(".enum.ts")), content(enumeration));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private static String content(Enums enumeration) {
        String values = enumeration.getValues().stream()
                .map(value -> String.format("  %s = '%s'", value, value))
                .collect(Collectors.joining(",\n"));

        return loadWxsd("enum")
                .replace("<<enumName>>", className(enumeration.getEnumName()))
                .replace("<<enumFields>>", values);
    }
}
