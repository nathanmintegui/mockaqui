package com.sicredi.poc.mockaqui.api.dto;

public record EndpointDTO(
        int id,
        char[] uri,
        byte verb,
        Object payload
) {
}
