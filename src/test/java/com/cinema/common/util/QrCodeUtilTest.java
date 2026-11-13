package com.cinema.common.util;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeUtilTest {

    @Test
    void bookingPayload_buildsCanonicalContent() {
        assertEquals("CINEMAX|BOOKING|BK001", QrCodeUtil.bookingPayload("BK001"));
        assertNull(QrCodeUtil.bookingPayload(" "));
    }

    @Test
    void toPngBase64_returnsDecodablePng() {
        String base64 = QrCodeUtil.toPngBase64(QrCodeUtil.bookingPayload("BK-DEMO-001"), 180);
        assertNotNull(base64);
        byte[] png = Base64.getDecoder().decode(base64);
        assertTrue(png.length > 100);
        // PNG magic bytes
        assertEquals((byte) 0x89, png[0]);
        assertEquals((byte) 0x50, png[1]);
        assertEquals((byte) 0x4E, png[2]);
        assertEquals((byte) 0x47, png[3]);
    }
}
