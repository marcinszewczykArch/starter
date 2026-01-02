package com.starter.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.starter.BaseIntegrationTest;
import com.starter.dto.ExampleDto;

import java.util.List;

/** Integration tests for ExampleController. */
class ExampleControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/example";
    }

    @Test
    void getAllExamples_shouldReturnList() {
        ResponseEntity<List<ExampleDto>> response =
            restTemplate.exchange(
                baseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isNotEmpty();
    }

    @Test
    void getAllExamples_shouldReturnSampleData() {
        ResponseEntity<List<ExampleDto>> response =
            restTemplate.exchange(
                baseUrl(), HttpMethod.GET, null, new ParameterizedTypeReference<>() {}
            );

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).anyMatch(e -> e.getName().contains("Sample"));
    }
}
