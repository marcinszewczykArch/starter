package com.starter.feature.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.starter.core.security.UserPrincipal;
import com.starter.feature.example.dto.CreateExampleRequest;
import com.starter.feature.example.dto.ExampleDto;

import jakarta.validation.Valid;

import java.util.List;

/** REST controller for Example operations. */
@RestController
@RequestMapping("/api/v1/example")
@RequiredArgsConstructor
@Tag(name = "Example", description = "Example API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping
    @Operation(
        summary = "Get examples",
        description = "Retrieves examples. Admin sees all, regular users see only their own."
    )
    @ApiResponses(
        value = {@ApiResponse(responseCode = "200", description = "Successfully retrieved examples")}
    )
    public ResponseEntity<List<ExampleDto>> getExamples(
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<ExampleDto> examples = exampleService.getExamples(principal);
        return ResponseEntity.ok(examples);
    }

    @PostMapping
    @Operation(summary = "Create example", description = "Creates a new example for the current user")
    @ApiResponses(
        value = {
                @ApiResponse(responseCode = "201", description = "Example created"),
                @ApiResponse(responseCode = "400", description = "Invalid request")
        }
    )
    public ResponseEntity<ExampleDto> createExample(
        @Valid @RequestBody CreateExampleRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        ExampleDto created = exampleService.createExample(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
