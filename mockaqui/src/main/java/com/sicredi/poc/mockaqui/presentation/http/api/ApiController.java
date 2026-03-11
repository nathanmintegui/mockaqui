package com.sicredi.poc.mockaqui.presentation.http.api;

import com.sicredi.poc.mockaqui.api.*;
import com.sicredi.poc.mockaqui.api.dto.AddEndpointRequest;
import com.sicredi.poc.mockaqui.api.dto.AddServiceRequest;
import com.sicredi.poc.mockaqui.api.dto.EditEndpointRequest;
import com.sicredi.poc.mockaqui.api.dto.StartStopRecordingRequest;
import com.sicredi.poc.mockaqui.shared.model.CollectionId;
import com.sicredi.poc.mockaqui.shared.model.ServiceId;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@AllArgsConstructor
@RestController
@RequestMapping("/api")
public class ApiController {

    private final IAddEndpointUseCase addEndpointUseCase;
    private final IGetEndpointUseCase getEndpointsUseCase;
    private final IAddServiceUseCase addServiceUseCase;
    private final IGetServiceUseCase getServiceUseCase;
    private final IStartStopRecordingUseCase startStopRecordingUseCase;
    private final IEditEndpointUseCase editEndpointUseCase;
    private final IGetEndpointByCollectionUseCase getEndpointByCollectionUseCase;

    // POST: /api/services/{id}/collections/{id}/endpoints
    @PostMapping("/services/{serviceId}/collections/{collectionId}/endpoints")
    public ResponseEntity<?> addEndpoint(
            @PathVariable final Integer serviceId,
            @PathVariable final Integer collectionId,
            @RequestBody AddEndpointRequest req
    ) {
        AddEndpointRequest addEndpointRequest = AddEndpointRequest.from(req);
        addEndpointUseCase.execute(addEndpointRequest, ServiceId.from(serviceId), CollectionId.from(collectionId));
        return ResponseEntity.ok().build();
    }

    // GET: /api/endpoints
    @GetMapping("/endpoints")
    public ResponseEntity<?> getEndpoints() {
        return ResponseEntity.ok(getEndpointsUseCase.execute());
    }

    // PATCH: /api/endpoints/{id}
    @PatchMapping("/endpoints/{id}")
    public ResponseEntity<?> editEndpoint(
            @RequestBody final EditEndpointRequest req,
            @PathVariable final Integer id
    ) {
        editEndpointUseCase.execute(id, req);
        return ResponseEntity.noContent().build();
    }

    // POST: /api/services
    @PostMapping("/services")
    public ResponseEntity<?> addService(@RequestBody AddServiceRequest req) {
        return new ResponseEntity<>(addServiceUseCase.execute(req), HttpStatusCode.valueOf(201));
    }

    // GET: /api/services
    @GetMapping("/services")
    public ResponseEntity<?> getServices() {
        return ResponseEntity.ok(getServiceUseCase.execute());
    }

    // PATCH: /api/start-stop/recording
    @PatchMapping("/start-stop/recording")
    public ResponseEntity<?> startStopRecording(
            @RequestBody StartStopRecordingRequest req,
            @RequestHeader(name = "ldap") String ldap
    ) {
        if (startStopRecordingUseCase.execute(ldap, req.uri()) == -1) {
            return ResponseEntity.internalServerError().body(null);
        }

        return ResponseEntity.noContent().build();
    }

    // GET: /api/collections/{id}/endpoints
    @GetMapping("/collections/{id}/endpoints")
    public ResponseEntity<?> getEndpointsByCollection(@PathVariable final Integer id) {
        return ResponseEntity.ok(getEndpointByCollectionUseCase.execute(id));
    }
}
