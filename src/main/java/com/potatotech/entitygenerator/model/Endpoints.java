package com.potatotech.entitygenerator.model;

import lombok.Data;

@Data
public class Endpoints {

    private String methodName;
    private String httpMethod;
    private Metadata metadata;

}
