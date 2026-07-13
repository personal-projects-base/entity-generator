package com.potatotech.entitygenerator.model;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Messaging {

    @SerializedName(value = "RabbitMq", alternate = {"rabbitMq", "rabbitMQ"})
    private RabbitMq rabbitMq;
}
