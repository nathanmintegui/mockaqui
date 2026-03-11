package com.sicredi.poc.mockaqui.api.dto;

import com.sicredi.poc.mockaqui.shared.model.Header;
import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;

import java.util.List;

/**
 * AddEndpointRequest
 */
public record AddEndpointRequest(
        String uri,
        HttpVerbEnum verb,
        short statusCode,
        List<Header> headers,
        Object payload,
        int responseLatency
) {
    public static AddEndpointRequest from(AddEndpointRequest req) {
        return new AddEndpointRequest(
                req.uri(),
                req.verb,
                req.statusCode,
                req.headers,
                req.payload,
                req.responseLatency
        );
    }
}
