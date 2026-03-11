package com.sicredi.poc.mockaqui.api;

import com.sicredi.poc.mockaqui.api.dto.AddServiceRequest;
import com.sicredi.poc.mockaqui.shared.model.Service;

public interface IAddServiceUseCase {
    Service execute(AddServiceRequest req);
}
