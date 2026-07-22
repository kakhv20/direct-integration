package ge.xcoder.playcore.direct_integration.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class CachedBodyHttpServletRequestTest {
    public static final String TEXT;
    public static final byte[] BODY_IN_BYTES;

    static {
        TEXT = "I am so happy to see you";
        BODY_IN_BYTES = TEXT.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void getInputStream_returnsSameBody() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(BODY_IN_BYTES);

        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);

        assertArrayEquals(BODY_IN_BYTES, cached.getInputStream().readAllBytes());
        assertArrayEquals(BODY_IN_BYTES, cached.getInputStream().readAllBytes());
    }

    @Test
    void getReader_returnsSameResult() throws IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContent(BODY_IN_BYTES);

        CachedBodyHttpServletRequest cached = new CachedBodyHttpServletRequest(request);
        Assertions.assertEquals(TEXT, cached.getReader().lines().collect(Collectors.joining("\n")));
        Assertions.assertEquals(TEXT, cached.getReader().lines().collect(Collectors.joining("\n")));
    }
}
