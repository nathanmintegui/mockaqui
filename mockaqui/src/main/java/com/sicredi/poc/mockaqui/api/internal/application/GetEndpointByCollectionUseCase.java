package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IGetEndpointByCollectionUseCase;
import com.sicredi.poc.mockaqui.shared.model.Endpoint;
import com.sicredi.poc.mockaqui.shared.model.Header;
import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@UseCase
final class GetEndpointByCollectionUseCase implements IGetEndpointByCollectionUseCase {

    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    @Override
    public Collection<Endpoint> execute(Integer id) {
        final String sql = """
                Select
                    e.id,
                    e.uri,
                    e.verb,
                    e.status_code,
                    e.payload,
                    e.headers,
                    e.response_latency
                From endpoints e
                Where e.id_collection = ?
                """;
        List<Endpoint> endpoints = new ArrayList<>();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    List<Header> headers =
                            objectMapper.readValue(
                                    rs.getString("headers"),
                                    new TypeReference<>() {
                                    }
                            );

                    Endpoint endpoint = Endpoint.builder()
                            .id(rs.getInt("id"))
                            .uri(rs.getString("uri"))
                            .verb(HttpVerbEnum.from(rs.getByte("verb")))
                            .statusCode(rs.getShort("status_code"))
                            .responseLatency(rs.getInt("response_latency"))
                            .payload(objectMapper.readTree(rs.getObject("payload").toString()))
                            .headers(headers)
                            .build();

                    endpoints.add(endpoint);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return endpoints;
    }
}
