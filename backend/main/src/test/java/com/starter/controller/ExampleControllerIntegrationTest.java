package com.starter.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.starter.BaseIntegrationTest;

/** Simple integration test for ExampleController. */
@AutoConfigureMockMvc
class ExampleControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAllExamples_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/example"))
            .andExpect(status().isOk());
    }
}
