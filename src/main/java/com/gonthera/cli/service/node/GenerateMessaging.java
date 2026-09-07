package com.gonthera.cli.service.node;

import com.gonthera.cli.model.MessagingChannel;
import com.gonthera.cli.model.RabbitMq;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.gonthera.cli.service.common.Common.firstCharacterUpperCase;
import static com.gonthera.cli.service.common.Common.loadWxsd;
import static com.gonthera.cli.service.node.NodeCommon.fileName;
import static com.gonthera.cli.service.node.NodeCommon.writeFile;

public class GenerateMessaging {

    public static void generateMessaging(RabbitMq rabbitMq, Path packagePath) {
        if (rabbitMq == null || !hasChannels(rabbitMq)) {
            return;
        }

        try {
            generateCommon(packagePath);
            generatePublishers(channels(rabbitMq.getPub()), packagePath);
            generateSubscribers(channels(rabbitMq.getSub()), packagePath);
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

    private static void generateCommon(Path packagePath) throws IOException {
        Path messagingPath = packagePath.resolve("messaging").resolve("rabbitmq");
        writeFile(messagingPath.resolve("rabbit-config.ts"), loadWxsd("rabbitconfig"));
    }

    private static void generatePublishers(List<MessagingChannel> publishers, Path packagePath) throws IOException {
        if (publishers.isEmpty()) {
            return;
        }
        Path messagingPath = packagePath.resolve("messaging").resolve("rabbitmq");
        writeFile(messagingPath.resolve("rabbit-publisher.ts"), loadWxsd("rabbitpublisher"));
        Path publisherPath = packagePath.resolve("messaging").resolve("rabbitmq").resolve("pub");
        for (MessagingChannel publisher : publishers) {
            String className = className(publisher, "Pub");
            writeFile(publisherPath.resolve(fileName(removeSuffix(className, "Pub")).concat(".pub.ts")), publisherContent(className, publisher));
        }
    }

    private static void generateSubscribers(List<MessagingChannel> subscribers, Path packagePath) throws IOException {
        Path subscriberPath = packagePath.resolve("messaging").resolve("rabbitmq").resolve("sub");
        for (MessagingChannel subscriber : subscribers) {
            String className = className(subscriber, "Sub");
            writeFile(subscriberPath.resolve(fileName(removeSuffix(className, "Sub")).concat(".sub.ts")), subscriberContent(className, subscriber));
        }
    }

    private static String publisherContent(String className, MessagingChannel publisher) {
        return loadWxsd("rabbitpub")
                .replace("<<className>>", className)
                .replace("<<queue>>", value(publisher.getQueue()))
                .replace("<<routingKey>>", value(publisher.getRoutingKey()));
    }

    private static String subscriberContent(String className, MessagingChannel subscriber) {
        String binding = value(subscriber.getRoutingKey()).isEmpty()
                ? ""
                : String.format("%n        await channel.bindQueue(this.queue, this.config.exchange, '%s');", value(subscriber.getRoutingKey()));

        return loadWxsd("rabbitsub")
                .replace("<<className>>", className)
                .replace("<<queue>>", value(subscriber.getQueue()))
                .replace("<<queueBinding>>", binding);
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
}
