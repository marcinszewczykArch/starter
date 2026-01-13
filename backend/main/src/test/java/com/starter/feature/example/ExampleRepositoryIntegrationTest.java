package com.starter.feature.example;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.starter.BaseIntegrationTest;

import java.util.List;

/** Simple integration test for ExampleRepository. */
class ExampleRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ExampleRepository exampleRepository;

    @Test
    void findAll_shouldReturnExamples() {
        List<Example> examples = exampleRepository.findAll();

        assertThat(examples).isNotNull();
    }
}
