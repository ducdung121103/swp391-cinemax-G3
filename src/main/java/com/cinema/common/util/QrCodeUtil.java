package com.cinema.common.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.EnumMap;
import java.util.Map;

/**
 * Sinh ảnh QR PNG phục vụ e-ticket (M-09.1).
 */
public final class QrCodeUtil {

    private static final int DEFAULT_SIZE = 240;

    private QrCodeUtil() {}

    public static String toPngBase64(String content) {
        return toPngBase64(content, DEFAULT_SIZE);
    }

    public static String toPngBase64(String content, int size) {
        byte[] png = toPngBytes(content, size);
        return png == null ? null : Base64.getEncoder().encodeToString(png);
    }

    public static byte[] toPngBytes(String content, int size) {
        if (content == null || content.isBlank()) return null;
        int qrSize = size <= 0 ? DEFAULT_SIZE : size;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix matrix = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, qrSize, qrSize, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /** Payload chuẩn để quầy/soát vé tra cứu theo mã đặt vé. */
    public static String bookingPayload(String bookingCode) {
        if (bookingCode == null || bookingCode.isBlank()) return null;
        return "CINEMAX|BOOKING|" + bookingCode.trim();
    }
}
