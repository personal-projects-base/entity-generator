package com.gonthera.cli;

import com.gonthera.cli.enuns.Language;
import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.ProjectValidationException;
import com.gonthera.cli.service.common.ProjectValidator;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;
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
}
