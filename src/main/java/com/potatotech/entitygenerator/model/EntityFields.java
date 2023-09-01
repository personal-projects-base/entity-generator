package com.potatotech.entitygenerator.model;

import lombok.Data;

@Data
public class EntityFields {

    private String comment;
    private String fieldName;
    private boolean list = false;
    private FieldProperties fieldProperties;
    private RelationsShips relationShips;
    private FieldMetadata metadata;
    private FrontEndProperties frontEndProperties;
}
