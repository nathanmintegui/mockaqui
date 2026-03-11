package com.sicredi.poc.mockaqui.api.dto;

import com.sicredi.poc.mockaqui.shared.model.Header;
import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;

import java.util.List;

import static java.util.Objects.isNull;

public record EditEndpointRequest(
        String uri,
        HttpVerbEnum verb,
        Short statusCode,
        List<Header> headers,
        Object payload,
        Integer responseLatency
) {
    public boolean isAtLeasOneFieldFilled() {
        int fieldsFilledCount = 0;

        if (!isNull(uri)) {
            fieldsFilledCount += 1;
        } else if (!isNull(verb)) {
            fieldsFilledCount += 1;
        } else if (!isNull(statusCode)) {
            fieldsFilledCount += 1;
        } else if (!headers().isEmpty()) {
            fieldsFilledCount += 1;
        } else if (!isNull(payload)) {
            fieldsFilledCount += 1;
        } else if (!isNull(responseLatency)) {
            fieldsFilledCount += 1;
        }

        return fieldsFilledCount >= 1;
    }
}
