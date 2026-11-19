
package io.github.ashan.hashdoslab.protection.request;

import io.github.ashan.hashdoslab.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@Import(RequestBodySizeLimitFilter.class)
class RequestBodySizeLimitFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalysisService analysisService;

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/analysis/compare-engines";

    private static final String VALID_PAYLOAD =
            """
            {
                "keyCount": 100
            }
            """;

    @Test
    void shouldAllowRequestWithinBodySizeLimit() throws Exception {

        mockMvc.perform(
                        post(PROTECTED_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_PAYLOAD)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectRequestWhenBodySizeExceedsLimit() throws Exception {

        String oversizedPayload = "a".repeat(1025);

        mockMvc.perform(
                        post(PROTECTED_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(oversizedPayload)
                )
                .andExpect(status().is(413))
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.valueOf("application/problem+json")
                ))
                .andExpect(jsonPath("$.type").value(
                        "https://hashdoslab.dev/problems/payload-too-large"
                ))
                .andExpect(jsonPath("$.title").value("Payload Too Large"))
                .andExpect(jsonPath("$.status").value(413));
    }


    @Test
    void shouldRejectOversizedRequestWithoutContentLengthHeader()
            throws Exception {

        String oversizedPayload = "a".repeat(1025);

        mockMvc.perform(
                        post(PROTECTED_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("Transfer-Encoding", "chunked")
                                .content(oversizedPayload)
                )
                .andExpect(status().is(413))
                .andExpect(jsonPath("$.status").value(413));
    }


}
