package com.gonthera.cli.service.common;

import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.model.Enums;
import com.gonthera.cli.model.MessagingChannel;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.model.RabbitMq;
import com.gonthera.cli.enuns.Language;

import java.util.ArrayList;
import java.util.List;

public final class ProjectValidator {

    private ProjectValidator() {
    }

    public static void validate(Properties project) {
        List<String> errors = new ArrayList<>();
        if (project == null) {
            errors.add("project configuration is empty");
            throw new ProjectValidationException(errors);
        }

        required(project.getMainPackage(), "mainPackage", errors);
        required(project.getProjectName(), "projectName", errors);
        if (project.getLanguage() == null) errors.add("language is required");

        validateEntities(project, errors);
        validateEndpoints(project, errors);
        validateEnums(project, errors);
        validateMessaging(project, errors);

        if (!errors.isEmpty()) throw new ProjectValidationException(errors);
    }

    public static List<String> warnings(Properties project) {
        List<String> warnings = new ArrayList<>();
        if (project == null || project.getEntities() == null) return warnings;
        for (int index = 0; index < project.getEntities().size(); index++) {
            Entities entity = project.getEntities().get(index);
            if (project.getLanguage() == Language.JAVA && entity != null && entity.isServiceAbstract()) {
                warnings.add(String.format(
                        "entities[%d].serviceAbstract=true: %sService will be abstract and will require a concrete Spring bean in the consumer project",
                        index,
                        entity.getEntityName() == null ? "Entity" : Common.firstCharacterUpperCase(entity.getEntityName())
                ));
            }
            if (entity != null && entity.getGenerateDefaultHandlers() != null) {
                warnings.add(String.format(
                        "entities[%d].generateDefaultHandlers is deprecated; use generateDefaultControllers",
                        index
                ));
                if (entity.getGenerateDefaultControllers() != null) {
                    warnings.add(String.format(
                            "entities[%d] declares both generateDefaultControllers and generateDefaultHandlers; generateDefaultControllers takes precedence",
                            index
                    ));
                }
            }
            if (entity != null && entity.getHandlerAbstract() != null) {
                warnings.add(String.format(
                        "entities[%d].handlerAbstract is deprecated; use controllerAbstract",
                        index
                ));
                if (entity.getControllerAbstract() != null) {
                    warnings.add(String.format(
                            "entities[%d] declares both controllerAbstract and handlerAbstract; controllerAbstract takes precedence",
                            index
                    ));
                }
            }
        }
        return warnings;
    }

    private static void validateEntities(Properties project, List<String> errors) {
        if (project.getEntities() == null) {
            errors.add("entities must be an array");
            return;
        }
        for (int index = 0; index < project.getEntities().size(); index++) {
            Entities entity = project.getEntities().get(index);
            String path = "entities[" + index + "]";
            if (entity == null) {
                errors.add(path + " must be an object");
                continue;
            }
            required(entity.getEntityName(), path + ".entityName", errors);
            if (entity.getEntityFields() == null || entity.getEntityFields().isEmpty()) {
                errors.add(path + ".entityFields must contain at least one field");
                continue;
            }
            int keys = 0;
            for (int fieldIndex = 0; fieldIndex < entity.getEntityFields().size(); fieldIndex++) {
                EntityFields field = entity.getEntityFields().get(fieldIndex);
                String fieldPath = path + ".entityFields[" + fieldIndex + "]";
                if (field == null) {
                    errors.add(fieldPath + " must be an object");
                    continue;
                }
                required(field.getFieldName(), fieldPath + ".fieldName", errors);
                if (field.getFieldProperties() == null) {
                    errors.add(fieldPath + ".fieldProperties is required");
                } else {
                    required(field.getFieldProperties().getFieldType(), fieldPath + ".fieldProperties.fieldType", errors);
                }
                if (field.getMetadata() == null) {
                    errors.add(fieldPath + ".metadata is required");
                } else if (field.getMetadata().isKey()) {
                    keys++;
                }
            }
            if (keys != 1) errors.add(path + " must declare exactly one field with metadata.key=true");
        }
    }

    private static void validateEndpoints(Properties project, List<String> errors) {
        if (project.getEndpoints() == null) {
            errors.add("endpoints must be an array");
            return;
        }
        for (int index = 0; index < project.getEndpoints().size(); index++) {
            Endpoints endpoint = project.getEndpoints().get(index);
            String path = "endpoints[" + index + "]";
            if (endpoint == null) {
                errors.add(path + " must be an object");
                continue;
            }
            required(endpoint.getMethodName(), path + ".methodName", errors);
            required(endpoint.getHttpMethod(), path + ".httpMethod", errors);
            if (endpoint.getHttpMethod() != null && !endpoint.getHttpMethod().equals("GET") && !endpoint.getHttpMethod().equals("POST")) {
                errors.add(path + ".httpMethod must be GET or POST");
            }
            if (endpoint.getMetadata() == null) {
                errors.add(path + ".metadata is required");
            } else {
                if (endpoint.getMetadata().getInput() == null) errors.add(path + ".metadata.input must be an array");
                if (endpoint.getMetadata().getOutput() == null) errors.add(path + ".metadata.output must be an array");
            }
        }
    }

    private static void validateEnums(Properties project, List<String> errors) {
        if (project.getEnums() == null) {
            errors.add("enums must be an array");
            return;
        }
        for (int index = 0; index < project.getEnums().size(); index++) {
            Enums item = project.getEnums().get(index);
            String path = "enums[" + index + "]";
            if (item == null) {
                errors.add(path + " must be an object");
                continue;
            }
            required(item.getEnumName(), path + ".enumName", errors);
            if (item.getValues() == null || item.getValues().isEmpty()) errors.add(path + ".values must contain at least one value");
        }
    }

    private static void validateMessaging(Properties project, List<String> errors) {
        if (project.getMessaging() == null || project.getMessaging().getRabbitMq() == null) return;
        RabbitMq rabbitMq = project.getMessaging().getRabbitMq();
        if (rabbitMq.getPub() == null) errors.add("messaging.RabbitMq.pub must be an array");
        else validateChannels(rabbitMq.getPub(), "messaging.RabbitMq.pub", true, errors);
        if (rabbitMq.getSub() == null) errors.add("messaging.RabbitMq.sub must be an array");
        else validateChannels(rabbitMq.getSub(), "messaging.RabbitMq.sub", false, errors);
    }

    private static void validateChannels(List<MessagingChannel> channels, String path, boolean publisher, List<String> errors) {
        for (int index = 0; index < channels.size(); index++) {
            MessagingChannel channel = channels.get(index);
            String channelPath = path + "[" + index + "]";
            if (channel == null) {
                errors.add(channelPath + " must be an object");
                continue;
            }
            if (blank(channel.getName()) && blank(channel.getClassName())) errors.add(channelPath + ".name or className is required");
            required(channel.getQueue(), channelPath + ".queue", errors);
            if (publisher) required(channel.getRoutingKey(), channelPath + ".routingKey", errors);
        }
    }

    private static void required(String value, String path, List<String> errors) {
        if (blank(value)) errors.add(path + " is required");
    }

    private static boolean blank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
