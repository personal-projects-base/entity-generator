package com.gonthera.cli.model;

import lombok.Data;

@Data
public class Parameters {

    private String parameterName;
    private String parameterType;
    private boolean list = false;
}
