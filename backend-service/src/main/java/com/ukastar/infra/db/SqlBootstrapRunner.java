package com.ukastar.infra.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

@Component
@ConditionalOnProperty(prefix = "db.bootstrap", name = "enabled", havingValue = "true")
public class SqlBootstrapRunner {

    private static final Logger log = LoggerFactory.getLogger(SqlBootstrapRunner.class);

    private final DataSource dataSource;

    @Value("${db.bootstrap.schema:classpath:/db/schema.sql}")
    private String schemaLocation;

    @Value("${db.bootstrap.seed:classpath:/db/seed.sql}")
    private String seedLocation;

    @Value("${db.bootstrap.externalDir:}")
    private String externalDir;

    public SqlBootstrapRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() throws Exception {
        log.info("DB bootstrap enabled, executing schema and seed scripts...");
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(true);
            if (externalDir != null && !externalDir.isBlank()) {
                log.info("Executing external SQL from {}", externalDir);
                ScriptUtils.executeSqlScript(conn, new FileSystemResource(externalDir + "/database/schema/001_tables.sql"));
                ScriptUtils.executeSqlScript(conn, new FileSystemResource(externalDir + "/database/seed/001_seed_data.sql"));
            } else {
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("/db/schema.sql"));
                ScriptUtils.executeSqlScript(conn, new ClassPathResource("/db/seed.sql"));
            }
        }
        log.info("DB bootstrap completed.");
    }
}
