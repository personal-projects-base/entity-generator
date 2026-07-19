package com.gonthera.cli.model;

import lombok.Data;
import java.util.List;

@Data
public class Entities {

    private String comment;
    private String entityName;
    private String tableName;
    private String classExtends;
    private List<EntityFields> entityFields;
    private Boolean generateDefaultControllers;
    private Boolean controllerAbstract;
    private Boolean generateDefaultHandlers;
    private Boolean handlerAbstract;
    private boolean serviceAbstract = false;
    private boolean onlyDTO = false;

    public Boolean getGenerateDefaultControllers() {
        return generateDefaultControllers;
    }

    public void setGenerateDefaultControllers(Boolean generateDefaultControllers) {
        this.generateDefaultControllers = generateDefaultControllers;
    }

    public Boolean getControllerAbstract() {
        return controllerAbstract;
    }

    public void setControllerAbstract(Boolean controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    public Boolean getGenerateDefaultHandlers() {
        return generateDefaultHandlers;
    }

    public void setGenerateDefaultHandlers(Boolean generateDefaultHandlers) {
        this.generateDefaultHandlers = generateDefaultHandlers;
    }

    public Boolean getHandlerAbstract() {
        return handlerAbstract;
    }

    public void setHandlerAbstract(Boolean handlerAbstract) {
        this.handlerAbstract = handlerAbstract;
    }

    public boolean isGenerateDefaultControllers() {
        if (generateDefaultControllers != null) return generateDefaultControllers;
        if (generateDefaultHandlers != null) return generateDefaultHandlers;
        return true;
    }

    public boolean isControllerAbstract() {
        if (controllerAbstract != null) return controllerAbstract;
        if (handlerAbstract != null) return handlerAbstract;
        return false;
    }

    public boolean isGenerateDefaultHandlers() {
        return isGenerateDefaultControllers();
    }

    public boolean isHandlerAbstract() {
        return isControllerAbstract();
    }
}
