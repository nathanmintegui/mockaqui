package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IEditEndpointUseCase;
import com.sicredi.poc.mockaqui.api.dto.EditEndpointRequest;
import com.sicredi.poc.mockaqui.shared.exception.BadRequestException;
import com.sicredi.poc.mockaqui.shared.exception.NotFoundException;
import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import com.sicredi.poc.mockaqui.shared.persistence.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import tools.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@RequiredArgsConstructor
@UseCase
final class EditEndpointUseCase implements IEditEndpointUseCase {

    private static final RadixTree endpoints = RadixTree.getInstance();
    private final DataSource dataSource;
    private final ObjectMapper objectMapper;

    @Override
    public void execute(final Integer id, final EditEndpointRequest req) {
        if (!req.isAtLeasOneFieldFilled()) {
            throw new BadRequestException("Invalid request, should provide at least one field to be patched.");
        }

        // @@Performance:: may try to update without verify if exists before, if the resource really does not exist
        // won't be such a big problem.
        if (!this.doesEndpointExist(id)) {
            throw new NotFoundException(
                    String.format("Could not find endpoint with id %d.", id)
            );
        }

        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement ps = QueryBuilder
                    .update("endpoints")
                    .setIfPresent(req.uri(), "uri")
                    .setIfPresent(req.verb(), "verb")
                    .setIfPresent(req.statusCode(), "status_code")
                    .setIfPresent(this.toJsonb(req.headers()), "headers")
                    .setIfPresent(this.toJsonb(req.payload()), "payload")
                    .setIfPresent(req.responseLatency(), "response_latency")
                    .where("id = ?", id)
                    .prepare(conn);
            int rows = ps.executeUpdate();
            if (rows <= 0) {
                log.error("Performed updated did not execute successfully.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // @@TODO:: update the radix tree structure
        endpoints.clear();
    }

    boolean doesEndpointExist(final int id) {
        final String sql = "SELECT COUNT(*) FROM endpoints WHERE id = ?";

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1) > 0;
                }
            }
        } catch (SQLException e) {
            log.error(
                    "ERROR : [EditEndpointUseCase.doesEndpointExist]: {}",
                    e.getMessage(),
                    e
            );
        }
        return false;
    }

    PGobject toJsonb(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        PGobject jsonb = new PGobject();
        jsonb.setType("jsonb");
        jsonb.setValue(objectMapper.writeValueAsString(value));
        return jsonb;
    }
}
