package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CommonProjectFileTest {

    @Test
    public void loadsProjectJsonBeforeLegacyPropertiesJson() throws Exception {
        Path directory = Files.createTempDirectory("gonthera-cli-project-");
        writeProject(directory.resolve("project.json"), "project", "JAVA");
        writeProject(directory.resolve("properties.json"), "legacy", "DOTNET");

        Properties properties = loadFrom(directory);

        assertEquals("project", properties.getProjectName());
        assertEquals(Language.JAVA, properties.getLanguage());
    }

    @Test
    public void fallsBackToLegacyPropertiesJson() throws Exception {
        Path directory = Files.createTempDirectory("gonthera-cli-legacy-");
        writeProject(directory.resolve("properties.json"), "legacy", "NODE");

        Properties properties = loadFrom(directory);

        assertEquals("legacy", properties.getProjectName());
        assertEquals(Language.NODE, properties.getLanguage());
    }

    @Test
    public void loadsAndUnifiesSeparatedGontheraConfigurationBeforeRootFiles() throws Exception {
        Path directory = Files.createTempDirectory("gonthera-cli-separated-");
        writeProject(directory.resolve("project.json"), "root", "DOTNET");

        Path gontheraDirectory = Files.createDirectory(directory.resolve(".gonthera"));
        Files.write(
                gontheraDirectory.resolve("project.json"),
                "{\"mainPackage\":\"com.example\",\"projectName\":\"separated\",\"language\":\"JAVA\"}".getBytes(StandardCharsets.UTF_8)
        );
        Files.write(gontheraDirectory.resolve("entities.json"), "[{\"entityName\":\"customer\"}]".getBytes(StandardCharsets.UTF_8));
        Files.write(gontheraDirectory.resolve("endpoints.json"), "[{\"methodName\":\"findCustomer\"}]".getBytes(StandardCharsets.UTF_8));
        Files.write(gontheraDirectory.resolve("enums.json"), "[{\"enumName\":\"Status\",\"values\":[\"ACTIVE\"]}]".getBytes(StandardCharsets.UTF_8));
        Files.write(gontheraDirectory.resolve("messaging.json"), "{\"RabbitMq\":{\"pub\":[],\"sub\":[]}}".getBytes(StandardCharsets.UTF_8));

        Properties properties = loadFrom(directory);

        assertEquals("separated", properties.getProjectName());
        assertEquals(Language.JAVA, properties.getLanguage());
        assertEquals("customer", properties.getEntities().get(0).getEntityName());
        assertEquals("findCustomer", properties.getEndpoints().get(0).getMethodName());
        assertEquals("Status", properties.getEnums().get(0).getEnumName());
        assertNotNull(properties.getMessaging().getRabbitMq());
    }

    @Test
    public void usesEmptyCollectionsWhenSeparatedFilesAreAbsent() throws Exception {
        Path directory = Files.createTempDirectory("gonthera-cli-header-only-");
        Path gontheraDirectory = Files.createDirectory(directory.resolve(".gonthera"));
        Files.write(
                gontheraDirectory.resolve("project.json"),
                "{\"mainPackage\":\"com.example\",\"projectName\":\"header\",\"language\":\"JAVA\"}".getBytes(StandardCharsets.UTF_8)
        );

        Properties properties = loadFrom(directory);

        assertEquals(0, properties.getEntities().size());
        assertEquals(0, properties.getEndpoints().size());
        assertEquals(0, properties.getEnums().size());
    }

    @Test
    public void keepsSectionsDeclaredInsideGontheraProjectWhenSeparatedFilesAreAbsent() throws Exception {
        Path directory = Files.createTempDirectory("gonthera-cli-single-file-");
        Path gontheraDirectory = Files.createDirectory(directory.resolve(".gonthera"));
        Files.write(
                gontheraDirectory.resolve("project.json"),
                ("{\"mainPackage\":\"com.example\",\"projectName\":\"complete\",\"language\":\"JAVA\"," +
                        "\"entities\":[{\"entityName\":\"customer\"}]," +
                        "\"endpoints\":[{\"methodName\":\"findCustomer\"}]," +
                        "\"enums\":[{\"enumName\":\"Status\",\"values\":[\"ACTIVE\"]}]," +
                        "\"messaging\":{\"RabbitMq\":{\"pub\":[],\"sub\":[]}}}").getBytes(StandardCharsets.UTF_8)
        );

        Properties properties = loadFrom(directory);

        assertEquals("customer", properties.getEntities().get(0).getEntityName());
        assertEquals("findCustomer", properties.getEndpoints().get(0).getMethodName());
        assertEquals("Status", properties.getEnums().get(0).getEnumName());
        assertNotNull(properties.getMessaging().getRabbitMq());
    }

    private Properties loadFrom(Path directory) {
        String previousDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", directory.toString());
            return Common.loadProperties();
        } finally {
            System.setProperty("user.dir", previousDirectory);
        }
    }

    private void writeProject(Path file, String projectName, String language) throws Exception {
        String json = String.format(
                "{\"mainPackage\":\"com.example\",\"projectName\":\"%s\",\"language\":\"%s\",\"entities\":[],\"endpoints\":[],\"enums\":[]}",
                projectName,
                language
        );
        Files.write(file, json.getBytes(StandardCharsets.UTF_8));
    }
}
