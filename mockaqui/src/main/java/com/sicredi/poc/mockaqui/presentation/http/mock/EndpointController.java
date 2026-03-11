package com.sicredi.poc.mockaqui.presentation.http.mock;

import com.sicredi.poc.mockaqui.cache.IRecordingsCacheManager;
import com.sicredi.poc.mockaqui.mock.IGetEndpointUseCase;
import com.sicredi.poc.mockaqui.mock.dto.GetEndpointRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/mock")
public class EndpointController {

    private static final String RECORDING_HEADER_KEY = "Recording";
    private static final String RECORDING_HEADER_VALUE = "true";

    private final IGetEndpointUseCase getEndpointUseCase;
    private final IRecordingsCacheManager recordingsCacheManager;

    @Value("${app.delay-endpoint:true}")
    private boolean delayEndpoint;

    @RequestMapping(value = "/**", method = RequestMethod.GET)
    public CompletableFuture<ResponseEntity<?>> getEndpoint(
            HttpServletRequest request,
            @RequestAttribute("startTime") Long startTime,
            @RequestHeader(name = "ldap", defaultValue = "", required = false) final String ldap
    ) {
        if (recordingsCacheManager.get(ldap) != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add(RECORDING_HEADER_KEY, RECORDING_HEADER_VALUE);
            return CompletableFuture.completedFuture(
                    new ResponseEntity<>(null, headers, HttpStatusCode.valueOf(204))
            );
        }

        String uri = request.getRequestURI().replace("/mock/", "");

        final var endpoint = this.getEndpointUseCase.execute(new GetEndpointRequest(uri));

        HttpHeaders headers = new HttpHeaders();
        headers.add(RECORDING_HEADER_KEY, "false");
        endpoint.headers().forEach(header -> {
            //@@Performance: May filter this on db select level instead of filtering in memory, what might not be
            // necessary because each endpoint won't have more than 10 headers I suppose...
            if (header.isSelected()) {
                headers.add(header.key(), header.value());
            }
        });
        var response = new ResponseEntity<>(endpoint.payload(), headers, HttpStatusCode.valueOf(endpoint.statusCode()));

        if (delayEndpoint) {
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            long latencyMs = endpoint.responseLatency();
            long remainingMs = Math.max(0, latencyMs - elapsedMs);

            return CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            Thread.sleep(remainingMs);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        return response;
                    },
                    CompletableFuture.delayedExecutor(remainingMs, TimeUnit.MILLISECONDS)
            );
        }

        return CompletableFuture.completedFuture(response);
    }
}
