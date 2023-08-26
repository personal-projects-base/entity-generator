package com.potatotech.entitygenerator.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Metadata {

    private List<Parameters> input;
    private List<Parameters> output = new ArrayList<>();
    private boolean anonymous;
}
