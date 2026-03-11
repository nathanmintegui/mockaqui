package com.example.demo.client;

import com.example.lib.interceptor.MockRequestInterceptor;
import com.example.lib.decoder.RoutingDecoder;
import feign.RequestInterceptor;
import feign.Retryer;
import feign.codec.Decoder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfiguration {

    @Bean
    public Decoder feignDecoder(ObjectProvider<FeignHttpMessageConverters> messageConverters) {
        Decoder springDecoder = new SpringDecoder(messageConverters);
        return new RoutingDecoder(springDecoder);
    }

    @Bean
    public RequestInterceptor mockAwareRequestInterceptor() {
        return new MockRequestInterceptor();
    }

    // Control order if needed
    @Bean
    public Retryer retryer() {
        return new Retryer.Default();
    }
}
