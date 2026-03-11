package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IAddServiceUseCase;
import com.sicredi.poc.mockaqui.api.dto.AddServiceRequest;
import com.sicredi.poc.mockaqui.shared.model.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;

import static com.sicredi.poc.mockaqui.shared.model.ServiceId.from;

@Slf4j
@AllArgsConstructor
@UseCase
final class AddServiceUseCase implements IAddServiceUseCase {

    private final DataSource dataSource;

    @Override
    public Service execute(AddServiceRequest req) {
        //@@TODO: validate input
        final String sqlInsertServices = "INSERT INTO services(name) VALUES (?)";

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement(sqlInsertServices, Statement.RETURN_GENERATED_KEYS);

            stmt.setString(1, req.name());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Insert failed, no rows affected.");
            }

            final int serviceId;
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    serviceId = generatedKeys.getInt(1);
                    System.out.println("Generated ID: " + serviceId);
                } else {
                    throw new SQLException("Insert failed, no ID obtained.");
                }
            }

            final String sqlInsertCollections = "INSERT INTO collections (id_service, name) VALUES(?, ?) ON CONFLICT DO NOTHING";
            PreparedStatement stmtCollection = connection.prepareStatement(sqlInsertCollections);

            stmtCollection.setLong(1, serviceId);
            final String DEFAULT_COLLECTION_NAME = "Default";
            stmtCollection.setString(2, DEFAULT_COLLECTION_NAME);

            stmtCollection.executeUpdate();

            return new Service(from(serviceId), req.name());
        } catch (SQLException e) {
            try {
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            log.error("ERROR : [AddServiceUseCase.execute]: An error has occurred, reason, {}.", e.getMessage());
        } finally {
            try {
                if (connection != null) {
                    connection.setAutoCommit(true);
                    connection.close();
                }
            } catch (SQLException e) {
                log.error("ERROR : [AddServiceUseCase.execute]:An error has occurred while trying to close the JDBC connection.");
            }
        }
        return null;
    }
}
