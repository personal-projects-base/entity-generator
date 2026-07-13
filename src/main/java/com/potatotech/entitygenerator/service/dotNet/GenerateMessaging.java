package com.potatotech.entitygenerator.service.dotNet;

import com.potatotech.entitygenerator.model.MessagingChannel;
import com.potatotech.entitygenerator.model.RabbitMq;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static com.potatotech.entitygenerator.service.common.Common.firstCharacterUpperCase;
import static com.potatotech.entitygenerator.service.common.Common.loadWxsd;

public class GenerateMessaging {

    public static void generateMessaging(RabbitMq rabbitMq, String packageName, Path packagePath) {
        if (rabbitMq == null || !hasChannels(rabbitMq)) {
            return;
        }

        try {
            generateCommonMessaging(channels(rabbitMq.getPub()), packageName, packagePath);
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

    private static void generateCommonMessaging(List<MessagingChannel> publishers, String packageName, Path packagePath) throws IOException {
        Path messagingPath = packagePath.resolve("Messaging");
        Files.createDirectories(messagingPath);

        writeFile(
                messagingPath.resolve("RabbitExchangeAttribute.cs"),
                loadWxsd("rabbitexchange")
                        .replace("<<projetcName>>", packageName)
                        .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
        );

        writeFile(
                messagingPath.resolve("RabbitConfig.cs"),
                loadWxsd("rabbitconfig")
                        .replace("<<projetcName>>", packageName)
                        .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
        );

        writeFile(
                messagingPath.resolve("AddRabbitMessaging.cs"),
                loadWxsd("rabbitmessagingscoped")
                        .replace("<<projetcName>>", packageName)
                        .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
                        .replace("<<publisherUsing>>", publisherUsing(publishers, packageName))
                        .replace("<<publisherScoped>>", publisherScoped(publishers))
        );
    }

    private static void generatePublishers(List<MessagingChannel> publishers, String packageName, Path packagePath) throws IOException {
        if (publishers.isEmpty()) {
            return;
        }

        Path publisherPath = packagePath.resolve("Messaging").resolve("Pub");
        Files.createDirectories(publisherPath);

        writeFile(
                publisherPath.resolve("RabbitPublisher.cs"),
                loadWxsd("rabbitpublisher")
                        .replace("<<projetcName>>", packageName)
                        .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
        );

        String model = loadWxsd("rabbitpub");
        for (MessagingChannel publisher : publishers) {
            String className = className(publisher, "Pub");
            String fileContent = model
                    .replace("<<projetcName>>", packageName)
                    .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
                    .replace("<<className>>", className)
                    .replace("<<queue>>", value(publisher.getQueue()))
                    .replace("<<routingKey>>", value(publisher.getRoutingKey()));

            writeFile(publisherPath.resolve(className.concat(".cs")), fileContent);
        }
    }

    private static void generateSubscribers(List<MessagingChannel> subscribers, String packageName, Path packagePath) throws IOException {
        if (subscribers.isEmpty()) {
            return;
        }

        Path subscriberPath = packagePath.resolve("Messaging").resolve("Sub");
        Files.createDirectories(subscriberPath);

        String model = loadWxsd("rabbitsub");
        for (MessagingChannel subscriber : subscribers) {
            String className = className(subscriber, "Sub");
            String fileContent = model
                    .replace("<<projetcName>>", packageName)
                    .replace("<<nameSpaceName>>", packageName.concat("_Gen"))
                    .replace("<<className>>", className)
                    .replace("<<queue>>", value(subscriber.getQueue()))
                    .replace("<<queueBinding>>", queueBinding(subscriber));

            writeFile(subscriberPath.resolve(className.concat(".cs")), fileContent);
        }
    }

    private static String publisherScoped(List<MessagingChannel> publishers) {
        AtomicReference<String> scoped = new AtomicReference<>("");
        publishers.forEach(publisher -> {
            String className = className(publisher, "Pub");
            scoped.set(scoped.get().concat(String.format("            builder.Services.AddScoped<%s>();%n", className)));
        });
        return scoped.get().stripTrailing();
    }

    private static String publisherUsing(List<MessagingChannel> publishers, String packageName) {
        if (publishers.isEmpty()) {
            return "";
        }

        return String.format("using %s.%s.Messaging.Pub;%n", packageName, packageName.concat("_Gen"));
    }

    private static String queueBinding(MessagingChannel subscriber) {
        if (value(subscriber.getRoutingKey()).isEmpty()) {
            return "";
        }

        return String.format(
                "\n            _channel.QueueBind(queue: QueueName, exchange: _rabbitConfig.ExchangeName, routingKey: \"%s\");",
                value(subscriber.getRoutingKey())
        );
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

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
