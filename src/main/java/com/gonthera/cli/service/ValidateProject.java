package com.gonthera.cli.service;

import com.gonthera.cli.model.Properties;
import com.gonthera.cli.service.common.Common;
import com.gonthera.cli.service.common.ConfigurationFileValidator;
import com.gonthera.cli.service.common.ProjectValidationException;
import com.gonthera.cli.service.common.ProjectValidator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.nio.file.Paths;

@Mojo(name = "validate", defaultPhase = LifecyclePhase.NONE)
public class ValidateProject extends AbstractMojo {

    @Override
    public void execute() throws MojoFailureException {
        try {
            ConfigurationFileValidator.validate(Paths.get(System.getProperty("user.dir")));
            Properties project = Common.loadProperties();
            ProjectValidator.validate(project);
            getLog().info("Gonthera project is valid");
        } catch (ProjectValidationException ex) {
            throw new MojoFailureException(ex.getMessage());
        } catch (RuntimeException ex) {
            throw new MojoFailureException("Unable to validate Gonthera project: " + ex.getMessage(), ex);
        }
    }
}
