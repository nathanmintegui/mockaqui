package com.sicredi.poc.mockaqui.api;

import com.sicredi.poc.mockaqui.api.dto.AddEndpointRequest;
import com.sicredi.poc.mockaqui.shared.model.CollectionId;
import com.sicredi.poc.mockaqui.shared.model.ServiceId;

public interface IAddEndpointUseCase {
    void execute(AddEndpointRequest req, ServiceId serviceId, CollectionId collectionId);
}
