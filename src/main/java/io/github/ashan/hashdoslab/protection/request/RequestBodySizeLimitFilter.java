
package io.github.ashan.hashdoslab.protection.request;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class RequestBodySizeLimitFilter extends OncePerRequestFilter {

    private static final String PROTECTED_ENDPOINT =
            "/api/v1/analysis/compare-engines";

    private static final int MAX_REQUEST_BODY_SIZE = 1024;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String requestPath = request.getRequestURI()
                .substring(request.getContextPath().length());

        return !PROTECTED_ENDPOINT.equals(requestPath)
                || !"POST".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        byte[] requestBody = request.getInputStream()
                .readNBytes(MAX_REQUEST_BODY_SIZE + 1);

        if (requestBody.length > MAX_REQUEST_BODY_SIZE) {
            writePayloadTooLargeResponse(response);
            return;
        }

        HttpServletRequest wrappedRequest =
                new CachedBodyHttpServletRequest(request, requestBody);

        filterChain.doFilter(wrappedRequest, response);
    }

    private void writePayloadTooLargeResponse(
            HttpServletResponse response
    ) throws IOException {

        response.setStatus(413);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.getWriter().write("""
                {
                    "type": "https://hashdoslab.dev/problems/payload-too-large",
                    "title": "Payload Too Large",
                    "status": 413,
                    "detail": "Request body exceeds the maximum allowed size of 1024 bytes."
                }
                """);
    }

    private static class CachedBodyHttpServletRequest
            extends HttpServletRequestWrapper {

        private final byte[] cachedBody;

        CachedBodyHttpServletRequest(
                HttpServletRequest request,
                byte[] cachedBody
        ) {
            super(request);
            this.cachedBody = cachedBody;
        }

        @Override
        public ServletInputStream getInputStream() {

            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(cachedBody);

            return new ServletInputStream() {

                @Override
                public int read() {
                    return inputStream.read();
                }

                @Override
                public boolean isFinished() {
                    return inputStream.available() == 0;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException(
                            "Non-blocking request reading is not supported."
                    );
                }
            };
        }

        @Override
        public BufferedReader getReader() throws UnsupportedEncodingException {

            return new BufferedReader(
                    new InputStreamReader(
                            getInputStream(),
                            getCharacterEncoding() != null
                                    ? getCharacterEncoding()
                                    : StandardCharsets.UTF_8.name()
                    )
            );
        }
    }
}
