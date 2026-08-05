package com.gonthera.cli;

import com.gonthera.cli.enuns.Architecture;
import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Authorization;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.ProjectValidationException;
import com.gonthera.cli.service.common.ProjectValidator;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProjectValidatorTest {

    @Test
    public void acceptsMinimumValidProject() {
        Properties project = new Properties();
        project.setMainPackage("com.example.service");
        project.setProjectName("service-name");
        project.setLanguage(Language.JAVA);
        project.setEntities(new ArrayList<>());
        project.setEndpoints(new ArrayList<>());
        project.setEnums(new ArrayList<>());

        ProjectValidator.validate(project);
        assertEquals(Architecture.MVC, project.getArchitecture());
    }

    @Test
    public void rejectsHexagonalArchitectureForNonJavaTargets() {
        Properties project = new Properties();
        project.setMainPackage("Example");
        project.setProjectName("service-name");
        project.setLanguage(Language.DOTNET);
        project.setArchitecture(Architecture.HEXAGONAL);
        project.setEntities(new ArrayList<>());
        project.setEndpoints(new ArrayList<>());
        project.setEnums(new ArrayList<>());

        try {
            ProjectValidator.validate(project);
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("architecture HEXAGONAL is currently supported only for language JAVA"));
        }
    }

    @Test
    public void reportsAllMissingHeaderAndCollectionValues() {
        try {
            ProjectValidator.validate(new Properties());
            fail("Expected validation to fail");
        } catch (ProjectValidationException ex) {
            assertTrue(ex.getErrors().contains("mainPackage is required"));
            assertTrue(ex.getErrors().contains("projectName is required"));
            assertTrue(ex.getErrors().contains("language is required"));
            assertTrue(ex.getErrors().contains("entities must be an array"));
            assertTrue(ex.getErrors().contains("endpoints must be an array"));
            assertTrue(ex.getErrors().contains("enums must be an array"));
        }
    }

    @Test
    public void warnsWhenAbstractServiceRequiresConsumerBean() {
        Properties project = new Properties();
        project.setLanguage(Language.JAVA);
        com.gonthera.cli.model.Entities entity = new com.gonthera.cli.model.Entities();
        entity.setEntityName("customer");
        entity.setServiceAbstract(true);
        project.setEntities(java.util.Collections.singletonList(entity));

        assertEquals(1, ProjectValidator.warnings(project).size());
        assertTrue(ProjectValidator.warnings(project).get(0).contains("CustomerService"));
    }

    @Test
    public void warnsAboutLegacyHandlerPropertiesAndNewNamePrecedence() {
        Properties project = new Properties();
        project.setLanguage(Language.JAVA);
        com.gonthera.cli.model.Entities entity = new com.gonthera.cli.model.Entities();
        entity.setGenerateDefaultHandlers(false);
        entity.setGenerateDefaultControllers(true);
        entity.setHandlerAbstract(true);
        entity.setControllerAbstract(false);
        project.setEntities(java.util.Collections.singletonList(entity));

        java.util.List<String> warnings = ProjectValidator.warnings(project);
        assertTrue(warnings.stream().anyMatch(item -> item.contains("generateDefaultHandlers is deprecated")));
        assertTrue(warnings.stream().anyMatch(item -> item.contains("generateDefaultControllers takes precedence")));
        assertTrue(warnings.stream().anyMatch(item -> item.contains("handlerAbstract is deprecated")));
        assertTrue(warnings.stream().anyMatch(item -> item.contains("controllerAbstract takes precedence")));
    }

    @Test
    public void warnsWhenAbstractAuthorizationClassesRequireConsumerBeans() {
        Properties project = new Properties();
        project.setLanguage(Language.JAVA);
        project.setEntities(new ArrayList<>());
        Authorization authorization = new Authorization();
        authorization.setAuthenticateAbstract(true);
        authorization.setTenantConfigurationAbstract(true);
        project.setAuthorization(authorization);

        java.util.List<String> warnings = ProjectValidator.warnings(project);
        assertTrue(warnings.stream().anyMatch(item -> item.contains("Authenticate")));
        assertTrue(warnings.stream().anyMatch(item -> item.contains("TenantConfiguration")));
    }
}
