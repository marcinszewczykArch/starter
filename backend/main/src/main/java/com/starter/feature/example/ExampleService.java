package com.starter.feature.example;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.starter.core.security.UserPrincipal;
import com.starter.core.user.User;
import com.starter.feature.example.dto.CreateExampleRequest;
import com.starter.feature.example.dto.ExampleDto;

import java.util.List;

/** Service layer for Example operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    /**
     * Get examples based on user role. ADMIN sees all examples, USER sees only their own.
     *
     * @param principal the authenticated user
     * @return list of examples visible to the user
     */
    public List<ExampleDto> getExamples(UserPrincipal principal) {
        if (principal.getRole() == User.Role.ADMIN) {
            log.debug("Admin {} fetching all examples", principal.getEmail());
            return exampleRepository.findAll().stream().map(this::toDto).toList();
        } else {
            log.debug("User {} fetching their examples", principal.getEmail());
            return exampleRepository.findByUserId(principal.getId()).stream().map(this::toDto).toList();
        }
    }

    /**
     * Create a new example for the authenticated user.
     *
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created example
     */
    public ExampleDto createExample(CreateExampleRequest request, UserPrincipal principal) {
        log.info("User {} creating example: {}", principal.getEmail(), request.getName());
        Example example =
            Example.builder()
                .userId(principal.getId())
                .name(request.getName())
                .description(request.getDescription())
                .active(true)
                .build();
        Example saved = exampleRepository.save(example);
        return toDto(saved);
    }

    private ExampleDto toDto(Example example) {
        return ExampleDto.builder()
            .id(example.getId())
            .userId(example.getUserId())
            .name(example.getName())
            .description(example.getDescription())
            .active(example.isActive())
            .createdAt(example.getCreatedAt())
            .updatedAt(example.getUpdatedAt())
            .build();
    }
}
