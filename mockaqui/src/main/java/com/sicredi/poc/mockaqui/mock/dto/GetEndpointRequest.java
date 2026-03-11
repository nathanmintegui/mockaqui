package com.sicredi.poc.mockaqui.mock.dto;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public record GetEndpointRequest(
        String uri
) {
}
