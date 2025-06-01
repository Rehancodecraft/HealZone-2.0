package com.example.healzone.DatabaseConnection;
import java.sql.*;
import java.util.List;

public class DatabaseUtils {
    public static class Column {
        String name;
        String type;
        boolean notNull;
        boolean primaryKey;
        String foreignKey;
        String checkConstraint;

        public Column(String name, String type, boolean notNull, boolean primaryKey, String foreignKey, String checkConstraint) {
            this.name = name;
            this.type = type;
            this.notNull = notNull;
            this.primaryKey = primaryKey;
            this.foreignKey = foreignKey;
            this.checkConstraint = checkConstraint;
        }
    }

    public static void createTable(String tableName, List<Column> columns, String defaultDateColumn, String defaultValue, List<String> compositePrimaryKey) {
        try {
            StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
            boolean first = true;
            for (Column col : columns) {
                if (!first) sql.append(", ");
                String type = col.type;
                if (DatabaseConnection.isSQLite()) {
                    // Remove length specifiers and replace VARCHAR/SERIAL
                    type = type.replaceAll("VARCHAR\\(\\d+\\)", "TEXT")
                            .replaceAll("TEXT\\(\\d+\\)", "TEXT")
                            .replace("SERIAL", "INTEGER PRIMARY KEY AUTOINCREMENT");
                }
                sql.append(col.name).append(" ").append(type);
                if (col.notNull) sql.append(" NOT NULL");
                if (col.primaryKey && (compositePrimaryKey == null || compositePrimaryKey.isEmpty()) && !col.type.equals("SERIAL")) {
                    sql.append(" PRIMARY KEY");
                }
                if (!DatabaseConnection.isSQLite() && col.foreignKey != null) {
                    sql.append(" REFERENCES ").append(col.foreignKey);
                }
                if (!DatabaseConnection.isSQLite() && col.checkConstraint != null) {
                    sql.append(" ").append(col.checkConstraint);
                }
                first = false;
            }
            if (defaultDateColumn != null) {
                String dateDefault = DatabaseConnection.isSQLite() ? (defaultValue != null ? defaultValue : "DATE('now')") : (defaultValue != null ? defaultValue : "CURRENT_DATE");
                sql.append(", ").append(defaultDateColumn).append(" DATE DEFAULT ").append(dateDefault);
            }
            if (compositePrimaryKey != null && !compositePrimaryKey.isEmpty()) {
                sql.append(", PRIMARY KEY (").append(String.join(", ", compositePrimaryKey)).append(")");
            }
            if (DatabaseConnection.isSQLite()) {
                for (Column col : columns) {
                    if (col.foreignKey != null) {
                        // Parse foreignKey (e.g., "doctors(govt_id) ON DELETE CASCADE")
                        String fkClean = col.foreignKey.trim();
                        String refTable = fkClean.substring(0, fkClean.indexOf('(')).trim();
                        String refColumn = fkClean.substring(fkClean.indexOf('(') + 1, fkClean.indexOf(')')).trim();
                        String onDelete = fkClean.contains("ON DELETE CASCADE") ? " ON DELETE CASCADE" : "";
                        sql.append(", FOREIGN KEY (").append(col.name).append(") REFERENCES ")
                                .append(refTable).append("(").append(refColumn).append(")").append(onDelete);
                    }
                }
            }
            sql.append(")");
            System.out.println("Executing SQL: " + sql); // Debugging
            PreparedStatement stmt = DatabaseConnection.connection.prepareStatement(sql.toString());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error creating table " + tableName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void validateCheckConstraint(String tableName, String columnName, List<String> validValues) throws SQLException {
        if (!DatabaseConnection.isSQLite()) return;
        String query = "SELECT DISTINCT " + columnName + " FROM " + tableName + " WHERE " + columnName + " NOT IN (" +
                String.join(",", validValues.stream().map(v -> "'" + v + "'").toList()) + ")";
        try (Statement stmt = DatabaseConnection.connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                throw new SQLException("Invalid " + columnName + " found in " + tableName + ": " + rs.getString(columnName));
            }
        }
    }
}