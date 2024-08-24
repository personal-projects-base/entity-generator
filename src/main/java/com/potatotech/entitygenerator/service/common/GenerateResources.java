package com.potatotech.entitygenerator.service.common;

import com.google.gson.Gson;
import com.potatotech.entitygenerator.enuns.PermissionType;
import com.potatotech.entitygenerator.model.Endpoints;
import com.potatotech.entitygenerator.model.Entities;
import com.potatotech.entitygenerator.model.Permissions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class GenerateResources {

    public static void generateResources(List<Entities> entities, List<Endpoints> endpoints){

        try{

            String fileName = String.format("%s/resources.json", Common.resourcePath);
            Common.dropFile(fileName);
            var path = Path.of(fileName);
            var resources = loadResources(entities, endpoints);
            Files.write(path, resources.getBytes(), StandardOpenOption.CREATE);
        }catch (IOException ex){
            ex.printStackTrace();
        }
    }

    private static String loadResources(List<Entities> entities, List<Endpoints> endpoints) {

        List<Permissions> permissions = new ArrayList<>();
        entities.forEach(entity -> {
            var permission = new Permissions();
            permission.setDescription(entity.getComment());
            permission.setResource(entity.getEntityName());
            permission.setPremissions(List.of(PermissionType.CREATE,PermissionType.VIEW,PermissionType.UPDATE,PermissionType.DELETE));
            permission.setPermissionDefault(false);
            permissions.add(permission);
        });

        endpoints.forEach(endpoint -> {
            var permission = new Permissions();
            permission.setResource(endpoint.getMethodName());
            permission.setDescription(endpoint.getComment());

            if(endpoint.getPermissions() != null){
                permission.setPermissionDefault(endpoint.getPermissions().isPermissionDefault());
                permission.setPremissions(endpoint.getPermissions().getPremissions());
            } else {
                permission.setPermissionDefault(false);
                permission.setPremissions(List.of(PermissionType.CREATE,PermissionType.VIEW,PermissionType.UPDATE,PermissionType.DELETE));
            }

            permissions.add(permission);
        });

        return new Gson().toJson(permissions);
    }
}
