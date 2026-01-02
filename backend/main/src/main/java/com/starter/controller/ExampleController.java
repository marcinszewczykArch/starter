package com.starter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starter.dto.CreateExampleRequest;
import com.starter.dto.ExampleDto;
import com.starter.service.ExampleService;

import jakarta.validation.Valid;

import java.util.List;

/** REST controller for Example operations. */
@RestController
@RequestMapping("/api/v1/example")
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example API endpoints")
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping
    @Operation(summary = "Get all examples", description = "Retrieves a list of all examples")
    @ApiResponses(
        value = {
                @ApiResponse(responseCode = "200", description = "Successfully retrieved examples")
        })
    public ResponseEntity<List<ExampleDto>> getAllExamples() {
        List<ExampleDto> examples = exampleService.getAllExamples();
        return ResponseEntity.ok(examples);
    }

    @PostMapping
    @Operation(summary = "Create example", description = "Creates a new example")
    @ApiResponses(
        value = {
                @ApiResponse(responseCode = "201", description = "Example created"),
                @ApiResponse(responseCode = "400", description = "Invalid request")
        })
    public ResponseEntity<ExampleDto> createExample(
        @Valid @RequestBody CreateExampleRequest request
    ) {
        ExampleDto created = exampleService.createExample(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
