package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IGetServiceUseCase;
import com.sicredi.poc.mockaqui.shared.model.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.sicredi.poc.mockaqui.shared.model.ServiceId.from;

@Slf4j
@AllArgsConstructor
@UseCase
public class GetServiceUseCase implements IGetServiceUseCase {

    private final DataSource dataSource;

    @Override
    public List<Service> execute() {
        String sql = "select id, name from services order by id limit 100";
        List<Service> services = new ArrayList<>();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Service service = new Service(
                        from(rs.getInt("id")),
                        rs.getString("name")
                );
                services.add(service);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return services;
    }
}
