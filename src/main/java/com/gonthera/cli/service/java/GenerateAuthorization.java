package com.gonthera.cli.service.java;

import com.gonthera.cli.model.Authorization;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateAuthorization {

    private static final String[][] TEMPLATES = {
            {"exception", "serviceexception", "ServiceException"},
            {"permission", "permissiontype", "PermissionType"},
            {"permission", "permissions", "Permissions"},
            {"security", "roles", "Roles"},
            {"security", "usersupplier", "UserSupplier"},
            {"security", "authenticate", "Authenticate"},
            {"stereotype", "anonymous", "Anonymous"},
            {"stereotype", "secureresource", "SecureResource"},
            {"tenant", "tenantconfiguration", "TenantConfiguration"},
            {"tenant", "tenantcontext", "TenantContext"}
    };

    public static void generateAuthorization(String packageName, Path packagePath, Authorization authorization) {
        for (String[] template : TEMPLATES) {
            generateFile(packageName, packagePath, template[0], template[1], template[2], authorization);
        }
    }

    private static void generateFile(
            String packageName,
            Path packagePath,
            String subpackage,
            String templateName,
            String className,
            Authorization authorization
    ) {
        try {
            Path targetDirectory = packagePath.resolve("authorization").resolve(subpackage);
            Files.createDirectories(targetDirectory);
            String content = loadWxsd(templateName)
                    .replace("<<packageName>>", packageName.concat("_gen.authorization"));
            content = configureCustomization(content, className, authorization);
            Files.write(
                    targetDirectory.resolve(className.concat(".java")),
                    content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ex) {
            throw new RuntimeException("Unable to generate authorization class " + className, ex);
        }
    }

    private static String configureCustomization(String content, String className, Authorization authorization) {
        boolean authenticateAbstract = authorization != null && authorization.isAuthenticateAbstract();
        boolean tenantConfigurationAbstract = authorization != null && authorization.isTenantConfigurationAbstract();
        if ("Authenticate".equals(className)) {
            return content
                    .replace("<<springImport>>", authenticateAbstract ? "" : "import org.springframework.stereotype.Service;")
                    .replace("<<springAnnotation>>", authenticateAbstract ? "" : "@Service")
                    .replace("<<abstractModifier>>", authenticateAbstract ? "abstract " : "");
        }
        if ("TenantConfiguration".equals(className)) {
            return content
                    .replace("<<springImport>>", tenantConfigurationAbstract ? "" : "import org.springframework.stereotype.Component;")
                    .replace("<<springAnnotation>>", tenantConfigurationAbstract ? "" : "@Component")
                    .replace("<<abstractModifier>>", tenantConfigurationAbstract ? "abstract " : "");
        }
        return content;
    }
}
