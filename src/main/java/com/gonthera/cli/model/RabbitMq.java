package com.gonthera.cli.model;

import lombok.Data;

import java.util.List;

@Data
public class RabbitMq {

    private List<MessagingChannel> pub;
    private List<MessagingChannel> sub;
}
