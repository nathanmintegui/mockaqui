package com.example.lib.interceptor;

import com.example.lib.annotation.Mock;
import com.example.lib.annotation.MockableFeign;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @Notes: Might consider adding properties to indicate when the interceptor should show logs to make debugging easier
 */
@Component
public final class MockRequestInterceptor implements RequestInterceptor {

    Logger logger = LoggerFactory.getLogger(MockRequestInterceptor.class);

    @Value("${mock.mock-api.base-url}")
    private String mockBaseUrl;

    @Value("${mock.recording-enabled}")
    private boolean recordingEnabled;

    @Value("${mock.ldap}")
    private String ldap;

    @Override
    public void apply(final RequestTemplate template) {
        try {
            final Class<?> clientClass =
                    template.methodMetadata()
                            .method()
                            .getDeclaringClass();

            if (clientClass.isAnnotationPresent(MockableFeign.class)) {
                final String methodName =
                        template.methodMetadata()
                                .method()
                                .getName();

                if (this.isMethodMocked(clientClass, methodName)) {
                    if (this.recordingEnabled) {
                        if (this.isRecording(template)) {
                            template.header("X-Enable-Routing", "true");
                            return;
                        }
                    }

                    final String originalUrl = template.feignTarget().url();
                    final String path = template.path();
                    final String newUrl = mockBaseUrl + path;

                    // Replace the URL
                    template.target(newUrl);

                    logger.info(
                            "\uD83D\uDD04 Redirecting [{}.{}]: {}{} -> {}",
                            clientClass.getSimpleName(),
                            methodName,
                            originalUrl,
                            path,
                            newUrl
                    );
                }
            }
        } catch (Exception e) {
            logger.error("⚠️ Error in MockAwareRequestInterceptor: {}", e.getMessage());
        }
    }

    private boolean isMethodMocked(final Class<?> clientClass, final String methodName) {
        try {
            for (Method method : clientClass.getMethods()) {
                if (method.getName().equals(methodName) && method.isAnnotationPresent(Mock.class)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(
                    "[ERROR] :: [MockRequestInterceptor.isMethodMocked]: Method not found or error {}",
                    e.getMessage()
            );
        }
        return false;
    }

    private boolean isRecording(final RequestTemplate template) {
        try {
            Objects.requireNonNull(template);

            final RestClient client = RestClient.builder()
                    .baseUrl(this.mockBaseUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("ldap", ldap)
                    .build();

            final ResponseEntity<String> response = client.get()
                    .uri(template.path())
                    .retrieve()
                    .toEntity(String.class);

            final HttpHeaders headers = response.getHeaders();

            final String recordingHeader = "Recording";
            final String isRecording = headers.getFirst(recordingHeader);
            if (isRecording == null) {
                logger.warn("[WARN] :: [MockAwareRequestInterceptor.isRecording]: Could not get 'Recording' header from mock api.");
                return false;
            }

            return isRecording.equals("true");
        } catch (Exception e) {
            System.out.printf(
                    "[ERROR] :: [MockAwareRequestInterceptor.isRecording]: An error has occurred, reason: %s.",
                    e.getMessage()
            );
            return false;
        }
    }
}
