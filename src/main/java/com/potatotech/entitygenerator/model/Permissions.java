package com.potatotech.entitygenerator.model;

import com.potatotech.entitygenerator.enuns.PermissionType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Permissions {
    private String description = "";
    private String resource = "";
    private List<PermissionType> premissions = new ArrayList<>();
    private boolean permissionDefault = false;
}
