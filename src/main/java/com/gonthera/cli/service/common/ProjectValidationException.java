package com.gonthera.cli.service.common;

import java.util.List;

public class ProjectValidationException extends RuntimeException {

    private final List<String> errors;

    public ProjectValidationException(List<String> errors) {
        super("Invalid project configuration:\n - " + String.join("\n - ", errors));
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
