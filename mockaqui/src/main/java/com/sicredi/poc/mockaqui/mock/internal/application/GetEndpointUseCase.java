package com.sicredi.poc.mockaqui.mock.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.mock.IGetEndpointUseCase;
import com.sicredi.poc.mockaqui.mock.dto.GetEndpointRequest;
import com.sicredi.poc.mockaqui.shared.exception.BadRequestException;
import com.sicredi.poc.mockaqui.shared.exception.NotFoundException;
import com.sicredi.poc.mockaqui.shared.model.Endpoint;
import com.sicredi.poc.mockaqui.shared.model.Header;
import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;
import lombok.AllArgsConstructor;
import org.postgresql.util.PGobject;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum.GET;

@AllArgsConstructor
@UseCase
public class GetEndpointUseCase implements IGetEndpointUseCase {

    private final RadixTree endpoints = RadixTree.getInstance();

    private final DataSource dataSource;

    private final ObjectMapper objectMapper;

    public Endpoint execute(GetEndpointRequest req) {
        Endpoint value = endpoints.search(req.uri());
        if (value == null) {
            // @@Incomplete: try search on db if this uri is persisted, if not found yet then throw not found.
            // But this fallback alternative doesn't cover all the edge cases for the various types of uri that can exist,
            // for example if the given URI resource contains any path parameter this may not work as expected.

            final String[] uriPaths = req.uri().split("/", 2);
            final String baseUriPath = uriPaths[0];
            final String restOfUri = "/" + uriPaths[1];

            try (Connection conn = dataSource.getConnection()) {
                /* =====================================================================================================
                 *  FIRST QUERY PART : Tries to find endpoint by base path on services table
                 * =====================================================================================================
                 * */
                final String findServiceNameSql = """
                        SELECT name
                        FROM services
                        WHERE name = ?
                        """;
                PreparedStatement findServiceStmt = conn.prepareStatement(findServiceNameSql);
                findServiceStmt.setString(1, baseUriPath);

                try (ResultSet rs = findServiceStmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new NotFoundException("No static resource found for " + req.uri());
                    }
                }

                /* =====================================================================================================
                 *  SECOND QUERY PART : Tries to find endpoint by url without the base path
                 * =====================================================================================================
                 * */
                final String sql = """
                        Select id, uri, verb, status_code, headers, payload, response_latency
                        From endpoints
                        Where uri = ?
                        Limit 1
                        """;
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, restOfUri);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        List<Header> headers =
                                objectMapper.readValue(
                                        rs.getString("headers"),
                                        new TypeReference<>() {
                                        }
                                );
                        Endpoint endpointFromDb = new Endpoint(
                                rs.getInt("id"),
                                rs.getString("uri"),
                                HttpVerbEnum.from(rs.getByte("verb")),
                                rs.getShort("status_code"),
                                headers,
                                objectMapper.readTree(rs.getString("payload")),
                                rs.getInt("response_latency")
                        );

                        endpoints.insert(req.uri(), endpointFromDb);
                        return endpointFromDb;
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            throw new NotFoundException("No static resource found for " + req.uri());
        }

        if (!GET.equals(value.verb())) {
            throw new BadRequestException("Endpoint errado? Método não suportado.");
        }
        return value;
    }
}
