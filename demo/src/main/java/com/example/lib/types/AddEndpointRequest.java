package com.example.lib.types;

import java.util.List;

public record AddEndpointRequest(
        String uri,
        String verb,
        short statusCode,
        List<Header> headers,
        Object payload,
        int responseLatency
) {
}
