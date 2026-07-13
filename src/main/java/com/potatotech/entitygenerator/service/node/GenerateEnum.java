package com.potatotech.entitygenerator.service.node;

import com.potatotech.entitygenerator.model.Enums;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static com.potatotech.entitygenerator.service.node.NodeCommon.className;
import static com.potatotech.entitygenerator.service.node.NodeCommon.fileName;
import static com.potatotech.entitygenerator.service.node.NodeCommon.writeFile;
import static com.potatotech.entitygenerator.service.common.Common.loadWxsd;

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
