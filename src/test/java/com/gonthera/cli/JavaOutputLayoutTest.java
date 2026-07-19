package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Endpoints;
import com.gonthera.cli.model.Entities;
import com.gonthera.cli.model.EntityFields;
import com.gonthera.cli.model.Enums;
import com.gonthera.cli.model.FieldMetadata;
import com.gonthera.cli.model.FieldProperties;
import com.gonthera.cli.model.Metadata;
import com.gonthera.cli.model.Parameters;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.java.GenerateJava;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class JavaOutputLayoutTest {

    @Test
    public void generatesJavaSourcesInDedicatedPackages() throws Exception {
        Path root = Files.createTempDirectory("gonthera-java-layout-");
        Files.createDirectories(root.resolve("src/main/resources"));
        Properties project = project();
        String previousDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", root.toString());
            Common.properties = project;
            GenerateJava.generateSource(project);
        } finally {
            System.setProperty("user.dir", previousDirectory);
        }

        Path generated = root.resolve("src/main/java/com/example/service_gen");
        assertPackage(generated.resolve("entities/CustomerEntity.java"), "com.example.service_gen.entities");
        assertPackage(generated.resolve("dtos/CustomerDTO.java"), "com.example.service_gen.dtos");
        assertPackage(generated.resolve("converters/CustomerDTOConverter.java"), "com.example.service_gen.converters");
        assertPackage(generated.resolve("repositories/CustomerRepository.java"), "com.example.service_gen.repositories");
        assertPackage(generated.resolve("services/CustomerService.java"), "com.example.service_gen.services");
        assertPackage(generated.resolve("controllers/CustomerController.java"), "com.example.service_gen.controllers");
        assertPackage(generated.resolve("endpoints/FindCustomer.java"), "com.example.service_gen.endpoints");
        assertPackage(generated.resolve("endpoints/FindCustomerOutput.java"), "com.example.service_gen.endpoints");
        assertPackage(generated.resolve("enums/Status.java"), "com.example.service_gen.enums");
        assertPackage(generated.resolve("common/CrudController.java"), "com.example.service_gen.common");
        try (java.util.stream.Stream<Path> files = Files.walk(generated)) {
            assertTrue(files.filter(Files::isRegularFile).noneMatch(this::containsTemplatePlaceholder));
        }
    }

    @Test
    public void generatesAbstractServiceWithoutServiceAnnotation() throws Exception {
        Path root = Files.createTempDirectory("gonthera-java-abstract-service-");
        Files.createDirectories(root.resolve("src/main/resources"));
        Properties project = project();
        project.getEntities().get(0).setServiceAbstract(true);
        String previousDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", root.toString());
            Common.properties = project;
            GenerateJava.generateSource(project);
        } finally {
            System.setProperty("user.dir", previousDirectory);
        }

        String service = readFile(root.resolve("src/main/java/com/example/service_gen/services/CustomerService.java"));
        assertTrue(service.contains("public abstract class CustomerService"));
        assertTrue(!service.contains("\n@Service\n"));
    }

    @Test
    public void keepsLegacyHandlerConfigurationAsControllerAliases() {
        Entities entity = new Entities();
        entity.setGenerateDefaultHandlers(false);
        entity.setHandlerAbstract(true);

        assertTrue(!entity.isGenerateDefaultControllers());
        assertTrue(entity.isControllerAbstract());

        entity.setGenerateDefaultControllers(true);
        entity.setControllerAbstract(false);
        assertTrue(entity.isGenerateDefaultControllers());
        assertTrue(!entity.isControllerAbstract());
    }

    private void assertPackage(Path file, String packageName) throws Exception {
        assertTrue("Expected generated file " + file, Files.isRegularFile(file));
        assertTrue(readFile(file).contains("package " + packageName + ";"));
    }

    private String readFile(Path file) throws Exception {
        return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    }

    private boolean containsTemplatePlaceholder(Path file) {
        try {
            return readFile(file).contains("<<");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private Properties project() {
        FieldProperties idProperties = new FieldProperties();
        idProperties.setFieldType("uuid");
        FieldMetadata idMetadata = new FieldMetadata();
        idMetadata.setKey(true);
        EntityFields id = new EntityFields();
        id.setFieldName("id");
        id.setComment("Identifier");
        id.setFieldProperties(idProperties);
        id.setMetadata(idMetadata);

        FieldProperties statusProperties = new FieldProperties();
        statusProperties.setFieldType("Status");
        EntityFields status = new EntityFields();
        status.setFieldName("status");
        status.setComment("Status");
        status.setFieldProperties(statusProperties);
        status.setMetadata(new FieldMetadata());

        Entities customer = new Entities();
        customer.setEntityName("customer");
        customer.setEntityFields(Arrays.asList(id, status));

        Enums statusEnum = new Enums();
        statusEnum.setEnumName("Status");
        statusEnum.setValues(Arrays.asList("ACTIVE", "INACTIVE"));

        Parameters output = new Parameters();
        output.setParameterName("customer");
        output.setParameterType("customer");
        Metadata metadata = new Metadata();
        metadata.setInput(Collections.emptyList());
        metadata.setOutput(Collections.singletonList(output));
        Endpoints endpoint = new Endpoints();
        endpoint.setMethodName("findCustomer");
        endpoint.setHttpMethod("GET");
        endpoint.setMetadata(metadata);

        Properties project = new Properties();
        project.setMainPackage("com.example.service");
        project.setProjectName("service");
        project.setLanguage(Language.JAVA);
        project.setEntities(Collections.singletonList(customer));
        project.setEndpoints(Collections.singletonList(endpoint));
        project.setEnums(Collections.singletonList(statusEnum));
        return project;
    }
}
