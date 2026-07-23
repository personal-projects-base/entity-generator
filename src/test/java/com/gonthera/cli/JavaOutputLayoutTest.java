package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Authorization;
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
        assertPackage(generated.resolve("authorization/exception/ServiceException.java"), "com.example.service_gen.authorization.exception");
        assertPackage(generated.resolve("authorization/permission/PermissionType.java"), "com.example.service_gen.authorization.permission");
        assertPackage(generated.resolve("authorization/permission/Permissions.java"), "com.example.service_gen.authorization.permission");
        assertPackage(generated.resolve("authorization/security/Authenticate.java"), "com.example.service_gen.authorization.security");
        assertPackage(generated.resolve("authorization/security/Roles.java"), "com.example.service_gen.authorization.security");
        assertPackage(generated.resolve("authorization/security/UserSupplier.java"), "com.example.service_gen.authorization.security");
        assertPackage(generated.resolve("authorization/stereotype/Anonymous.java"), "com.example.service_gen.authorization.stereotype");
        assertPackage(generated.resolve("authorization/stereotype/SecureResource.java"), "com.example.service_gen.authorization.stereotype");
        assertPackage(generated.resolve("authorization/tenant/TenantConfiguration.java"), "com.example.service_gen.authorization.tenant");
        assertPackage(generated.resolve("authorization/tenant/TenantContext.java"), "com.example.service_gen.authorization.tenant");
        String dto = readFile(generated.resolve("dtos/CustomerDTO.java"));
        assertTrue(dto.matches("(?s).*public\\s+\\S+\\s+id;.*"));
        assertTrue(dto.contains("public Status status;"));
        String endpoint = readFile(generated.resolve("endpoints/FindCustomer.java"));
        assertTrue(endpoint.contains("import com.example.service_gen.authorization.stereotype.Anonymous;"));
        assertTrue(endpoint.contains("@Anonymous"));
        String specificationFilter = readFile(generated.resolve("common/SpecificationFilter.java"));
        assertTrue(specificationFilter.contains("import com.example.service_gen.authorization.exception.ServiceException;"));
        try (java.util.stream.Stream<Path> files = Files.walk(generated)) {
            assertTrue(files.filter(Files::isRegularFile).noneMatch(this::containsTemplatePlaceholder));
        }
        try (java.util.stream.Stream<Path> files = Files.walk(generated)) {
            assertTrue(files.filter(Files::isRegularFile).noneMatch(this::containsExternalAuthorizationImport));
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
    public void generatesCustomizableAuthorizationClassesAsAbstractWithoutSpringBeans() throws Exception {
        Path root = Files.createTempDirectory("gonthera-java-abstract-authorization-");
        Files.createDirectories(root.resolve("src/main/resources"));
        Properties project = project();
        Authorization authorization = new Authorization();
        authorization.setAuthenticateAbstract(true);
        authorization.setTenantConfigurationAbstract(true);
        project.setAuthorization(authorization);
        String previousDirectory = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", root.toString());
            Common.properties = project;
            GenerateJava.generateSource(project);
        } finally {
            System.setProperty("user.dir", previousDirectory);
        }

        String authenticate = readFile(root.resolve("src/main/java/com/example/service_gen/authorization/security/Authenticate.java"));
        assertTrue(authenticate.contains("public abstract class Authenticate"));
        assertTrue(!authenticate.contains("import org.springframework.stereotype.Service;"));
        assertTrue(authenticate.contains("protected String resolveSecret()"));
        assertTrue(authenticate.contains("protected String extractToken("));
        assertTrue(authenticate.contains("protected io.jsonwebtoken.Claims parseClaims("));

        String tenantConfiguration = readFile(root.resolve("src/main/java/com/example/service_gen/authorization/tenant/TenantConfiguration.java"));
        assertTrue(tenantConfiguration.contains("public abstract class TenantConfiguration"));
        assertTrue(!tenantConfiguration.contains("import org.springframework.stereotype.Component;"));
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

    private boolean containsExternalAuthorizationImport(Path file) {
        try {
            return readFile(file).contains("com.potatotech.authorization");
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
        metadata.setAnonymous(true);
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
