package com.potatotech.entitygenerator.service.common;

import com.potatotech.entitygenerator.model.Parameters;

import java.util.List;

public class Validators {

    public static boolean validRequestOrResponseData(List<Parameters> parameters) {
        return parameters.stream().anyMatch(item -> item.getParameterType().equalsIgnoreCase("requestdata") || item.getParameterType().equalsIgnoreCase("responsedata"));
    }
}
