package com.sicredi.poc.mockaqui.shared.model;

import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;
import lombok.Builder;

import java.util.List;

@Builder
public record Endpoint(
        int id,
        String uri,
        HttpVerbEnum verb,
        short statusCode,
        List<Header> headers,
        Object payload,
        int responseLatency
) {
}
