package com.gonthera.cli.service.java;

import com.gonthera.cli.model.MessagingChannel;
import com.gonthera.cli.model.RabbitMq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterLowerCase;
import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;

public class GenerateMessaging {

    public static void generateMessaging(RabbitMq rabbitMq, String packageName, Path packagePath) {
        if (rabbitMq == null || !hasChannels(rabbitMq)) {
            return;
        }

        try {
            generateCommonMessaging(packageName, packagePath);
            generatePublishers(channels(rabbitMq.getPub()), packageName, packagePath);
            generateSubscribers(channels(rabbitMq.getSub()), packageName, packagePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static boolean hasChannels(RabbitMq rabbitMq) {
        return !channels(rabbitMq.getPub()).isEmpty() || !channels(rabbitMq.getSub()).isEmpty();
    }

    private static List<MessagingChannel> channels(List<MessagingChannel> channels) {
        return channels == null ? Collections.emptyList() : channels;
    }

    private static void generateCommonMessaging(String packageName, Path packagePath) throws IOException {
        Path messagingPath = packagePath.resolve("messaging");
        Files.createDirectories(messagingPath);

        writeFile(
                messagingPath.resolve("RabbitExchange.java"),
                loadWxsd("rabbitexchange")
                        .replace("<<packageName>>", packageName.concat("_gen"))
        );

        writeFile(
                messagingPath.resolve("RabbitConfig.java"),
                loadWxsd("rabbitconfig")
                        .replace("<<packageName>>", packageName.concat("_gen"))
        );
    }

    private static void generatePublishers(List<MessagingChannel> publishers, String packageName, Path packagePath) throws IOException {
        if (publishers.isEmpty()) {
            return;
        }

        Path publisherPath = packagePath.resolve("messaging").resolve("pub");
        Files.createDirectories(publisherPath);

        writeFile(
                publisherPath.resolve("RabbitPublisher.java"),
                loadWxsd("rabbitpublisher")
                        .replace("<<packageName>>", packageName.concat("_gen"))
        );

        String model = loadWxsd("rabbitpub");
        for (MessagingChannel publisher : publishers) {
            String className = className(publisher, "Pub");
            String beanName = firstCharacterLowerCase(removeSuffix(className, "Pub"));
            String fileContent = model
                    .replace("<<packageName>>", packageName.concat("_gen"))
                    .replace("<<className>>", className)
                    .replace("<<beanName>>", beanName)
                    .replace("<<queue>>", value(publisher.getQueue()))
                    .replace("<<routingKey>>", value(publisher.getRoutingKey()));

            writeFile(publisherPath.resolve(className.concat(".java")), fileContent);
        }
    }

    private static void generateSubscribers(List<MessagingChannel> subscribers, String packageName, Path packagePath) throws IOException {
        if (subscribers.isEmpty()) {
            return;
        }

        Path subscriberPath = packagePath.resolve("messaging").resolve("sub");
        Files.createDirectories(subscriberPath);

        String model = loadWxsd("rabbitsub");
        for (MessagingChannel subscriber : subscribers) {
            String className = className(subscriber, "Sub");
            String methodName = removeSuffix(className, "Sub");
            String fileContent = model
                    .replace("<<packageName>>", packageName.concat("_gen"))
                    .replace("<<className>>", className)
                    .replace("<<methodName>>", methodName)
                    .replace("<<queue>>", value(subscriber.getQueue()));

            writeFile(subscriberPath.resolve(className.concat(".java")), fileContent);
        }
    }

    private static String className(MessagingChannel channel, String suffix) {
        String configuredClassName = value(channel.getClassName());
        if (!configuredClassName.isEmpty()) {
            return ensureSuffix(firstCharacterUpperCase(configuredClassName), suffix);
        }

        String name = value(channel.getName());
        return ensureSuffix(firstCharacterUpperCase(name.isEmpty() ? "Messaging" : name), suffix);
    }

    private static String ensureSuffix(String className, String suffix) {
        return className.endsWith(suffix) ? className : className.concat(suffix);
    }

    private static String removeSuffix(String className, String suffix) {
        if (!className.endsWith(suffix)) {
            return className;
        }
        return className.substring(0, className.length() - suffix.length());
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
