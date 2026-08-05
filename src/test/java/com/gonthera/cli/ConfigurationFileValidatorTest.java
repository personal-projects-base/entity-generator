package com.gonthera.cli;

import com.gonthera.cli.service.common.ConfigurationFileValidator;
import com.gonthera.cli.service.common.ProjectValidationException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationFileValidatorTest {

    @Test
    public void requiresGontheraDirectory() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-root-");

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains(".gonthera directory is required for validation"));
        }
    }

    @Test
    public void rejectsUnknownPropertiesAtAnyLevel() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-typo-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"entities\":[{\"entityNme\":\"customer\",\"entityFields\":[]}],\"endpoints\":[],\"enums\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("project.json.entities[0].entityNme is not a recognized property"));
        }
    }

    @Test
    public void rejectsInvalidJsonStructure() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-json-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        Files.write(directory.resolve("project.json"), "{\"language\":\"JAVA\"".getBytes(StandardCharsets.UTF_8));

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().get(0).contains("project.json contains invalid JSON"));
        }
    }

    @Test
    public void rejectsArraysAndObjectsInTheWrongSections() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-types-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"entities\":{},\"endpoints\":{},\"enums\":{},\"messaging\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("project.json.entities must be an array"));
            assertTrue(ex.getErrors().contains("project.json.endpoints must be an array"));
            assertTrue(ex.getErrors().contains("project.json.enums must be an array"));
            assertTrue(ex.getErrors().contains("project.json.messaging must be an object"));
        }
    }

    @Test
    public void rejectsWrongBooleanTypesForControllerConfiguration() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-controller-types-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"entities\":[{\"entityName\":\"customer\",\"controllerAbstract\":\"true\",\"entityFields\":[]}]," +
                "\"endpoints\":[],\"enums\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("project.json.entities[0].controllerAbstract must be a boolean"));
        }
    }

    @Test
    public void rejectsWrongBooleanTypesInAuthorizationFile() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-authorization-types-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"entities\":[],\"endpoints\":[],\"enums\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));
        Files.write(
                directory.resolve("authorization.json"),
                "{\"authenticateAbstract\":\"true\"}".getBytes(StandardCharsets.UTF_8)
        );

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("authorization.json.authenticateAbstract must be a boolean"));
        }
    }

    @Test
    public void acceptsKnownJavaArchitecture() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-architecture-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"architecture\":\"HEXAGONAL\",\"entities\":[],\"endpoints\":[],\"enums\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));

        ConfigurationFileValidator.validate(root);
    }

    @Test
    public void rejectsUnknownArchitecture() throws Exception {
        Path root = Files.createTempDirectory("gonthera-validator-invalid-architecture-");
        Path directory = Files.createDirectory(root.resolve(".gonthera"));
        String project = "{\"mainPackage\":\"com.example\",\"projectName\":\"example\",\"language\":\"JAVA\"," +
                "\"architecture\":\"hexagonal\",\"entities\":[],\"endpoints\":[],\"enums\":[]}";
        Files.write(directory.resolve("project.json"), project.getBytes(StandardCharsets.UTF_8));

        try {
            ConfigurationFileValidator.validate(root);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("project.json.architecture must be one of [MVC, HEXAGONAL]"));
        }
    }
}
