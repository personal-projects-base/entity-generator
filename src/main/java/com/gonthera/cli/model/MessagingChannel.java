package com.gonthera.cli.model;

import lombok.Data;

@Data
public class MessagingChannel {

    private String name;
    private String className;
    private String queue;
    private String routingKey;
}
