
package io.github.ashan.hashdoslab.protection.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RateLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/analysis/compare-engines";

    private static final String VALID_PAYLOAD =
            """
            {
                "keyCount": 1
            }
            """;

    @BeforeEach
    void setUp() {
        // Each test should use a separate client IP.
    }

    @Test
    void shouldAllowRequestsWithinRateLimit() throws Exception {

        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                    post(PROTECTED_ENDPOINT)
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.10");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_PAYLOAD)
            ).andExpect(status().isOk());
        }
    }

    @Test
    void shouldRejectRequestWhenRateLimitExceeded() throws Exception {

        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                    post(PROTECTED_ENDPOINT)
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.20");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_PAYLOAD)
            ).andExpect(status().isOk());
        }

        mockMvc.perform(
                        post(PROTECTED_ENDPOINT)
                                .with(request -> {
                                    request.setRemoteAddr("192.0.2.20");
                                    return request;
                                })
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_PAYLOAD)
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(content().contentTypeCompatibleWith(
                        "application/problem+json"
                ))
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.title").value("Too Many Requests"));
    }

    @Test
    void shouldAllowRequestsFromDifferentClients() throws Exception {

        for (int i = 0; i < 5; i++) {

            mockMvc.perform(
                    post(PROTECTED_ENDPOINT)
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.30");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_PAYLOAD)
            ).andExpect(status().isOk());
        }

        mockMvc.perform(
                post(PROTECTED_ENDPOINT)
                        .with(request -> {
                            request.setRemoteAddr("192.0.2.31");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_PAYLOAD)
        ).andExpect(status().isOk());
    }

    @Test
    void shouldNotApplyRateLimitToOtherEndpoints() throws Exception {

        for (int i = 0; i < 6; i++) {

            mockMvc.perform(
                    get("/api/v1/non-existing-endpoint")
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.40");
                                return request;
                            })
            ).andExpect(status().isNotFound());
        }
    }

    @Test
    void shouldNotApplyRateLimitToNonPostRequests() throws Exception {

        for (int i = 0; i < 6; i++) {

            mockMvc.perform(
                    get(PROTECTED_ENDPOINT)
                            .with(request -> {
                                request.setRemoteAddr("192.0.2.50");
                                return request;
                            })
            ).andExpect(status().isMethodNotAllowed());
        }
    }
}
