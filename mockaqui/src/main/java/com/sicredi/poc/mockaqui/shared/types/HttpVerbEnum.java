package com.sicredi.poc.mockaqui.shared.types;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.modulith.NamedInterface;

/**
 * HttpVerb
 */
@Getter
@AllArgsConstructor
@NamedInterface()
public enum HttpVerbEnum {
    DELETE((byte) 6),
    GET((byte) 1),
    PATCH((byte) 8),
    POST((byte) 7),
    PUT((byte) 5);

    private final byte code;

    public static HttpVerbEnum from(byte code) {
        for (HttpVerbEnum verb : HttpVerbEnum.values()) {
            if (verb.code == code) {
                return verb;
            }
        }
        throw new IllegalArgumentException("Invalid code.");
    }
}
