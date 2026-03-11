package com.sicredi.poc.mockaqui.shared.model;

import static java.util.Objects.isNull;

public record ServiceId(
        int id
) {
    public static ServiceId from(final Integer id) {
        if (isNull(id) || id < 1) {
            throw new IllegalArgumentException("Parameter id must not be null");
        }

        return new ServiceId(id);
    }
}
