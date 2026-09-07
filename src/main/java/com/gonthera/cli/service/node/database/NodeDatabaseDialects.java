package com.gonthera.cli.service.node.database;

import com.gonthera.cli.enuns.DatabaseProvider;
import com.gonthera.cli.model.Properties;

public final class NodeDatabaseDialects {
    private static final NodeDatabaseDialect POSTGRESQL = new PostgreSqlDatabaseDialect();
    private static final NodeDatabaseDialect MONGODB = new MongoDbDatabaseDialect();

    private NodeDatabaseDialects() {
    }

    public static DatabaseProvider provider(Properties project) {
        if (project == null || project.getDatabase() == null || project.getDatabase().getProvider() == null) {
            return DatabaseProvider.POSTGRESQL;
        }
        return project.getDatabase().getProvider();
    }

    public static NodeDatabaseDialect resolve(Properties project) {
        return resolve(provider(project));
    }

    public static NodeDatabaseDialect resolve(DatabaseProvider provider) {
        if (provider == null || provider == DatabaseProvider.POSTGRESQL) return POSTGRESQL;
        if (provider == DatabaseProvider.MONGODB) return MONGODB;
        throw new IllegalArgumentException("Unsupported Node database provider: " + provider);
    }
}
