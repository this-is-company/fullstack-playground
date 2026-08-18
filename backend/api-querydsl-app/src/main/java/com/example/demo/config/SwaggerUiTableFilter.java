package com.example.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/** Swagger index.html 에 표 CSS/JS 를 넣는다. */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class SwaggerUiTableFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri == null || !uri.contains("/swagger-ui/") || !uri.endsWith("index.html");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        BufferingResponse wrapper = new BufferingResponse(response);
        filterChain.doFilter(request, wrapper);
        String html = wrapper.captured();
        if (html.contains("</head>") && !html.contains("swagger-custom.css")) {
            html = html.replace(
                    "</head>",
                    "<link rel=\"stylesheet\" href=\"/swagger-custom.css\">"
                            + "<script defer src=\"/swagger-custom.js\"></script></head>"
            );
        }
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        response.setContentLength(bytes.length);
        response.getOutputStream().write(bytes);
    }

    private static final class BufferingResponse extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        private final PrintWriter writer = new PrintWriter(buffer, true, StandardCharsets.UTF_8);

        private BufferingResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public PrintWriter getWriter() {
            return writer;
        }

        @Override
        public jakarta.servlet.ServletOutputStream getOutputStream() {
            return new jakarta.servlet.ServletOutputStream() {
                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                }

                @Override
                public void write(int b) {
                    buffer.write(b);
                }
            };
        }

        private String captured() {
            writer.flush();
            return buffer.toString(StandardCharsets.UTF_8);
        }
    }
}
