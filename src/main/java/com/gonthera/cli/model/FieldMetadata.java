package com.gonthera.cli.model;
import lombok.Data;

@Data
public class FieldMetadata {

    private boolean nullable = true;
    private boolean key = false;
}
