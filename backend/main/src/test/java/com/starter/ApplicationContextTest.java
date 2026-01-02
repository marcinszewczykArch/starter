package com.starter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/** Test to verify that the Spring application context loads successfully. */
class ApplicationContextTest extends BaseIntegrationTest {

    @Autowired private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void applicationBeanExists() {
        assertThat(applicationContext.containsBean("application")).isTrue();
    }
}
