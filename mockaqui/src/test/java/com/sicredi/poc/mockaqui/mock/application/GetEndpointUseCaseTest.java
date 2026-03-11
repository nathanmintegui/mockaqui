package com.sicredi.poc.mockaqui.mock.application;

import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import com.sicredi.poc.mockaqui.mock.internal.application.GetEndpointUseCase;
import com.sicredi.poc.mockaqui.mock.dto.GetEndpointRequest;
import com.sicredi.poc.mockaqui.shared.model.Endpoint;
import com.sicredi.poc.mockaqui.shared.types.HttpVerbEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetEndpointUseCaseTest {

    @InjectMocks
    private GetEndpointUseCase getEndpointUseCase;

    @Test
    void foo() {
        // arrange
        Endpoint endpoint = new Endpoint("owner/pets", HttpVerbEnum.GET, null, null);

        RadixTree.getInstance().insert("owner/pets", endpoint);

        getEndpointUseCase.execute(new GetEndpointRequest("owner/pets"));
    }
}
