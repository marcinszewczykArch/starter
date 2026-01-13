package com.starter.core.auth;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.starter.BaseIntegrationTest;

/** Integration tests for AuthController. */
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
    }

    @Test
    void register_shouldCreateUserAndReturnToken() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "newuser@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(201)
            .body("token", notNullValue())
            .body("email", equalTo("newuser@example.com"))
            .body("role", equalTo("USER"))
            .body("userId", notNullValue());
    }

    @Test
    void register_shouldReturnConflict_whenEmailExists() {
        // First registration
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "duplicate@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(201);

        // Second registration with same email
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "duplicate@example.com",
                        "password": "different123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(409)
            .body("error", equalTo("EMAIL_ALREADY_EXISTS"))
            .body("message", notNullValue());
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordTooShort() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "test@example.com",
                        "password": "short"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"))
            .body("details", hasKey("password"));
    }

    @Test
    void register_shouldReturnBadRequest_whenInvalidEmail() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "not-an-email",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"))
            .body("details", hasKey("email"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() {
        // First register a user
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "login@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(201);

        // Then login
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "login@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("email", equalTo("login@example.com"))
            .body("role", equalTo("USER"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenUserNotFound() {
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "nonexistent@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    void login_shouldReturnUnauthorized_whenPasswordWrong() {
        // First register a user
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "wrongpass@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(201);

        // Then try to login with wrong password
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "wrongpass@example.com",
                        "password": "wrongpassword"
                    }
                    """
            )
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    void login_shouldWorkWithDifferentEmailCase() {
        // Register with lowercase
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "casetest@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/register")
            .then()
            .statusCode(201);

        // Login with UPPERCASE - should work due to email normalization
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "CASETEST@EXAMPLE.COM",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("email", equalTo("casetest@example.com"));
    }

    @Test
    void me_shouldReturnCurrentUser_whenAuthenticated() {
        // First register and get token
        String token =
            given()
                .contentType(ContentType.JSON)
                .body(
                    """
                        {
                            "email": "metest@example.com",
                            "password": "password123"
                        }
                        """
                )
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .extract()
                .path("token");

        // Use token to access /me endpoint
        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/auth/me")
            .then()
            .statusCode(200)
            .body("email", equalTo("metest@example.com"))
            .body("role", equalTo("USER"))
            .body("id", notNullValue());
    }

    @Test
    void me_shouldReturn401_whenNoToken() {
        given()
            .when()
            .get("/api/auth/me")
            .then()
            .statusCode(401);
    }

    @Test
    void me_shouldReturn401_whenInvalidToken() {
        given()
            .header("Authorization", "Bearer invalid-token")
            .when()
            .get("/api/auth/me")
            .then()
            .statusCode(401);
    }
}
