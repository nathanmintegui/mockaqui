package com.sicredi.poc.mockaqui.api;

import com.sicredi.poc.mockaqui.shared.model.Service;

import java.util.List;

public interface IGetServiceUseCase {
    List<Service> execute();
}
