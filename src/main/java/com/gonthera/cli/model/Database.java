package com.gonthera.cli.model;

import com.gonthera.cli.enuns.DatabaseProvider;
import lombok.Data;

@Data
public class Database {
    private DatabaseProvider provider;
}
