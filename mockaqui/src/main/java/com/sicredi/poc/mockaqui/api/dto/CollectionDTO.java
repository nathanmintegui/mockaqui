package com.sicredi.poc.mockaqui.api.dto;

import java.util.List;

public record CollectionDTO(
        int id,
        char[] name,
        List<EndpointDTO> endpoints
) {
}
