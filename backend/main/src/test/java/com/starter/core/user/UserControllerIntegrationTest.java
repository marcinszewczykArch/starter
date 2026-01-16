package com.starter.core.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.starter.BaseIntegrationTest;
import com.starter.core.security.JwtUtil;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

/** Integration tests for UserController. */
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @org.springframework.beans.factory.annotation.Autowired
    private UserService userService;

    @org.springframework.beans.factory.annotation.Autowired
    private JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.port = port;
    }

    private String createUserAndGetToken(String email, String password) {
        String hashedPassword = passwordEncoder.encode(password);
        User user = userService.createUser(email, hashedPassword, User.Role.USER);
        return jwtUtil.generateToken(user);
    }

    @Test
    void getProfile_shouldReturnUserProfile_whenAuthenticated() {
        // given
        String token = createUserAndGetToken("profile@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/users/me/profile")
            .then()
            .statusCode(200)
            .body("id", notNullValue())
            .body("email", equalTo("profile@example.com"))
            .body("displayName", nullValue())
            .body("bio", nullValue());
    }

    @Test
    void getProfile_shouldReturn401_whenNotAuthenticated() {
        given()
            .when()
            .get("/api/users/me/profile")
            .then()
            .statusCode(401);
    }

    @Test
    void updateProfile_shouldUpdateAllFields() {
        // given
        String token = createUserAndGetToken("update@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "displayName": "John Doe",
                        "bio": "Software developer",
                        "website": "https://johndoe.com",
                        "company": "Tech Corp",
                        "location": "Warsaw",
                        "country": "PL"
                    }
                    """
            )
            .when()
            .put("/api/users/me/profile")
            .then()
            .statusCode(200)
            .body("displayName", equalTo("John Doe"))
            .body("bio", equalTo("Software developer"))
            .body("website", equalTo("https://johndoe.com"))
            .body("company", equalTo("Tech Corp"))
            .body("location", equalTo("Warsaw"))
            .body("country", equalTo("PL"));
    }

    @Test
    void updateProfile_shouldNormalizeEmptyStringsToNull() {
        // given
        String token = createUserAndGetToken("normalize@example.com", "password123");

        // First set some values
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "displayName": "Test",
                        "bio": "Test bio"
                    }
                    """
            )
            .when()
            .put("/api/users/me/profile")
            .then()
            .statusCode(200);

        // Then clear them with empty strings
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "displayName": "",
                        "bio": "   "
                    }
                    """
            )
            .when()
            .put("/api/users/me/profile")
            .then()
            .statusCode(200)
            .body("displayName", nullValue())
            .body("bio", nullValue());
    }

    @Test
    void updateProfile_shouldReturnBadRequest_whenFieldTooLong() {
        // given
        String token = createUserAndGetToken("validation@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "displayName": "%s"
                    }
                    """.formatted("a".repeat(101)) // 101 characters - exceeds max of 100
            )
            .when()
            .put("/api/users/me/profile")
            .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"))
            .body("details", hasKey("displayName"));
    }

    @Test
    void uploadAvatar_shouldSaveAvatar() throws IOException {
        // given
        String token = createUserAndGetToken("avatar@example.com", "password123");
        byte[] imageBytes = createTestImageBytes();

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("multipart/form-data")
            .multiPart("file", "avatar.jpg", imageBytes, "image/jpeg")
            .when()
            .post("/api/users/me/avatar")
            .then()
            .statusCode(200)
            .body("message", equalTo("Avatar uploaded successfully"));
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_whenFileTooLarge() {
        // given
        String token = createUserAndGetToken("largeavatar@example.com", "password123");
        // Create a file just over 5MB limit (5.1MB) - exceeds our validation limit
        byte[] largeBytes = new byte[(int) (5.1 * 1024 * 1024)]; // 5.1MB - exceeds 5MB limit

        // when & then
        // Our validation should catch it and return 400 Bad Request with proper error message
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("multipart/form-data")
            .multiPart("file", "large.jpg", largeBytes, "image/jpeg")
            .when()
            .post("/api/users/me/avatar")
            .then()
            .statusCode(400)
            .body("error", equalTo("FILE_TOO_LARGE"))
            .body("message", org.hamcrest.Matchers.containsString("exceeds maximum allowed size"));
    }

    @Test
    void uploadAvatar_shouldReturnBadRequest_whenNotAnImage() {
        // given
        String token = createUserAndGetToken("notimage@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("multipart/form-data")
            .multiPart("file", "test.txt", "not an image".getBytes(), "text/plain")
            .when()
            .post("/api/users/me/avatar")
            .then()
            .statusCode(400);
    }

    @Test
    void deleteAvatar_shouldRemoveAvatar() throws IOException {
        // given
        String token = createUserAndGetToken("deleteavatar@example.com", "password123");
        byte[] imageBytes = createTestImageBytes();

        // Upload avatar first
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("multipart/form-data")
            .multiPart("file", "avatar.jpg", imageBytes, "image/jpeg")
            .when()
            .post("/api/users/me/avatar")
            .then()
            .statusCode(200);

        // when & then - delete avatar
        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/api/users/me/avatar")
            .then()
            .statusCode(200)
            .body("message", equalTo("Avatar deleted successfully"));
    }

    @Test
    void getAvatar_shouldReturnAvatar_whenExists() throws IOException {
        // given
        String token = createUserAndGetToken("getavatar@example.com", "password123");
        byte[] imageBytes = createTestImageBytes();

        // Upload avatar
        given()
            .header("Authorization", "Bearer " + token)
            .contentType("multipart/form-data")
            .multiPart("file", "avatar.jpg", imageBytes, "image/jpeg")
            .when()
            .post("/api/users/me/avatar")
            .then()
            .statusCode(200);

        // Get user ID from profile
        Integer userIdInt = given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/users/me/profile")
            .then()
            .extract()
            .jsonPath()
            .getInt("id");
        Long userId = userIdInt.longValue();

        // when & then - get avatar (requires authentication)
        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/users/" + userId + "/avatar")
            .then()
            .statusCode(200)
            .contentType(org.hamcrest.Matchers.containsString("image/jpeg"));
    }

    @Test
    void getAvatar_shouldReturn404_whenNoAvatar() {
        // given
        String token = createUserAndGetToken("noavatar@example.com", "password123");

        // Get user ID from profile
        Integer userIdInt = given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/users/me/profile")
            .then()
            .extract()
            .jsonPath()
            .getInt("id");
        Long userId = userIdInt.longValue();

        // when & then - requires authentication
        given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/api/users/" + userId + "/avatar")
            .then()
            .statusCode(404);
    }

    @Test
    void changeEmail_shouldSendVerificationEmail() {
        // given
        String token = createUserAndGetToken("changeemail@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "newEmail": "newemail@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/users/me/change-email")
            .then()
            .statusCode(200)
            .body("message", equalTo("Verification email sent to newemail@example.com"));
    }

    @Test
    void changeEmail_shouldReturnUnauthorized_whenPasswordIncorrect() {
        // given
        String token = createUserAndGetToken("wrongpass@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "newEmail": "newemail@example.com",
                        "password": "wrongpassword"
                    }
                    """
            )
            .when()
            .post("/api/users/me/change-email")
            .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    void changeEmail_shouldReturnBadRequest_whenEmailInvalid() {
        // given
        String token = createUserAndGetToken("invalidemail@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "newEmail": "not-an-email",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/users/me/change-email")
            .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"));
    }

    @Test
    void changeEmail_shouldReturnBadRequest_whenSameEmail() {
        // given
        String token = createUserAndGetToken("sameemail@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "newEmail": "sameemail@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/users/me/change-email")
            .then()
            .statusCode(400)
            .body("error", equalTo("INVALID_ARGUMENT"))
            .body("message", equalTo("New email must be different from current email"));
    }

    @Test
    void deleteAccount_shouldArchiveUser() {
        // given
        String token = createUserAndGetToken("delete@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "password": "password123"
                    }
                    """
            )
            .when()
            .delete("/api/users/me")
            .then()
            .statusCode(200)
            .body("message", equalTo("Account deleted successfully"));

        // Verify user is archived - should not be able to login
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "email": "delete@example.com",
                        "password": "password123"
                    }
                    """
            )
            .when()
            .post("/api/auth/login")
            .then()
            .statusCode(401);
    }

    @Test
    void deleteAccount_shouldReturnUnauthorized_whenPasswordIncorrect() {
        // given
        String token = createUserAndGetToken("deletewrong@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(
                """
                    {
                        "password": "wrongpassword"
                    }
                    """
            )
            .when()
            .delete("/api/users/me")
            .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_CREDENTIALS"));
    }

    @Test
    void deleteAccount_shouldReturnBadRequest_whenPasswordMissing() {
        // given
        String token = createUserAndGetToken("deletemissing@example.com", "password123");

        // when & then
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{}")
            .when()
            .delete("/api/users/me")
            .then()
            .statusCode(400)
            .body("error", equalTo("VALIDATION_ERROR"));
    }

    /**
     * Create a simple test image as byte array.
     */
    private byte[] createTestImageBytes() throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return baos.toByteArray();
    }
}
