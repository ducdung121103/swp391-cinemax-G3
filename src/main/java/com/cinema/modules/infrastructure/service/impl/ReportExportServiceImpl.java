package com.cinema.modules.infrastructure.service.impl;

import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.dto.RevenueTimeSeriesDTO;
import com.cinema.modules.infrastructure.service.ReportExportService;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of ReportExportService using Apache POI and OpenPDF.
 */
public class ReportExportServiceImpl implements ReportExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportExportServiceImpl.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DecimalFormat CURRENCY_FMT = new DecimalFormat("#,##0 ₫");

    @Override
    public void exportRevenueExcel(RevenueFilterDTO filter,
                                   RevenueKpiDTO kpi,
                                   List<RevenueTimeSeriesDTO> timeSeries,
                                   List<RevenueBreakdownDTO> byMovie,
                                   List<RevenueBreakdownDTO> byCinema,
                                   List<RevenueBreakdownDTO> byChannel,
                                   List<BookingDetailDTO> bookings,
                                   OutputStream out) throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            DataFormat format = workbook.createDataFormat();

            // Styles
            CellStyle titleStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_RED.getIndex());
            titleStyle.setFont(titleFont);

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            setThinBorders(headerStyle);

            CellStyle dataStyle = workbook.createCellStyle();
            setThinBorders(dataStyle);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(format.getFormat("#,##0 \"₫\""));
            setThinBorders(currencyStyle);

            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.setDataFormat(format.getFormat("0.0%"));
            setThinBorders(percentStyle);

            CellStyle intStyle = workbook.createCellStyle();
            intStyle.setDataFormat(format.getFormat("#,##0"));
            setThinBorders(intStyle);

            // =================================================================
            // SHEET 1: TỔNG QUAN KPI
            // =================================================================
            Sheet kpiSheet = workbook.createSheet("Tổng Quan KPI");
            int rIdx = 0;

            Row titleRow = kpiSheet.createRow(rIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU & DOANH SỐ HỆ THỐNG CINEMAX");
            titleCell.setCellStyle(titleStyle);

            Row periodRow = kpiSheet.createRow(rIdx++);
            periodRow.createCell(0).setCellValue("Kỳ báo cáo: " + filter.getFrom().format(DATE_FMT) + " đến " + filter.getTo().format(DATE_FMT));
            Row exportedRow = kpiSheet.createRow(rIdx++);
            exportedRow.createCell(0).setCellValue("Thời điểm xuất file: " + LocalDateTime.now().format(DATETIME_FMT));
            rIdx++; // Empty row

            Row kpiHeader = kpiSheet.createRow(rIdx++);
            createCell(kpiHeader, 0, "Chỉ Số Đo Lường (KPI)", headerStyle);
            createCell(kpiHeader, 1, "Giá Trị Kỳ Này", headerStyle);
            createCell(kpiHeader, 2, "Tăng Trưởng Kỳ Trước", headerStyle);

            addKpiRow(kpiSheet, rIdx++, "Tổng Doanh Thu Toàn Hệ Thống", kpi.getTotalRevenue(), currencyStyle, null, percentStyle, dataStyle);
            kpiSheet.autoSizeColumn(0);
            kpiSheet.autoSizeColumn(1);
            kpiSheet.autoSizeColumn(2);

            // =================================================================
            // SHEET 2: THEO THỜI GIAN
            // =================================================================
            if (timeSeries != null && !timeSeries.isEmpty()) {
                Sheet tsSheet = workbook.createSheet("Theo Thời Gian");
                int tsRowIdx = 0;
                Row tsH = tsSheet.createRow(tsRowIdx++);
                createCell(tsH, 0, "Mốc Thời Gian", headerStyle);
                createCell(tsH, 1, "Tổng Doanh Thu", headerStyle);
                createCell(tsH, 2, "Doanh Thu Vé", headerStyle);
                createCell(tsH, 3, "Doanh Thu F&B", headerStyle);
                createCell(tsH, 4, "Số Đơn Hàng", headerStyle);
                createCell(tsH, 5, "Số Vé Bán", headerStyle);

                for (RevenueTimeSeriesDTO ts : timeSeries) {
                    Row row = tsSheet.createRow(tsRowIdx++);
                    createCell(row, 0, ts.getPeriodLabel(), dataStyle);
                    createNumericCell(row, 1, ts.getTotalRevenue(), currencyStyle);
                    createNumericCell(row, 2, ts.getTicketRevenue(), currencyStyle);
                    createNumericCell(row, 3, ts.getFnbRevenue(), currencyStyle);
                    createNumericCell(row, 4, BigDecimal.valueOf(ts.getBookingsCount()), intStyle);
                    createNumericCell(row, 5, BigDecimal.valueOf(ts.getTicketsCount()), intStyle);
                }
                for (int i = 0; i <= 5; i++) tsSheet.autoSizeColumn(i);
            }

            // =================================================================
            // SHEET 3: THEO PHIM
            // =================================================================
            if (byMovie != null && !byMovie.isEmpty()) {
                Sheet mSheet = workbook.createSheet("Theo Phim");
                int mRowIdx = 0;
                Row mH = mSheet.createRow(mRowIdx++);
                createCell(mH, 0, "Tên Phim", headerStyle);
                createCell(mH, 1, "Doanh Thu", headerStyle);
                createCell(mH, 2, "Tỷ Trọng (%)", headerStyle);
                createCell(mH, 3, "Số Đơn", headerStyle);
                createCell(mH, 4, "Số Vé Bán", headerStyle);

                for (RevenueBreakdownDTO m : byMovie) {
                    Row row = mSheet.createRow(mRowIdx++);
                    createCell(row, 0, m.getCategoryName(), dataStyle);
                    createNumericCell(row, 1, m.getRevenue(), currencyStyle);
                    createNumericCell(row, 2, BigDecimal.valueOf(m.getSharePercent() / 100.0), percentStyle);
                    createNumericCell(row, 3, BigDecimal.valueOf(m.getBookingsCount()), intStyle);
                    createNumericCell(row, 4, BigDecimal.valueOf(m.getTicketsSold()), intStyle);
                }
                for (int i = 0; i <= 4; i++) mSheet.autoSizeColumn(i);
            }

            // =================================================================
            // SHEET 4: THEO CỤM RẠP
            // =================================================================
            if (byCinema != null && !byCinema.isEmpty()) {
                Sheet cSheet = workbook.createSheet("Theo Cụm Rạp");
                int cRowIdx = 0;
                Row cH = cSheet.createRow(cRowIdx++);
                createCell(cH, 0, "Tên Cụm Rạp", headerStyle);
                createCell(cH, 1, "Doanh Thu", headerStyle);
                createCell(cH, 2, "Tỷ Trọng (%)", headerStyle);
                createCell(cH, 3, "Số Đơn", headerStyle);
                createCell(cH, 4, "Số Vé Bán", headerStyle);

                for (RevenueBreakdownDTO c : byCinema) {
                    Row row = cSheet.createRow(cRowIdx++);
                    createCell(row, 0, c.getCategoryName(), dataStyle);
                    createNumericCell(row, 1, c.getRevenue(), currencyStyle);
                    createNumericCell(row, 2, BigDecimal.valueOf(c.getSharePercent() / 100.0), percentStyle);
                    createNumericCell(row, 3, BigDecimal.valueOf(c.getBookingsCount()), intStyle);
                    createNumericCell(row, 4, BigDecimal.valueOf(c.getTicketsSold()), intStyle);
                }
                for (int i = 0; i <= 4; i++) cSheet.autoSizeColumn(i);
            }

            // =================================================================
            // SHEET 5: CHI TIẾT ĐƠN HÀNG
            // =================================================================
            if (bookings != null && !bookings.isEmpty()) {
                Sheet bSheet = workbook.createSheet("Chi Tiết Đơn Hàng");
                int bRowIdx = 0;
                Row bH = bSheet.createRow(bRowIdx++);
                createCell(bH, 0, "Mã Đơn", headerStyle);
                createCell(bH, 1, "Thời Gian", headerStyle);
                createCell(bH, 2, "Tên Phim", headerStyle);
                createCell(bH, 3, "Cụm Rạp", headerStyle);
                createCell(bH, 4, "Kênh", headerStyle);
                createCell(bH, 5, "Phương Thức", headerStyle);
                createCell(bH, 6, "Tiền Vé", headerStyle);
                createCell(bH, 7, "Tiền F&B", headerStyle);
                createCell(bH, 8, "Chiết Khấu", headerStyle);
                createCell(bH, 9, "Tổng Thanh Toán", headerStyle);

                for (BookingDetailDTO b : bookings) {
                    Row row = bSheet.createRow(bRowIdx++);
                    createCell(row, 0, b.getBookingCode(), dataStyle);
                    createCell(row, 1, b.getPaymentTime() != null ? b.getPaymentTime().format(DATETIME_FMT) : "", dataStyle);
                    createCell(row, 2, b.getMovieTitle(), dataStyle);
                    createCell(row, 3, b.getCinemaName(), dataStyle);
                    createCell(row, 4, b.getChannel(), dataStyle);
                    createCell(row, 5, b.getPaymentMethod(), dataStyle);
                    createNumericCell(row, 6, b.getTicketsAmount(), currencyStyle);
                    createNumericCell(row, 7, b.getFnbAmount(), currencyStyle);
                    createNumericCell(row, 8, b.getDiscountAmount(), currencyStyle);
                    createNumericCell(row, 9, b.getFinalAmount(), currencyStyle);
                }
                for (int i = 0; i <= 9; i++) bSheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    @Override
    public void exportRevenuePdf(RevenueFilterDTO filter,
                                 RevenueKpiDTO kpi,
                                 List<RevenueBreakdownDTO> byMovie,
                                 List<RevenueBreakdownDTO> byCinema,
                                 List<RevenueBreakdownDTO> byChannel,
                                 OutputStream out) throws Exception {

        Document document = new Document(PageSize.A4, 25, 25, 25, 25);
        PdfWriter.getInstance(document, out);
        document.open();

        Font fontTitle = getUnicodeFont(16, Font.BOLD, new Color(229, 9, 20));
        Font fontSubtitle = getUnicodeFont(10, Font.ITALIC, Color.DARK_GRAY);
        Font fontHeader = getUnicodeFont(10, Font.BOLD, Color.WHITE);
        Font fontBody = getUnicodeFont(9, Font.NORMAL, Color.BLACK);
        Font fontBodyBold = getUnicodeFont(9, Font.BOLD, Color.BLACK);

        // Header Title
        Paragraph title = new Paragraph("BÁO CÁO DOANH THU & DOANH SỐ HỆ THỐNG CINEMAX", fontTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph sub = new Paragraph("Kỳ báo cáo: " + filter.getFrom().format(DATE_FMT) + " đến " + filter.getTo().format(DATE_FMT) +
                "  |  Xuất lúc: " + LocalDateTime.now().format(DATETIME_FMT), fontSubtitle);
        sub.setAlignment(Element.ALIGN_CENTER);
        sub.setSpacingAfter(15);
        document.add(sub);

        // 1. KPI Table
        Paragraph kpiTitle = new Paragraph("1. CÁC CHỈ SỐ DOANH THU CỐT LÕI (KPIS)", fontBodyBold);
        kpiTitle.setSpacingAfter(6);
        document.add(kpiTitle);

        PdfPTable kpiTable = new PdfPTable(3);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{45, 35, 20});
        kpiTable.setSpacingAfter(15);

        addPdfHeaderCell(kpiTable, "Chỉ Số Đo Lường", fontHeader);
        addPdfHeaderCell(kpiTable, "Giá Trị Thực Đạt", fontHeader);
        addPdfHeaderCell(kpiTable, "Tăng Trưởng", fontHeader);

        addPdfRow(kpiTable, "Tổng Doanh Thu Toàn Hệ Thống", CURRENCY_FMT.format(kpi.getTotalRevenue()), "-", fontBody);
        document.add(kpiTable);

        // 2. Movie Breakdown Table
        if (byMovie != null && !byMovie.isEmpty()) {
            Paragraph movieTitle = new Paragraph("2. CƠ CẤU DOANH THU THEO PHIM (TOP PHIM)", fontBodyBold);
            movieTitle.setSpacingAfter(6);
            document.add(movieTitle);

            PdfPTable movieTable = new PdfPTable(4);
            movieTable.setWidthPercentage(100);
            movieTable.setWidths(new float[]{40, 25, 15, 20});
            movieTable.setSpacingAfter(15);

            addPdfHeaderCell(movieTable, "Tên Phim", fontHeader);
            addPdfHeaderCell(movieTable, "Doanh Thu", fontHeader);
            addPdfHeaderCell(movieTable, "Tỷ Trọng", fontHeader);
            addPdfHeaderCell(movieTable, "Số Vé Bán", fontHeader);

            for (RevenueBreakdownDTO m : byMovie) {
                addPdfCell(movieTable, m.getCategoryName(), fontBody, Element.ALIGN_LEFT);
                addPdfCell(movieTable, CURRENCY_FMT.format(m.getRevenue()), fontBody, Element.ALIGN_RIGHT);
                addPdfCell(movieTable, m.getSharePercent() + "%", fontBody, Element.ALIGN_CENTER);
                addPdfCell(movieTable, String.valueOf(m.getTicketsSold()), fontBody, Element.ALIGN_RIGHT);
            }
            document.add(movieTable);
        }

        // 3. Cinema Breakdown Table
        if (byCinema != null && !byCinema.isEmpty()) {
            Paragraph cinemaTitle = new Paragraph("3. DOANH THU THEO CỤM RẠP CHI NHÁNH", fontBodyBold);
            cinemaTitle.setSpacingAfter(6);
            document.add(cinemaTitle);

            PdfPTable cinemaTable = new PdfPTable(4);
            cinemaTable.setWidthPercentage(100);
            cinemaTable.setWidths(new float[]{40, 25, 15, 20});
            cinemaTable.setSpacingAfter(15);

            addPdfHeaderCell(cinemaTable, "Tên Cụm Rạp", fontHeader);
            addPdfHeaderCell(cinemaTable, "Doanh Thu", fontHeader);
            addPdfHeaderCell(cinemaTable, "Tỷ Trọng", fontHeader);
            addPdfHeaderCell(cinemaTable, "Số Vé Bán", fontHeader);

            for (RevenueBreakdownDTO c : byCinema) {
                addPdfCell(cinemaTable, c.getCategoryName(), fontBody, Element.ALIGN_LEFT);
                addPdfCell(cinemaTable, CURRENCY_FMT.format(c.getRevenue()), fontBody, Element.ALIGN_RIGHT);
                addPdfCell(cinemaTable, c.getSharePercent() + "%", fontBody, Element.ALIGN_CENTER);
                addPdfCell(cinemaTable, String.valueOf(c.getTicketsSold()), fontBody, Element.ALIGN_RIGHT);
            }
            document.add(cinemaTable);
        }

        // Footer note
        Paragraph footer = new Paragraph("CineMax Chain Management System - Báo Cáo Nội Bộ Bảo Mật", fontSubtitle);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
    }

    // =========================================================================
    // Font & Styling Helpers
    // =========================================================================

    private Font getUnicodeFont(float size, int style, Color color) {
        try {
            // Check bundled Arial font in resources
            String fontPath = "C:/Windows/Fonts/arial.ttf";
            File fontFile = new File(fontPath);
            if (!fontFile.exists()) {
                // Try classpath
                File localFont = new File("src/main/resources/fonts/Arial.ttf");
                if (localFont.exists()) {
                    fontPath = localFont.getAbsolutePath();
                }
            }
            BaseFont bf = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(bf, size, style, color);
        } catch (Exception e) {
            logger.warn("Unicode font not found, falling back to HELVETICA: {}", e.getMessage());
            return new Font(Font.HELVETICA, size, style, color);
        }
    }

    private void addPdfHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(229, 9, 20));
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addPdfCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private void addPdfRow(PdfPTable table, String col1, String col2, String col3, Font font) {
        addPdfCell(table, col1, font, Element.ALIGN_LEFT);
        addPdfCell(table, col2, font, Element.ALIGN_RIGHT);
        addPdfCell(table, col3, font, Element.ALIGN_CENTER);
    }

    private String formatGrowth(Double growth) {
        if (growth == null) return "-";
        return (growth >= 0 ? "+" : "") + growth + "%";
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private void createNumericCell(Row row, int col, BigDecimal value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private void addKpiRow(Sheet sheet, int rIdx, String name, BigDecimal val, CellStyle valStyle,
                           Double growth, CellStyle growthStyle, CellStyle defStyle) {
        Row row = sheet.createRow(rIdx);
        createCell(row, 0, name, defStyle);
        createNumericCell(row, 1, val, valStyle);
        if (growth != null && growthStyle != null) {
            createNumericCell(row, 2, BigDecimal.valueOf(growth / 100.0), growthStyle);
        } else {
            createCell(row, 2, "-", defStyle);
        }
    }
}
