package com.sicredi.poc.mockaqui.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class RequestTimingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        request.setAttribute(START_TIME, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        long start = (long) request.getAttribute(START_TIME);
        long durationNs = System.nanoTime() - start;
        double durationMs = durationNs / 1_000_000.0;

        log.info(
                "Request {} {} took {} ms",
                request.getMethod(),
                request.getRequestURI(),
                durationMs
        );
    }
}
