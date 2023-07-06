package com.potatotech.entitygenerator.model;

import lombok.Data;

@Data
public class FieldProperties {

    private String fieldType;
    private boolean required;
    private String valueDefault;
    private boolean key = false;
}
