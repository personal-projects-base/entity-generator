package com.potatotech.entitygenerator.model;

import com.potatotech.entitygenerator.enuns.Language;
import lombok.Data;

import java.util.List;

@Data
public class Properties {

    private String mainPackage;
    private Language language;
    private String projectName;
    private List<Entities> entities;
    private List<Endpoints> endpoints;
    private List<Enums> enums;

}
