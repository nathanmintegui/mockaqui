package com.sicredi.poc.mockaqui.api;

import com.sicredi.poc.mockaqui.shared.model.Endpoint;

import java.util.Collection;

public interface IGetEndpointByCollectionUseCase {
    Collection<Endpoint> execute(Integer id);
}
