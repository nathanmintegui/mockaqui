package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IAddEndpointUseCase;
import com.sicredi.poc.mockaqui.api.dto.AddEndpointRequest;
import com.sicredi.poc.mockaqui.shared.model.CollectionId;
import com.sicredi.poc.mockaqui.shared.model.Endpoint;
import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import com.sicredi.poc.mockaqui.shared.model.ServiceId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
@UseCase
public final class AddEndpointUseCase implements IAddEndpointUseCase {

    private static final RadixTree endpoints = RadixTree.getInstance();
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    public void execute(AddEndpointRequest req, ServiceId serviceId, CollectionId collectionId) {
        if (req.uri().charAt(0) != '/') {
            throw new IllegalArgumentException("Invalid URI resource, expect to start with /");
        }

        try (Connection conn = dataSource.getConnection()) {
            final String selectSql = """
                    SELECT s.name
                    FROM services s,collections c
                    WHERE c.id_service = s.id
                      AND c.id         = ?
                      AND s.id         = ?;
                    """;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, collectionId.id());
                selectStmt.setInt(2, serviceId.id());

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new RuntimeException("Endpoint not found");
                    }
                    var basePath = this.extractBasePath(rs.getString("name"), req.uri());
                    Endpoint endpoint = new Endpoint(
                            -1,
                            basePath,
                            req.verb(),
                            req.statusCode(),
                            req.headers(),
                            req.payload(),
                            req.responseLatency()
                    );
                    endpoints.insert(basePath, endpoint);
                }
            }

            final String insertSql = "INSERT INTO endpoints (id_collection, uri, verb, status_code, payload, headers, query_params, response_latency)" +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                Object payload = req.payload();

                String json =
                        payload instanceof String s
                                ? s
                                : objectMapper.writeValueAsString(payload);
                PGobject payloadJson = new PGobject();
                payloadJson.setType("jsonb");
                payloadJson.setValue(json);

                PGobject headersJson = new PGobject();
                headersJson.setType("jsonb");
                headersJson.setValue(objectMapper.writeValueAsString(req.headers()));

                stmt.setInt(1, collectionId.id());
                stmt.setString(2, req.uri());
                stmt.setByte(3, req.verb().getCode());
                stmt.setShort(4, req.statusCode());
                stmt.setObject(5, payloadJson);
                stmt.setObject(6, headersJson);
                stmt.setObject(7, null);
                stmt.setInt(8, req.responseLatency());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    String extractBasePath(final String serviceName, final String uri) {
        Objects.requireNonNull(serviceName, "Parameter serviceName must not be null.");
        Objects.requireNonNull(uri, "Parameter uri must not be null.");

        assert serviceName.charAt(0) == '/';
        assert uri.charAt(0) == '/';

        return "/" + serviceName + uri;
    }
}
