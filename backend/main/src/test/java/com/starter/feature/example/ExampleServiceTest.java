package com.starter.feature.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.starter.core.security.UserPrincipal;
import com.starter.core.user.User;
import com.starter.feature.example.dto.CreateExampleRequest;
import com.starter.feature.example.dto.ExampleDto;

import java.time.Instant;
import java.util.List;

/** Unit tests for ExampleService. */
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

    @Mock
    private ExampleRepository exampleRepository;

    @InjectMocks
    private ExampleService exampleService;

    @Test
    void getExamples_adminSeesAllExamples() {
        // Given
        UserPrincipal adminPrincipal =
            UserPrincipal.builder().id(1L).email("admin@example.com").role(User.Role.ADMIN).build();

        Example example1 = createExample(1L, 10L, "Example 1");
        Example example2 = createExample(2L, 20L, "Example 2");
        when(exampleRepository.findAll()).thenReturn(List.of(example1, example2));

        // When
        List<ExampleDto> result = exampleService.getExamples(adminPrincipal);

        // Then
        assertThat(result).hasSize(2);
        verify(exampleRepository).findAll();
    }

    @Test
    void getExamples_userSeesOnlyOwnExamples() {
        // Given
        Long userId = 10L;
        UserPrincipal userPrincipal =
            UserPrincipal.builder().id(userId).email("user@example.com").role(User.Role.USER).build();

        Example example = createExample(1L, userId, "My Example");
        when(exampleRepository.findByUserId(userId)).thenReturn(List.of(example));

        // When
        List<ExampleDto> result = exampleService.getExamples(userPrincipal);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
        verify(exampleRepository).findByUserId(userId);
    }

    @Test
    void createExample_setsCurrentUserAsOwner() {
        // Given
        Long userId = 10L;
        UserPrincipal userPrincipal =
            UserPrincipal.builder().id(userId).email("user@example.com").role(User.Role.USER).build();

        CreateExampleRequest request =
            CreateExampleRequest.builder().name("New Example").description("Description").build();

        when(exampleRepository.save(any(Example.class)))
            .thenAnswer(
                invocation -> {
                    Example e = invocation.getArgument(0);
                    return Example.builder()
                        .id(1L)
                        .userId(e.getUserId())
                        .name(e.getName())
                        .description(e.getDescription())
                        .active(e.isActive())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                }
            );

        // When
        ExampleDto result = exampleService.createExample(request, userPrincipal);

        // Then
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("New Example");

        ArgumentCaptor<Example> captor = ArgumentCaptor.forClass(Example.class);
        verify(exampleRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
    }

    @Test
    void createExample_setsActiveToTrue() {
        // Given
        UserPrincipal userPrincipal =
            UserPrincipal.builder().id(1L).email("user@example.com").role(User.Role.USER).build();

        CreateExampleRequest request =
            CreateExampleRequest.builder().name("New Example").description("Description").build();

        when(exampleRepository.save(any(Example.class)))
            .thenAnswer(
                invocation -> {
                    Example e = invocation.getArgument(0);
                    return Example.builder()
                        .id(1L)
                        .userId(e.getUserId())
                        .name(e.getName())
                        .description(e.getDescription())
                        .active(e.isActive())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                }
            );

        // When
        ExampleDto result = exampleService.createExample(request, userPrincipal);

        // Then
        assertThat(result.isActive()).isTrue();
    }

    private Example createExample(Long id, Long userId, String name) {
        return Example.builder()
            .id(id)
            .userId(userId)
            .name(name)
            .description("Description")
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}
