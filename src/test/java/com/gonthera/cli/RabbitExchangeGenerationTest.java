package com.gonthera.cli;

import com.gonthera.cli.model.MessagingChannel;
import com.gonthera.cli.model.RabbitMq;
import com.gonthera.cli.service.java.GenerateMessaging;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class RabbitExchangeGenerationTest {

    @Test
    public void generatesRabbitExchangeAsInheritedAnnotation() throws Exception {
        Path packagePath = Files.createTempDirectory("gonthera-rabbit-exchange-");
        MessagingChannel publisher = new MessagingChannel();
        publisher.setName("customer");

        RabbitMq rabbitMq = new RabbitMq();
        rabbitMq.setPub(Collections.singletonList(publisher));
        rabbitMq.setSub(Collections.emptyList());

        GenerateMessaging.generateMessaging(rabbitMq, "com.example.service", packagePath);

        Path annotationFile = packagePath.resolve("messaging/RabbitExchange.java");
        String annotation = new String(Files.readAllBytes(annotationFile), StandardCharsets.UTF_8);
        assertTrue(annotation.contains("import java.lang.annotation.Inherited;"));
        assertTrue(annotation.contains("@Inherited\n@Retention(RetentionPolicy.RUNTIME)"));
    }
}
