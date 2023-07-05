package com.potatotech.entitygenerator.model;

import lombok.Data;

import java.util.List;

@Data
public class EntityFields {

    private String comment;
    private String fieldName;
    private FieldProperties fieldProperties;
    private List<RelationsShips> relationships;
}
