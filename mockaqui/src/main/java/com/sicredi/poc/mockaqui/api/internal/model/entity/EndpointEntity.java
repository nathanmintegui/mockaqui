package com.sicredi.poc.mockaqui.api.internal.model.entity;

public record EndpointEntity(
        int id,
        int idCollection,
        String uri,
        byte verb,
        short statusCode,
        Object payload,
        Object headers,
        Object queryParams,
        int responseLatency
) {
}
