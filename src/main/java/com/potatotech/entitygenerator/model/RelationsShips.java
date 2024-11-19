package com.potatotech.entitygenerator.model;

import lombok.Data;

@Data
public class RelationsShips {

    private String fetchType;
    private String relationShip;
    private String mappedBy;
    private boolean bidirectional = false;
    private boolean reference = false;
}
