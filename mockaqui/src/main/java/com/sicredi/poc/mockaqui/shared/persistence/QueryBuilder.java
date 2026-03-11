package com.sicredi.poc.mockaqui.shared.persistence;

import org.postgresql.util.PGobject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class QueryBuilder {

    private final StringBuilder sql = new StringBuilder();
    private final List<Object> params = new ArrayList<>();
    private boolean hasWhere = false;
    private boolean isUpdate = false;
    private int setCount = 0;

    public static QueryBuilder select(String columns) {
        QueryBuilder q = new QueryBuilder();
        q.sql.append("SELECT ").append(columns).append(" ");
        return q;
    }

    public static QueryBuilder update(String table) {
        QueryBuilder q = new QueryBuilder();
        q.sql.append("UPDATE ").append(table).append(" SET ");
        q.isUpdate = true;
        return q;
    }

    public QueryBuilder from(String table) {
        sql.append("FROM ").append(table).append(" ");
        return this;
    }

    public QueryBuilder set(String column, Object value) {
        if (!isUpdate) {
            throw new IllegalStateException("set() can only be used with UPDATE");
        }
        if (setCount > 0) {
            sql.append(", ");
        }
        sql.append(column).append(" = ?");
        params.add(value);
        setCount++;
        return this;
    }

    public QueryBuilder setIfPresent(Object value, String column) {
        if (value instanceof PGobject) {
            set(column, value);
        } else if (value instanceof List<?>) {
            if (!((List<?>) value).isEmpty()) {
                set(column, value);
            }
        } else if (value != null) {
            set(column, value);
        }
        return this;
    }

    public QueryBuilder where(String condition, Object value) {
        if (!hasWhere && isUpdate && setCount > 0) {
            sql.append(" "); // ensure space before WHERE after SET clauses
        }
        sql.append(hasWhere ? "AND " : "WHERE ");
        sql.append(condition).append(" ");
        params.add(value);
        hasWhere = true;
        return this;
    }

    public QueryBuilder orderBy(String clause) {
        sql.append("ORDER BY ").append(clause).append(" ");
        return this;
    }

    public QueryBuilder limit(int limit) {
        sql.append("LIMIT ? ");
        params.add(limit);
        return this;
    }

    public PreparedStatement prepare(Connection conn) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(sql.toString());
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
        return ps;
    }
}
