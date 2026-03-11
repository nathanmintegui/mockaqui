package com.example.lib.decoder;

import com.example.lib.types.AddEndpointRequest;
import com.example.lib.types.Header;
import feign.Request;
import feign.Response;
import feign.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class RoutingDecoder implements Decoder {

    private static final Logger logger = LoggerFactory.getLogger(RoutingDecoder.class);

    private final Decoder delegate;

    @Value("${mock.api.base-url}")
    private String mockApiBaseUrl;

    @Value("${mock.recording-enabled:true}")
    private boolean recordingEnabled;

    public RoutingDecoder(Decoder delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object decode(final Response response, final Type type) throws IOException {
        if (!this.recordingEnabled) {
            return delegate.decode(response, type);
        }

        final Object decodedResponse = delegate.decode(response, type);

        try {
            final Collection<String> routingHeaders = response.request().headers().get("X-Enable-Routing");

            if (routingHeaders != null && !routingHeaders.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        logger.debug("[DEBUG] :: [RoutingDecoder.decode]: Sending response back to mock api.");
                        this.sendRecordedData(decodedResponse, response);
                    } catch (Exception e) {
                        logger.error("[ERROR] :: [RoutingDecoder.decode]: Error during response routing", e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("[ERROR] :: [RoutingDecoder.decode]: Error checking routing headers, but decode succeeded", e);
            // Don't fail the decode if routing check fails
        }

        return decodedResponse;
    }

    private Object getBody(final Response response, final Object decodedResponse) {
        try {
            final String uriWithoutHttp = response.request().url()
                    .replace("https://", "")
                    .replace("http://", "");

            final String uri = "/" + uriWithoutHttp.substring(uriWithoutHttp.indexOf("/") + 1);

            final Request.HttpMethod httpMethod = response.request().httpMethod();
            final var headers = response.headers();
            final List<Header> parsedHeaders = new ArrayList<>(headers.size());

            headers.forEach((k, v) ->
                    parsedHeaders.add(new Header(k, v.stream()
                            .findFirst()
                            .orElse(""), true))
            );

            final int statusCode = response.status();

            return new AddEndpointRequest(
                    uri,
                    httpMethod.name(),
                    (short) statusCode,
                    parsedHeaders,
                    decodedResponse,
                    200
            );
        } catch (Exception e) {
            logger.error(
                    "[ERROR] :: [RoutingDecoder.getBody]: An unexpected error has occurred, reason: {}",
                    e.getMessage()
            );
            return new Object();
        }
    }

    private void sendRecordedData(final Object decodedResponse, final Response response) {
        try {
            final RestClient client = RestClient.builder()
                    .baseUrl(mockApiBaseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            var body = this.getBody(response, decodedResponse);

            final ResponseEntity<String> res = client.post()
                    .uri("/services/1/collections/1/endpoints")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(String.class);

            if (res.getStatusCode().is2xxSuccessful()) {
                logger.info("[INFO] :: [RoutingDecoder.sendRecordedData]: The response content was successfully sent to the mock api!");
                return;
            }
            if (res.getStatusCode().isError()) {
                logger.info(
                        "[INFO] :: [RoutingDecoder.sendRecordedData]: Could not the original data to to the mock api, reason: {}",
                        res.getBody()
                );
            }
        } catch (Exception e) {
            logger.error(
                    "[ERROR] :: [RoutingDecoder.sendRecordedData]: An error has occurred, reason: {}",
                    e.getMessage()
            );
        }
    }
}
