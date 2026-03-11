package com.sicredi.poc.mockaqui.mock;

import com.sicredi.poc.mockaqui.mock.dto.GetEndpointRequest;
import com.sicredi.poc.mockaqui.shared.model.Endpoint;

public interface IGetEndpointUseCase {

    Endpoint execute(GetEndpointRequest req);
}
