package com.example.parking.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Fixes PostgreSQL sequence synchronization issues that can occur
 * when data is imported or inserted without using the sequence.
 */
@Component
public class SequenceFixInitializer {

    private static final Logger log = LoggerFactory.getLogger(SequenceFixInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SequenceFixInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixSequences() {
        try {
            // Fix vehicle sequence
            fixSequence("vehicle", "id");

            // Fix resident sequence
            fixSequence("resident", "id");

            // Fix other tables if needed
            fixSequence("scan_entry", "id");
            fixSequence("scan_session", "id");
            fixSequence("outsider_plate", "id");
            fixSequence("app_user", "id");
            fixSequence("audit_log", "id");

            log.info("PostgreSQL sequences synchronized successfully");
        } catch (Exception e) {
            log.warn("Could not synchronize sequences (this is normal on first run): {}", e.getMessage());
        }
    }

    private void fixSequence(String tableName, String columnName) {
        try {
            // Get the sequence name (PostgreSQL default naming convention)
            String sequenceName = tableName + "_" + columnName + "_seq";

            // Reset the sequence to max(id) + 1
            String sql = String.format(
                "SELECT setval('%s', COALESCE((SELECT MAX(%s) FROM %s), 0) + 1, false)",
                sequenceName, columnName, tableName
            );

            jdbcTemplate.queryForObject(sql, Long.class);
            log.debug("Fixed sequence for table: {}", tableName);
        } catch (Exception e) {
            log.debug("Could not fix sequence for {}: {}", tableName, e.getMessage());
        }
    }
}

