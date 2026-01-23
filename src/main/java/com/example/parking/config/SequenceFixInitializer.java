package com.example.parking.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

/**
 * Fixes PostgreSQL sequence synchronization issues that can occur
 * when data is imported or inserted without using the sequence.
 */
@Component
@Order(1) // Run early
public class SequenceFixInitializer {

    private static final Logger log = LoggerFactory.getLogger(SequenceFixInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public SequenceFixInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void fixSequences() {
        log.info("Starting PostgreSQL sequence synchronization...");

        // Fix all table sequences
        String[] tables = {"vehicle", "resident", "scan_entry", "scan_session", "outsider_plate", "app_user", "audit_log", "location_definition"};

        for (String table : tables) {
            fixSequence(table);
        }

        log.info("PostgreSQL sequence synchronization completed");
    }

    private void fixSequence(String tableName) {
        try {
            // Get the sequence name (PostgreSQL default naming convention)
            String sequenceName = tableName + "_id_seq";

            // First check if table exists and has data
            String checkSql = "SELECT MAX(id) FROM " + tableName;
            Long maxId = jdbcTemplate.queryForObject(checkSql, Long.class);

            if (maxId != null && maxId > 0) {
                // Reset the sequence to max(id) + 1
                String sql = String.format("SELECT setval('%s', %d)", sequenceName, maxId);
                jdbcTemplate.queryForObject(sql, Long.class);
                log.info("Fixed sequence for table '{}': set to {}", tableName, maxId);
            } else {
                log.debug("Table '{}' is empty or has no ID, skipping sequence fix", tableName);
            }
        } catch (Exception e) {
            log.debug("Could not fix sequence for '{}': {}", tableName, e.getMessage());
        }
    }
}

