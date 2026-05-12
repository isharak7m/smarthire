package com.smarthire;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarthire.model.Employee;
import com.smarthire.model.User;
import com.smarthire.repository.EmployeeRepository;
import com.smarthire.repository.UserRepository;
import com.smarthire.security.JwtUtil;
import com.smarthire.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SmartHire Integration & Unit Tests
 * Demonstrates: JUnit 5, MockMvc, Spring Boot Test, H2 in-memory DB,
 * JWT token generation in tests, proper test isolation with @BeforeEach.
 *
 * Test 1: JWT token generation and validation (unit test)
 * Test 2: Auth endpoint — register + login returns valid JWT (integration test)
 * Test 3: Employee CRUD with JWT auth — create and retrieve (integration test)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SmarthireApplicationTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private UserRepository userRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EmployeeService employeeService;

    private String jwtToken;
    private static final String TEST_USER = "testuser_" + System.currentTimeMillis();
    private static final String TEST_PASS = "Test@1234";
    private static final String TEST_EMAIL = TEST_USER + "@test.com";

    @BeforeEach
    void setUp() {
        // Create a test user and generate a JWT token for authenticated requests
        if (!userRepository.existsByUsername(TEST_USER)) {
            User user = new User();
            user.setUsername(TEST_USER);
            user.setEmail(TEST_EMAIL);
            user.setPassword(passwordEncoder.encode(TEST_PASS));
            user.setRole("ROLE_USER");
            userRepository.save(user);
        }

        // Generate JWT for the test user
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                TEST_USER, TEST_PASS,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        jwtToken = jwtUtil.generateToken(userDetails);
    }

    // ════════════════════════════════════════════════════════════
    // TEST 1: JWT Token — unit test of JwtUtil directly
    // ════════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 1: JWT token is generated, username extracted correctly, and validated")
    void testJwtTokenGenerationAndValidation() {
        // Arrange
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "jwtTestUser", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));

        // Act
        String token = jwtUtil.generateToken(userDetails);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts: header.payload.signature

        String extractedUsername = jwtUtil.extractUsername(token);
        assertThat(extractedUsername).isEqualTo("jwtTestUser");

        boolean isValid = jwtUtil.isTokenValid(token, userDetails);
        assertThat(isValid).isTrue();

        // Tampered token should be invalid
        String tamperedToken = token + "tampered";
        try {
            jwtUtil.extractUsername(tamperedToken);
            // If no exception thrown, this should fail the test
            assertThat(false).as("Tampered token should have thrown an exception").isTrue();
        } catch (Exception e) {
            // Expected — tampered token is invalid
            assertThat(e).isNotNull();
        }
    }

    // ════════════════════════════════════════════════════════════
    // TEST 2: Auth Controller — register + login integration test
    // ════════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 2: POST /api/auth/register creates user; POST /api/auth/login returns JWT")
    void testRegisterAndLogin() throws Exception {
        String uniqueUser = "newuser_" + System.currentTimeMillis();

        // ── Register ──────────────────────────────────────
        Map<String, String> registerBody = Map.of(
                "username", uniqueUser,
                "email", uniqueUser + "@test.com",
                "password", "NewPass@123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(uniqueUser))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));

        // ── Login with same credentials ───────────────────
        Map<String, String> loginBody = Map.of(
                "username", uniqueUser,
                "password", "NewPass@123"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value(uniqueUser));

        // ── Wrong password returns 401 ────────────────────
        Map<String, String> wrongPassBody = Map.of(
                "username", uniqueUser,
                "password", "WrongPassword"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid username or password"));
    }

    // ════════════════════════════════════════════════════════════
    // TEST 3: Employee CRUD — authenticated integration test
    // ════════════════════════════════════════════════════════════
    @Test
    @DisplayName("TEST 3: POST /api/employees creates employee; GET /api/employees returns paginated list")
    void testEmployeeCreateAndRetrieve() throws Exception {
        String uniqueEmail = "emp_" + System.currentTimeMillis() + "@test.com";

        // ── Create employee (requires JWT) ────────────────
        Map<String, Object> empBody = Map.of(
                "fullName", "Test Employee",
                "email", uniqueEmail,
                "role", "QA Engineer",
                "status", "PENDING"
        );

        String responseJson = mockMvc.perform(post("/api/employees")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(empBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fullName").value("Test Employee"))
                .andExpect(jsonPath("$.email").value(uniqueEmail))
                .andExpect(jsonPath("$.avatarInitials").value("TE")) // generated initials
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(responseJson).get("id").asLong();

        // ── Retrieve paginated list ────────────────────────
        mockMvc.perform(get("/api/employees?page=0&size=10")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable").exists());

        // ── Retrieve by ID ────────────────────────────────
        mockMvc.perform(get("/api/employees/" + createdId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.fullName").value("Test Employee"));

        // ── Unauthenticated request returns 403 ──────────
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());

        // ── Search works ──────────────────────────────────
        mockMvc.perform(get("/api/employees?search=Test Employee")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Test Employee"));

        // ── Cleanup: delete employee ──────────────────────
        mockMvc.perform(delete("/api/employees/" + createdId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }
}
