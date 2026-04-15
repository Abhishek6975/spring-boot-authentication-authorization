package com.koyta.auth.filter.wrapper;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachedBodyHttpServletRequestTest {

    @Test
    void shouldReadBodyFromInputStream() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        String body = "{\"key\":\"value\"}";
        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, body);

        ServletInputStream inputStream = wrapper.getInputStream();

        StringBuilder result = new StringBuilder();
        int ch;
        while ((ch = inputStream.read()) != -1) {
            result.append((char) ch);
        }

        assertEquals(body, result.toString());
    }

    @Test
    void shouldReturnReaderContent() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        String body = "test-body";
        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, body);

        BufferedReader reader = wrapper.getReader();

        assertEquals(body, reader.readLine());
    }

    //  IMPORTANT: cover isFinished()
    @Test
    void shouldCheckStreamFinished() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, "abc");

        ServletInputStream stream = wrapper.getInputStream();

        while (stream.read() != -1) {}

        assertTrue(stream.isFinished());
    }

    //  cover isReady()
    @Test
    void shouldCheckStreamReady() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, "abc");

        assertTrue(wrapper.getInputStream().isReady());
    }

    //  cover setReadListener()
    @Test
    void shouldCallSetReadListener() {

        HttpServletRequest request = mock(HttpServletRequest.class);

        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, "abc");

        wrapper.getInputStream().setReadListener(null); // just call
    }

    @Test
    void shouldReturnFalseWhenStreamNotFinished() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        String body = "test-body";

        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, body);

        ServletInputStream inputStream = wrapper.getInputStream();

        // read NOT called → data still available
        boolean result = inputStream.isFinished();

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueWhenStreamFinished() throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);

        String body = "test-body";

        CachedBodyHttpServletRequest wrapper =
                new CachedBodyHttpServletRequest(request, body);

        ServletInputStream inputStream = wrapper.getInputStream();

        // consume full stream
        while (inputStream.read() != -1) {
            // read all bytes
        }

        boolean result = inputStream.isFinished();

        assertTrue(result);
    }


}
