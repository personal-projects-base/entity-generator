package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.model.FieldMetadata;
import com.gonthera.cli.model.FieldProperties;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.dotNet.GenerateDotNet;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DotNetOutputLayoutTest {

    @Test
    public void generatesDotNetSourcesInDedicatedDirectoriesWithoutHandlers() throws Exception {
        Path root = Files.createTempDirectory("gonthera-dotnet-layout-");
        Files.createDirectories(root.resolve("static"));
        Properties project = project(false);

        generate(root, project);

        Path generated = root.resolve("ExampleBackend_gen");
        assertFile(generated.resolve("Entities/CustomerEntity.cs"));
        assertFile(generated.resolve("Dtos/CustomerDTO.cs"));
        assertFile(generated.resolve("Converters/CustomerDTOConverter.cs"));
        assertFile(generated.resolve("Repositories/ICustomerRepository.cs"));
        assertFile(generated.resolve("Repositories/CustomerRepository.cs"));
        assertFile(generated.resolve("Controllers/CustomerController.cs"));
        assertFile(generated.resolve("Data/CustomDbContext.cs"));
        assertFile(generated.resolve("Common/RequestData.cs"));

        String controller = readFile(generated.resolve("Controllers/CustomerController.cs"));
        assertTrue(controller.contains("public class CustomerController"));
        assertFalse(controller.contains("Handler"));
        try (java.util.stream.Stream<Path> files = Files.walk(generated)) {
            assertFalse(files.anyMatch(path -> path.getFileName().toString().contains("Handler")));
        }
    }

    @Test
    public void generatesAbstractDotNetControllerWhenConfigured() throws Exception {
        Path root = Files.createTempDirectory("gonthera-dotnet-abstract-controller-");
        Files.createDirectories(root.resolve("static"));

        generate(root, project(true));

        String controller = readFile(root.resolve("ExampleBackend_gen/Controllers/CustomerController.cs"));
        assertTrue(controller.contains("public abstract class CustomerController"));
        assertTrue(controller.contains("public virtual ActionResult<CustomerDTO> Save"));
    }

    private void generate(Path root, Properties project) {
        String previousDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", root.toString());
            Common.properties = project;
            GenerateDotNet.generateSource(project);
        } finally {
            System.setProperty("user.dir", previousDirectory);
        }
    }

    private void assertFile(Path file) {
        assertTrue("Expected generated file " + file, Files.isRegularFile(file));
    }

    private String readFile(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private Properties project(boolean controllerAbstract) {
        FieldProperties fieldProperties = new FieldProperties();
        fieldProperties.setFieldType("uuid");
        FieldMetadata metadata = new FieldMetadata();
        metadata.setKey(true);

        EntityFields id = new EntityFields();
        id.setFieldName("id");
        id.setFieldProperties(fieldProperties);
        id.setMetadata(metadata);

        Entities customer = new Entities();
        customer.setEntityName("customer");
        customer.setTableName("customer");
        customer.setEntityFields(Collections.singletonList(id));
        customer.setGenerateDefaultControllers(true);
        customer.setControllerAbstract(controllerAbstract);

        Properties project = new Properties();
        project.setMainPackage("ExampleBackend");
        project.setProjectName("example-backend");
        project.setLanguage(Language.DOTNET);
        project.setEntities(Collections.singletonList(customer));
        project.setEndpoints(Collections.emptyList());
        project.setEnums(Collections.emptyList());
        return project;
    }
}
