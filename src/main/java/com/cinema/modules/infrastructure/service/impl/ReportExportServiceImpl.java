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

            addKpiRow(kpiSheet, rIdx++, "Tổng Doanh Thu Toàn Hệ Thống", kpi.getTotalRevenue(), currencyStyle, kpi.getRevenueGrowthPercent(), percentStyle, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Doanh Thu Vé Xem Phim", kpi.getTicketRevenue(), currencyStyle, null, null, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Doanh Thu Bắp Nước & F&B", kpi.getFnbRevenue(), currencyStyle, null, null, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Tổng Tiền Chiết Khấu / Voucher", kpi.getDiscountAmount(), currencyStyle, null, null, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Số Đơn Đặt Vé Xác Nhận", BigDecimal.valueOf(kpi.getTotalConfirmedBookings()), intStyle, kpi.getBookingsGrowthPercent(), percentStyle, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Số Lượng Vé Bán Ra", BigDecimal.valueOf(kpi.getTotalTicketsSold()), intStyle, kpi.getTicketsGrowthPercent(), percentStyle, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Giá Trị Đơn Trung Bình (AOV)", kpi.getAverageOrderValue(), currencyStyle, null, null, dataStyle);
            addKpiRow(kpiSheet, rIdx++, "Giá Vé Trung Bình (ATP)", kpi.getAverageTicketPrice(), currencyStyle, null, null, dataStyle);

            kpiSheet.autoSizeColumn(0);
            kpiSheet.autoSizeColumn(1);
            kpiSheet.autoSizeColumn(2);

            // =================================================================
            // SHEET 2: THEO THỜI GIAN
            // =================================================================
            if (timeSeries != null && !timeSeries.isEmpty()) {
                Sheet tsSheet = workbook.createSheet("Theo Thời Gian (" + filter.getGroupBy() + ")");
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

        addPdfRow(kpiTable, "Tổng Doanh Thu Toàn Hệ Thống", CURRENCY_FMT.format(kpi.getTotalRevenue()), formatGrowth(kpi.getRevenueGrowthPercent()), fontBody);
        addPdfRow(kpiTable, "Doanh Thu Vé Xem Phim", CURRENCY_FMT.format(kpi.getTicketRevenue()), "-", fontBody);
        addPdfRow(kpiTable, "Doanh Thu Bắp Nước (F&B)", CURRENCY_FMT.format(kpi.getFnbRevenue()), "-", fontBody);
        addPdfRow(kpiTable, "Số Đơn Đặt Vé Xác Nhận", String.valueOf(kpi.getTotalConfirmedBookings()), formatGrowth(kpi.getBookingsGrowthPercent()), fontBody);
        addPdfRow(kpiTable, "Số Lượng Vé Bán Ra", String.valueOf(kpi.getTotalTicketsSold()), formatGrowth(kpi.getTicketsGrowthPercent()), fontBody);
        addPdfRow(kpiTable, "Giá Trị Đơn Trung Bình (AOV)", CURRENCY_FMT.format(kpi.getAverageOrderValue()), "-", fontBody);
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

    @Override
    public void exportComparisonExcel(List<com.cinema.modules.infrastructure.dto.CinemaMetricDTO> metrics,
                                      com.cinema.modules.infrastructure.dto.ChainSummaryDTO summary,
                                      com.cinema.modules.infrastructure.dto.ComparisonFilterDTO filter,
                                      OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            DataFormat df = wb.createDataFormat();

            CellStyle titleStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.DARK_RED.getIndex());
            titleStyle.setFont(titleFont);

            CellStyle metaStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font metaFont = wb.createFont();
            metaFont.setItalic(true);
            metaStyle.setFont(metaFont);

            CellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            setThinBorders(headerStyle);
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle currencyStyle = wb.createCellStyle();
            setThinBorders(currencyStyle);
            currencyStyle.setDataFormat(df.getFormat("#,##0 ₫"));
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle percentStyle = wb.createCellStyle();
            setThinBorders(percentStyle);
            percentStyle.setDataFormat(df.getFormat("0.00%"));
            percentStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle intStyle = wb.createCellStyle();
            setThinBorders(intStyle);
            intStyle.setDataFormat(df.getFormat("#,##0"));
            intStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle textStyle = wb.createCellStyle();
            setThinBorders(textStyle);
            textStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle centerStyle = wb.createCellStyle();
            setThinBorders(centerStyle);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);

            Sheet sheet = wb.createSheet("So Sánh Cụm Rạp");
            sheet.setDisplayGridlines(true);

            int rIdx = 0;
            Row rTitle = sheet.createRow(rIdx++);
            Cell cTitle = rTitle.createCell(0);
            cTitle.setCellValue("CINEMAX - BÁO CÁO SO SÁNH HIỆU SUẤT CỤM RẠP TOÀN HỆ THỐNG");
            cTitle.setCellStyle(titleStyle);

            Row rSub = sheet.createRow(rIdx++);
            rSub.createCell(0).setCellValue("Kỳ báo cáo: " + filter.getFrom().format(DATE_FMT) + " đến " + filter.getTo().format(DATE_FMT)
                    + " | Chế độ: " + filter.getCompareMode()
                    + " | Xuất lúc: " + LocalDateTime.now().format(DATETIME_FMT));
            rSub.getCell(0).setCellStyle(metaStyle);

            rIdx++; // blank

            // Chain summary KPI block
            Row rKpiH = sheet.createRow(rIdx++);
            rKpiH.createCell(0).setCellValue("Chỉ Số Toàn Chuỗi");
            rKpiH.createCell(1).setCellValue("Giá Trị");
            rKpiH.getCell(0).setCellStyle(headerStyle);
            rKpiH.getCell(1).setCellStyle(headerStyle);

            addSummaryRow(sheet, rIdx++, "Tổng doanh thu chuỗi", summary.getTotalRevenue(), currencyStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Tổng doanh thu kỳ trước/cùng kỳ", summary.getTotalComparisonRevenue(), currencyStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Tăng trưởng toàn chuỗi", summary.getChainGrowthPercent().divide(BigDecimal.valueOf(100)), percentStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Số cụm rạp hoạt động", BigDecimal.valueOf(summary.getActiveCinemasCount()), intStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Tổng số phòng chiếu", BigDecimal.valueOf(summary.getTotalRoomsCount()), intStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Tổng vé bán ra", BigDecimal.valueOf(summary.getTotalTicketsSold()), intStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Doanh thu TB mỗi rạp", summary.getChainAverageRevenuePerCinema(), currencyStyle, textStyle);
            addSummaryRow(sheet, rIdx++, "Tỷ lệ lấp đầy TB toàn chuỗi", summary.getChainAverageOccupancyRate().divide(BigDecimal.valueOf(100)), percentStyle, textStyle);
            addSummaryTextRow(sheet, rIdx++, "Cụm rạp dẫn đầu", summary.getBestPerformerName() + " (" + CURRENCY_FMT.format(summary.getBestPerformerRevenue()) + ")", textStyle);
            addSummaryTextRow(sheet, rIdx++, "Cụm rạp thấp nhất", summary.getWorstPerformerName() + " (" + CURRENCY_FMT.format(summary.getWorstPerformerRevenue()) + ")", textStyle);

            rIdx++; // blank

            // Data Table Headers
            String[] headers = {
                    "Hạng", "Mã Rạp", "Tên Cụm Rạp", "Thành Phố", "Trạng Thái",
                    "Doanh Thu Kỳ Này", "Doanh Thu Kỳ So Sánh", "Tăng Trưởng", "Tỷ Trọng",
                    "Số Đơn", "Vé Bán Ra", "Giá Vé TB", "Doanh Thu F&B",
                    "Số Phòng", "Suất Chiếu", "Tỷ Lệ Lấp Đầy", "DT / Phòng", "DT / Suất",
                    "Phim Dẫn Đầu", "Phân Tầng Hiệu Suất"
            };

            Row rHead = sheet.createRow(rIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell hCell = rHead.createCell(i);
                hCell.setCellValue(headers[i]);
                hCell.setCellStyle(headerStyle);
            }

            for (com.cinema.modules.infrastructure.dto.CinemaMetricDTO m : metrics) {
                Row row = sheet.createRow(rIdx++);
                createNumericCell(row, 0, BigDecimal.valueOf(m.getRank()), centerStyle);
                createCell(row, 1, m.getCinemaCode(), centerStyle);
                createCell(row, 2, m.getCinemaName(), textStyle);
                createCell(row, 3, m.getCity() != null ? m.getCity() : "-", centerStyle);
                createCell(row, 4, m.isActive() ? "Hoạt động" : "Tạm dừng", centerStyle);

                createNumericCell(row, 5, m.getRevenue(), currencyStyle);
                createNumericCell(row, 6, m.getComparisonRevenue(), currencyStyle);
                createNumericCell(row, 7, m.getGrowthPercent().divide(BigDecimal.valueOf(100)), percentStyle);
                createNumericCell(row, 8, m.getRevenueSharePercent().divide(BigDecimal.valueOf(100)), percentStyle);

                createNumericCell(row, 9, BigDecimal.valueOf(m.getConfirmedBookings()), intStyle);
                createNumericCell(row, 10, BigDecimal.valueOf(m.getTicketsSold()), intStyle);
                createNumericCell(row, 11, m.getAverageTicketPrice(), currencyStyle);
                createNumericCell(row, 12, m.getFnbRevenue(), currencyStyle);

                createNumericCell(row, 13, BigDecimal.valueOf(m.getRoomsCount()), centerStyle);
                createNumericCell(row, 14, BigDecimal.valueOf(m.getShowtimesCount()), centerStyle);
                createNumericCell(row, 15, m.getOccupancyRate().divide(BigDecimal.valueOf(100)), percentStyle);
                createNumericCell(row, 16, m.getRevenuePerRoom(), currencyStyle);
                createNumericCell(row, 17, m.getRevenuePerShowtime(), currencyStyle);

                createCell(row, 18, m.getTopMovieName(), textStyle);
                createCell(row, 19, m.getPerformanceTier().name(), centerStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
        }
    }

    private void addSummaryRow(Sheet sheet, int rIdx, String label, BigDecimal val, CellStyle valStyle, CellStyle lblStyle) {
        Row row = sheet.createRow(rIdx);
        createCell(row, 0, label, lblStyle);
        createNumericCell(row, 1, val, valStyle);
    }

    private void addSummaryTextRow(Sheet sheet, int rIdx, String label, String val, CellStyle lblStyle) {
        Row row = sheet.createRow(rIdx);
        createCell(row, 0, label, lblStyle);
        createCell(row, 1, val, lblStyle);
    }

    @Override
    public void exportComparisonPdf(List<com.cinema.modules.infrastructure.dto.CinemaMetricDTO> metrics,
                                    com.cinema.modules.infrastructure.dto.ChainSummaryDTO summary,
                                    com.cinema.modules.infrastructure.dto.ComparisonFilterDTO filter,
                                    OutputStream out) throws Exception {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = getUnicodeFont(15, Font.BOLD, new Color(229, 9, 20));
        Font subtitleFont = getUnicodeFont(9, Font.ITALIC, Color.DARK_GRAY);
        Font sectionFont = getUnicodeFont(11, Font.BOLD, new Color(33, 37, 41));
        Font headerFont = getUnicodeFont(8, Font.BOLD, Color.WHITE);
        Font bodyFont = getUnicodeFont(8, Font.NORMAL, Color.BLACK);

        Paragraph pTitle = new Paragraph("CINEMAX - BÁO CÁO SO SÁNH HIỆU SUẤT CỤM RẠP TOÀN HỆ THỐNG", titleFont);
        pTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(pTitle);

        Paragraph pSub = new Paragraph("Giai đoạn: " + filter.getFrom().format(DATE_FMT) + " đến " + filter.getTo().format(DATE_FMT)
                + " | Chế độ so sánh: " + filter.getCompareMode()
                + " | Thời điểm xuất: " + LocalDateTime.now().format(DATETIME_FMT), subtitleFont);
        pSub.setAlignment(Element.ALIGN_CENTER);
        pSub.setSpacingAfter(10);
        document.add(pSub);

        // Chain summary table
        PdfPTable sumTable = new PdfPTable(4);
        sumTable.setWidthPercentage(100);
        sumTable.setSpacingAfter(12);

        addPdfCell(sumTable, "Tổng Doanh Thu Chuỗi: " + CURRENCY_FMT.format(summary.getTotalRevenue()), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Tăng Trưởng Toàn Chuỗi: " + formatGrowth(summary.getChainGrowthPercent().doubleValue()), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Doanh Thu TB Mỗi Rạp: " + CURRENCY_FMT.format(summary.getChainAverageRevenuePerCinema()), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Tỷ Lệ Lấp Đầy TB: " + summary.getChainAverageOccupancyRate() + "%", bodyFont, Element.ALIGN_LEFT);

        addPdfCell(sumTable, "Số Rạp Hoạt Động: " + summary.getActiveCinemasCount() + " / " + summary.getTotalCinemasCount(), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Tổng Suất Chiếu: " + summary.getTotalShowtimesCount(), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Rạp Xuất Sắc Nhất: " + summary.getBestPerformerName(), bodyFont, Element.ALIGN_LEFT);
        addPdfCell(sumTable, "Rạp Thấp Nhất: " + summary.getWorstPerformerName(), bodyFont, Element.ALIGN_LEFT);

        document.add(sumTable);

        // Comparison Table
        float[] colWidths = {2f, 3.5f, 5.5f, 3.5f, 4.5f, 4.5f, 3f, 3f, 3.5f, 3f, 3f, 5f, 3.5f};
        PdfPTable table = new PdfPTable(colWidths);
        table.setWidthPercentage(100);

        String[] headers = {
                "Hạng", "Mã", "Tên Cụm Rạp", "T.Phố", "Doanh Thu", "DT Kỳ Trước",
                "T.Trưởng", "Tỷ Trọng", "Vé Bán", "Lấp Đầy", "DT/Phòng", "Phim Top 1", "Phân Tầng"
        };
        for (String h : headers) {
            addPdfHeaderCell(table, h, headerFont);
        }

        for (com.cinema.modules.infrastructure.dto.CinemaMetricDTO m : metrics) {
            addPdfCell(table, String.valueOf(m.getRank()), bodyFont, Element.ALIGN_CENTER);
            addPdfCell(table, m.getCinemaCode(), bodyFont, Element.ALIGN_CENTER);
            addPdfCell(table, m.getCinemaName(), bodyFont, Element.ALIGN_LEFT);
            addPdfCell(table, m.getCity() != null ? m.getCity() : "-", bodyFont, Element.ALIGN_CENTER);
            addPdfCell(table, CURRENCY_FMT.format(m.getRevenue()), bodyFont, Element.ALIGN_RIGHT);
            addPdfCell(table, CURRENCY_FMT.format(m.getComparisonRevenue()), bodyFont, Element.ALIGN_RIGHT);
            addPdfCell(table, formatGrowth(m.getGrowthPercent().doubleValue()), bodyFont, Element.ALIGN_CENTER);
            addPdfCell(table, m.getRevenueSharePercent() + "%", bodyFont, Element.ALIGN_RIGHT);
            addPdfCell(table, String.valueOf(m.getTicketsSold()), bodyFont, Element.ALIGN_RIGHT);
            addPdfCell(table, m.getOccupancyRate() + "%", bodyFont, Element.ALIGN_CENTER);
            addPdfCell(table, CURRENCY_FMT.format(m.getRevenuePerRoom()), bodyFont, Element.ALIGN_RIGHT);
            addPdfCell(table, m.getTopMovieName(), bodyFont, Element.ALIGN_LEFT);
            addPdfCell(table, m.getPerformanceTier().name(), bodyFont, Element.ALIGN_CENTER);
        }

        document.add(table);
        document.close();
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
