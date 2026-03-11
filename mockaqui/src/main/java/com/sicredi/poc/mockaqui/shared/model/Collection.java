package com.sicredi.poc.mockaqui.shared.model;

public record Collection(
        CollectionId id,
        ServiceId serviceId,
        String name
) {
}
