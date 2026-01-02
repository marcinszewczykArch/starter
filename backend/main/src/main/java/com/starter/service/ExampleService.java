package com.starter.service;

import com.starter.domain.Example;
import com.starter.dto.CreateExampleRequest;
import com.starter.dto.ExampleDto;
import com.starter.repository.ExampleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service layer for Example operations. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleRepository exampleRepository;

    /** Get all examples. */
    public List<ExampleDto> getAllExamples() {
        log.debug("Fetching all examples");
        return exampleRepository.findAll().stream().map(this::toDto).toList();
    }

    /** Create a new example. */
    public ExampleDto createExample(CreateExampleRequest request) {
        log.info("Creating example: {}", request.getName());
        Example example =
                Example.builder()
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
                .name(example.getName())
                .description(example.getDescription())
                .active(example.isActive())
                .createdAt(example.getCreatedAt())
                .updatedAt(example.getUpdatedAt())
                .build();
    }
}
