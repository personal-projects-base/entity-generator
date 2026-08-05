package com.gonthera.cli.model;

import com.gonthera.cli.enuns.Architecture;
import com.gonthera.cli.enuns.Language;
import lombok.Data;

import java.util.List;

@Data
public class Properties {

    private String mainPackage;
    private Language language;
    private Architecture architecture;
    private String projectName;
    private List<Entities> entities;
    private List<Endpoints> endpoints;
    private List<Enums> enums;
    private Messaging messaging;
    private Authorization authorization;

    public Architecture getArchitecture() {
        return architecture == null ? Architecture.MVC : architecture;
    }

}
