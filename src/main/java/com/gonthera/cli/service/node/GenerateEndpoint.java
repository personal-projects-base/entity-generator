package com.gonthera.cli.service.node;

import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.Parameters;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.gonthera.cli.service.node.NodeCommon.className;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.parameterType;
import static com.gonthera.cli.service.node.NodeCommon.varName;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateEndpoint {

    public static void generateEndpoints(List<Endpoints> endpoints, Path packagePath) {
        Path endpointPath = packagePath.resolve("endpoints");
        endpoints.forEach(endpoint -> {
            try {
                writeFile(endpointPath.resolve(fileName(endpoint.getMethodName()).concat(".endpoint.ts")), endpointContent(endpoint));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    public static void generateCrudControllers(List<Entities> entities, Path packagePath) {
        Path controllerPath = packagePath.resolve("controllers");
        Path routerPath = packagePath.resolve("routes");
        entities.forEach(entity -> {
            if (!entity.isOnlyDTO() && entity.isGenerateDefaultHandlers()) {
                try {
                    writeFile(controllerPath.resolve(fileName(entity.getEntityName()).concat(".controller.ts")), controllerContent(entity));
                    writeFile(routerPath.resolve(fileName(entity.getEntityName()).concat(".routes.ts")), routeContent(entity));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        try {
            writeFile(routerPath.resolve("index.ts"), routesIndex(entities));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static String endpointContent(Endpoints endpoint) {
        String inputFields = parameters(endpoint.getMetadata().getInput());
        String outputFields = parameters(endpoint.getMetadata().getOutput());
        String method = endpoint.getHttpMethod() == null ? "post" : endpoint.getHttpMethod().toLowerCase();
        String name = className(endpoint.getMethodName());
        String imports = parameterImports(endpoint);

        return loadWxsd("endpoint")
                .replace("<<imports>>", imports)
                .replace("<<endpointName>>", name)
                .replace("<<inputFields>>", inputFields)
                .replace("<<outputFields>>", outputFields)
                .replace("<<httpMethod>>", method)
                .replace("<<methodName>>", endpoint.getMethodName());
    }

    private static String parameterImports(Endpoints endpoint) {
        return Stream.concat(endpoint.getMetadata().getInput().stream(), endpoint.getMetadata().getOutput().stream())
                .filter(parameter -> !NodeCommon.isPrimitive(parameter.getParameterType()))
                .map(parameter -> {
                    if ("requestdata".equals(parameter.getParameterType()) || "responsedata".equals(parameter.getParameterType())) {
                        return String.format("import type { %s } from '../common/contracts';", NodeCommon.typeName(parameter.getParameterType()));
                    }
                    if (NodeCommon.isEnum(parameter.getParameterType())) {
                        String name = NodeCommon.typeName(parameter.getParameterType());
                        return String.format("import type { %s } from '../enums/%s.enum';", name, fileName(name));
                    }
                    return String.format("import type { %sDTO } from '../models/%s.model';", className(parameter.getParameterType()), fileName(parameter.getParameterType()));
                })
                .distinct()
                .collect(Collectors.joining("\n"));
    }

    private static String parameters(List<Parameters> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }
        return parameters.stream()
                .map(parameter -> String.format("  %s: %s;%n", parameter.getParameterName(), parameterType(parameter)))
                .collect(Collectors.joining());
    }

    private static String controllerContent(Entities entity) {
        String name = className(entity.getEntityName());
        String variable = varName(entity.getEntityName());

        return loadWxsd("controller")
                .replace("<<entityName>>", name)
                .replace("<<entityFileName>>", fileName(entity.getEntityName()))
                .replace("<<abstract>>", entity.isControllerAbstract() ? "abstract " : "")
                .replace("<<entity>>", variable);
    }

    private static String routeContent(Entities entity) {
        String name = className(entity.getEntityName());
        String variable = varName(entity.getEntityName());
        String file = fileName(entity.getEntityName());

        String controllerCreation;
        if (entity.isControllerAbstract()) {
            controllerCreation = String.format(
                    "  if (!factory) throw new Error(\"%sController requires a concrete factory\");%n" +
                    "  const controller = factory(repository);",
                    name
            );
        } else {
            controllerCreation = String.format(
                    "  const controller = factory ? factory(repository) : new %sController(repository);",
                    name
            );
        }

        return loadWxsd("route")
                .replace("<<entityName>>", name)
                .replace("<<entityFileName>>", file)
                .replace("<<factoryOptional>>", entity.isControllerAbstract() ? "" : "?")
                .replace("<<controllerCreation>>", controllerCreation)
                .replace("<<entityRoute>>", variable);
    }

    private static String routesIndex(List<Entities> entities) {
        List<Entities> generatedEntities = entities.stream()
                .filter(entity -> !entity.isOnlyDTO() && entity.isGenerateDefaultHandlers())
                .collect(Collectors.toList());

        String imports = generatedEntities.stream()
                .map(entity -> String.format(
                        "import { create%sRoutes, type %sControllerFactory } from './%s.routes';",
                        className(entity.getEntityName()),
                        className(entity.getEntityName()),
                        fileName(entity.getEntityName())
                ))
                .collect(Collectors.joining("\n"));

        String factoryFields = generatedEntities.stream()
                .map(entity -> String.format(
                        "  %s%s: %sControllerFactory;",
                        varName(entity.getEntityName()),
                        entity.isControllerAbstract() ? "" : "?",
                        className(entity.getEntityName())
                ))
                .collect(Collectors.joining("\n"));

        String uses = generatedEntities.stream()
                .map(entity -> String.format(
                        "  router.use(create%sRoutes(prisma, factories.%s));",
                        className(entity.getEntityName()),
                        varName(entity.getEntityName())
                ))
                .collect(Collectors.joining("\n"));

        boolean requiresFactories = generatedEntities.stream().anyMatch(Entities::isControllerAbstract);

        return loadWxsd("routesindex")
                .replace("<<imports>>", imports)
                .replace("<<factoryFields>>", factoryFields)
                .replace("<<factoryDefault>>", requiresFactories ? "" : " = {}")
                .replace("<<routes>>", uses);
    }
}
