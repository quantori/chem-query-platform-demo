CREATE TABLE IF NOT EXISTS task_status (
                                           id UUID NOT NULL PRIMARY KEY,
                                           status INTEGER NOT NULL,
                                           restart_flag INTEGER NOT NULL,
                                           flow_id VARCHAR(255),
                                           deserializer VARCHAR(1023) NOT NULL,
                                           created_by VARCHAR(255) NOT NULL,
                                           created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                                           updated_date TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           state TEXT NOT NULL,
                                           parallelism INTEGER NOT NULL DEFAULT 1,
                                           buffer INTEGER NOT NULL DEFAULT 1,
                                           task_type VARCHAR(255) NOT NULL
);