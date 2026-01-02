package com.starter.core.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

    @Test
    void isBlank_shouldReturnTrueForNull() {
        assertThat(StringUtils.isBlank(null)).isTrue();
    }

    @Test
    void isBlank_shouldReturnTrueForEmptyString() {
        assertThat(StringUtils.isBlank("")).isTrue();
    }

    @Test
    void isBlank_shouldReturnTrueForWhitespaceOnly() {
        assertThat(StringUtils.isBlank("   ")).isTrue();
    }

    @Test
    void isBlank_shouldReturnFalseForNonBlankString() {
        assertThat(StringUtils.isBlank("hello")).isFalse();
    }
}
