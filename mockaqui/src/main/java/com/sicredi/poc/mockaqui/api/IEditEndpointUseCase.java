package com.sicredi.poc.mockaqui.api;

import com.sicredi.poc.mockaqui.api.dto.EditEndpointRequest;

public interface IEditEndpointUseCase {
    void execute(Integer id, EditEndpointRequest req);
}
