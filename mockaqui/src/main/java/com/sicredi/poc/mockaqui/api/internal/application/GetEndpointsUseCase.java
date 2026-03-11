package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IGetEndpointUseCase;
import com.sicredi.poc.mockaqui.api.dto.CollectionDTO;
import com.sicredi.poc.mockaqui.api.dto.EndpointDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@UseCase
public class GetEndpointsUseCase implements IGetEndpointUseCase {

    private final DataSource dataSource;

    public Object execute() {
        String sql = """
                select
                 	c.id as id_collection,
                 	c.name,
                 	e.id as id_endpoint,
                 	e.uri,
                 	e.verb,
                 	e.payload
                 from collections c
                 join endpoints e on e.id_collection = c.id
                """;
        Map<Integer, CollectionDTO> collectionMap = new HashMap<>();

        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Integer key = rs.getInt("id_endpoint");
                if (collectionMap.get(key) == null) {
                    CollectionDTO value = new CollectionDTO(
                            rs.getInt("id_collection"),
                            rs.getString("name").toCharArray(),
                            List.of(
                                    new EndpointDTO(
                                            rs.getInt("id_endpoint"),
                                            rs.getString("uri").toCharArray(),
                                            rs.getByte("verb"),
                                            rs.getObject("payload").toString()
                                    )
                            )
                    );
                    collectionMap.put(key, value);
                } else {
                    CollectionDTO collectionDTO = collectionMap.get(key);
                    collectionDTO.endpoints.add(new EndpointDTO(
                            rs.getInt("id_endpoint"),
                            rs.getString("uri").toCharArray(),
                            rs.getByte("verb"),
                            rs.getObject("payload").toString()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return collectionMap;
    }

    record CollectionDTO(
            int id,
            char[] name,
            List<EndpointDTO> endpoints
    ) {
    }

    record EndpointDTO(
            int id,
            char[] uri,
            byte verb,
            Object payload
    ) {
    }
}
