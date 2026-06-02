package com.shopee.banking.bams.adapter.filter;

import com.shopee.banking.bams.common.exception.enums.AuthErrorCode;
import com.shopee.banking.bams.common.util.JwtUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final Long ADMIN_ID = 1L;
    private static final String USERNAME = "test-admin";
    private static final String JWT_SECRET = "jwt-filter-secret";
    private static final String OTHER_SECRET = "some-other-secret";

    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter();
        ReflectionTestUtils.setField(jwtAuthFilter, "jwtSecret", JWT_SECRET);
    }

    @Test
    @DisplayName("filter rejects requests without an Authorization header")
    void doFilter_missingAuthorizationHeader_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects requests without a Bearer token")
    void doFilter_nonBearerAuthorizationHeader_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects requests with a blank Bearer token")
    void doFilter_blankBearerToken_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer ");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects requests with an invalid role in the JWT")
    void doFilter_invalidRoleToken_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(JWT_SECRET, Instant.now().plusSeconds(300), "customer"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects requests with an expired JWT")
    void doFilter_expiredToken_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + createToken(JWT_SECRET, Instant.now().minusSeconds(60), JwtUtils.ROLE_ADMIN));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects requests with a JWT signed by a different secret")
    void doFilter_wrongSecretToken_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + JwtUtils.createAccessToken(ADMIN_ID, USERNAME, OTHER_SECRET, 300));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter rejects malformed JWT values")
    void doFilter_malformedToken_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer not-a-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("filter allows requests to protected endpoints with a valid token")
    void doFilter_validToken_allowsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + JwtUtils.createAccessToken(ADMIN_ID, USERNAME, JWT_SECRET, 300));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertEquals(200, response.getStatus());
        assertEquals("", response.getContentAsString());
    }

    @Test
    @DisplayName("filter rejects requests with lowercase bearer prefix")
    void doFilter_lowercaseBearerPrefix_rejectsRequest() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "bearer " + JwtUtils.createAccessToken(ADMIN_ID, USERNAME, JWT_SECRET, 300));
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertUnauthorized(response);
        verify(filterChain, never()).doFilter(request, response);
    }

    private void assertUnauthorized(MockHttpServletResponse response) throws UnsupportedOperationException, IOException {
        assertEquals(401, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertTrue(response.getContentAsString().contains(String.valueOf(AuthErrorCode.INVALID_ACCESS_TOKEN.getCode())));
        assertTrue(response.getContentAsString().contains(AuthErrorCode.INVALID_ACCESS_TOKEN.getMsg()));
    }

    private String createToken(String secret, Instant expiresAt, String role) {
        return Jwts.builder()
                .subject(String.valueOf(ADMIN_ID))
                .claim(JwtUtils.ROLE_CLAIM, role)
                .claim(JwtUtils.USERNAME_CLAIM, USERNAME)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey(secret))
                .compact();
    }

    private SecretKey signingKey(String secret) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
