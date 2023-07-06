package com.potatotech.entitygenerator.model;

import lombok.Data;

import java.util.List;

@Data
public class Entities {


    private String entityName;
    private String tableName;
    private String classExtends;
    private List<EntityFields> entityFields;
}
