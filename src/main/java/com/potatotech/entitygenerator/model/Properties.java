package com.potatotech.entitygenerator.model;

import lombok.Data;

import java.util.List;

@Data
public class Properties {


    private String projectName;
    private List<Entities> entities;
    private List<Endpoints> endpoints;
    private List<Enums> enums;


}
