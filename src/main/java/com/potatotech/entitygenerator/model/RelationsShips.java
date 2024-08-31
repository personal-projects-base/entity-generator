package com.potatotech.entitygenerator.model;

import lombok.Data;

@Data
public class RelationsShips {

    private String fetchType;
    private String relationShip;
    private boolean bidirectional = false;
}
