package org.bee.product.webclient;

import org.bee.product.data.ProductUserData;
import org.springframework.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class ProductUserWebClient {
   // @Value("{user.baseurl}")
   // private String userbaseurl;

    protected WebClient client = WebClient.builder()
            .baseUrl("http://localhost:8081/brite/api")
            .defaultCookie("cookieKey", "cookieValue")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8081/brite/api"))
            .build();

    public void getproductUserData(){


        Mono<ProductUserData> userMono = client.get()
                .uri("/api/v1/user/findById/{id}", "554")
                .retrieve()
                .bodyToMono(ProductUserData.class);


        userMono.subscribe(System.out::println);
    }
}
