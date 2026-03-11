package com.example.demo.client;

import com.example.lib.annotation.Mock;
import com.example.lib.annotation.MockableFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@MockableFeign
@FeignClient(
        value = "jplaceholder",
        url = "https://jsonplaceholder.typicode.com/",
        configuration = FeignConfiguration.class
)
public interface JSONPlaceHolderClient {

    @Mock
    @GetMapping("/users")
    List<Object> getUsers();
}
