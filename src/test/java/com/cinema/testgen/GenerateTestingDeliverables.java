package com.cinema.testgen;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Generator and validator for DungBD's Integration Test & System Test deliverables.
 */
public class GenerateTestingDeliverables {

    public static class TestCaseData {
        public String id;
        public String description;
        public String procedure;
        public String expected;
        public String preConditions;
        public String round1;
        public String testDate;
        public String tester;
        public String note;

        public TestCaseData(String id, String description, String procedure, String expected,
                            String preConditions, String round1, String testDate, String tester, String note) {
            this.id = id;
            this.description = description;
            this.procedure = procedure;
            this.expected = expected;
            this.preConditions = preConditions;
            this.round1 = round1;
            this.testDate = testDate;
            this.tester = tester;
            this.note = note;
        }
    }

    public static class ScenarioGroup {
        public String scenarioName;
        public List<TestCaseData> cases = new ArrayList<>();

        public ScenarioGroup(String scenarioName) {
            this.scenarioName = scenarioName;
        }

        public void add(TestCaseData tc) {
            this.cases.add(tc);
        }
    }

    public static class SheetDef {
        public String sheetName;
        public String workflowTitle;
        public String testRequirement;
        public List<ScenarioGroup> scenarios = new ArrayList<>();

        public SheetDef(String sheetName, String workflowTitle, String testRequirement) {
            this.sheetName = sheetName;
            this.workflowTitle = workflowTitle;
            this.testRequirement = testRequirement;
        }
    }

    @Test
    void generateBothWorkbooks() throws Exception {
        File templateFile = new File("D:\\SWP\\SWP391-Gr3\\docs\\SE2056-JV - Group x  - Template3.2_System-Test.xlsx");
        File stDest = new File("D:\\SWP\\SWP391-Gr3\\docs\\testing\\SE2056-JV_Group3_System-Test_DungBD.xlsx");
        File itDest = new File("D:\\SWP\\SWP391-Gr3\\docs\\testing\\SE2056-JV_Group3_Integration-Test_DungBD.xlsx");

        // 1. Build System Test Workbook
        System.out.println("Building System Test Workbook...");
        buildSystemTestWorkbook(templateFile, stDest);

        // 2. Build Integration Test Workbook
        System.out.println("Building Integration Test Workbook...");
        buildIntegrationTestWorkbook(templateFile, itDest);

        System.out.println("All testing workbooks generated and validated successfully!");
    }

    private void buildSystemTestWorkbook(File templateFile, File destFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(templateFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            // Fix Cover sheet
            setupCoverSheet(wb, "System Test Report for Cinema Branch, Room/Seat, System Administration and Reporting Workflows");

            List<SheetDef> sheets = SystemTestData.getSystemTestWorkflows();

            // Setup Workflow sheets
            Sheet templateSheet = wb.getSheet("Workflow Name1");
            for (SheetDef def : sheets) {
                Sheet s = wb.cloneSheet(wb.getSheetIndex(templateSheet));
                wb.setSheetName(wb.getSheetIndex(s), def.sheetName);
                populateWorkflowSheet(s, def);
            }

            // Remove sample sheets
            wb.removeSheetAt(wb.getSheetIndex("Workflow Name1"));
            wb.removeSheetAt(wb.getSheetIndex("Workflow Name2"));

            // Setup Test Cases sheet
            setupTestCasesSheet(wb, sheets, "System Test");

            // Setup Test Statistics sheet
            setupStatisticsSheet(wb, sheets);

            // Re-evaluate all formulas
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                wb.write(fos);
            }
            System.out.println("Wrote System Test to: " + destFile.getAbsolutePath());
        }
    }

    private void buildIntegrationTestWorkbook(File templateFile, File destFile) throws Exception {
        try (FileInputStream fis = new FileInputStream(templateFile);
             Workbook wb = new XSSFWorkbook(fis)) {

            // Fix Cover sheet
            setupCoverSheet(wb, "Integration Test Report for Cinema Branch, Room/Seat, Settings, Dashboard and BI Reports Screens");

            List<SheetDef> sheets = IntegrationTestData.getIntegrationTestSheets();

            // Setup Screen sheets
            Sheet templateSheet = wb.getSheet("Workflow Name1");
            for (SheetDef def : sheets) {
                Sheet s = wb.cloneSheet(wb.getSheetIndex(templateSheet));
                wb.setSheetName(wb.getSheetIndex(s), def.sheetName);
                populateWorkflowSheet(s, def);
            }

            // Remove sample sheets
            wb.removeSheetAt(wb.getSheetIndex("Workflow Name1"));
            wb.removeSheetAt(wb.getSheetIndex("Workflow Name2"));

            // Setup Test Cases sheet
            setupTestCasesSheet(wb, sheets, "Integration Test");

            // Setup Test Statistics sheet
            setupStatisticsSheet(wb, sheets);

            // Re-evaluate all formulas
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                wb.write(fos);
            }
            System.out.println("Wrote Integration Test to: " + destFile.getAbsolutePath());
        }
    }

    private void setupCoverSheet(Workbook wb, String description) {
        Sheet cover = wb.getSheet("Cover");
        if (cover == null) return;

        // Project Name
        Row rProj = cover.getRow(14);
        if (rProj != null && rProj.getCell(5) != null) {
            rProj.getCell(5).setCellValue("CineMax – Multi-Branch Cinema Management System");
        }
        // Project Code
        Row rCode = cover.getRow(15);
        if (rCode != null && rCode.getCell(5) != null) {
            rCode.getCell(5).setCellValue("SE2056-JV - Group 3");
        }
        // Creator
        Row rCreator = cover.getRow(16);
        if (rCreator != null && rCreator.getCell(5) != null) {
            rCreator.getCell(5).setCellValue("Bùi Đức Dũng (DungBD)");
        }
        // Date
        Row rDate = cover.getRow(17);
        if (rDate != null && rDate.getCell(5) != null) {
            rDate.getCell(5).setCellValue("2026-11-03");
        }
        // Version
        Row rVer = cover.getRow(18);
        if (rVer != null && rVer.getCell(5) != null) {
            rVer.getCell(5).setCellValue("1.0");
        }

        // Record of change
        Row rRec = cover.getRow(22);
        if (rRec != null) {
            if (rRec.getCell(1) != null) rRec.getCell(1).setCellValue("2026-11-03");
            if (rRec.getCell(3) != null) rRec.getCell(3).setCellValue("1.0");
            if (rRec.getCell(5) != null) rRec.getCell(5).setCellValue("A");
            if (rRec.getCell(7) != null) rRec.getCell(7).setCellValue(description);
            if (rRec.getCell(13) != null) rRec.getCell(13).setCellValue("Feature Tree v2.2, ProjectTracking, SRS M-04, M-10, M-11");
        }
    }

    private void setupTestCasesSheet(Workbook wb, List<SheetDef> sheets, String docType) {
        Sheet tc = wb.getSheet("Test Cases");
        if (tc == null) return;

        // Fix D3, D4 formulas: point to Cover
        Row r3 = tc.getRow(2);
        if (r3 != null) {
            Cell c = r3.getCell(3);
            if (c == null) c = r3.createCell(3);
            c.setCellFormula("Cover!F15");
        }
        Row r4 = tc.getRow(3);
        if (r4 != null) {
            Cell c = r4.getCell(3);
            if (c == null) c = r4.createCell(3);
            c.setCellFormula("Cover!F16");
        }

        // Test Environment table (Row 7 to 13)
        setEnvCell(tc, 6, 3, "Windows 11 Pro 64-bit / Linux Ubuntu 22.04 LTS");
        setEnvCell(tc, 7, 3, "Oracle JDK 17 (Java SE Development Kit 17.0.12)");
        setEnvCell(tc, 8, 3, "Apache Tomcat 10.1.34 (Jakarta EE 10, Servlet 6.0)");
        setEnvCell(tc, 9, 3, "Microsoft SQL Server 2022 / Database: cinema_chain_db (Seed scripts 01-05 applied)");
        setEnvCell(tc, 10, 3, "Google Chrome (Latest v130+), Microsoft Edge (Chromium)");
        setEnvCell(tc, 11, 3, "Admin: admin@cinemax.vn (pw: 123456) | Manager: manager.hn01@cinemax.vn (pw: 123456) | Staff: staff.hn01@cinemax.vn (pw: 123456)");

        // Table list of functions starting at row 16 (index 15)
        int startRow = 15;
        // Clean old sample rows
        for (int i = startRow; i <= startRow + 30; i++) {
            Row r = tc.getRow(i);
            if (r != null) {
                for (int c = 0; c < 15; c++) {
                    Cell cell = r.getCell(c);
                    if (cell != null) cell.setCellValue("");
                }
            }
        }

        CellStyle borderStyle = wb.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        for (int i = 0; i < sheets.size(); i++) {
            SheetDef def = sheets.get(i);
            Row r = tc.getRow(startRow + i);
            if (r == null) r = tc.createRow(startRow + i);

            Cell c0 = getOrCreateCell(r, 0); c0.setCellValue(i + 1); c0.setCellStyle(borderStyle);
            Cell c1 = getOrCreateCell(r, 1); c1.setCellValue(def.workflowTitle); c1.setCellStyle(borderStyle);
            Cell c2 = getOrCreateCell(r, 2); c2.setCellValue(def.sheetName); c2.setCellStyle(borderStyle);
            Cell c3 = getOrCreateCell(r, 3); c3.setCellValue(def.testRequirement); c3.setCellStyle(borderStyle);
            Cell c4 = getOrCreateCell(r, 4); c4.setCellValue("Đã nạp DBContext, SQL Server localhost và tài khoản hợp lệ."); c4.setCellStyle(borderStyle);
        }
    }

    private void setupStatisticsSheet(Workbook wb, List<SheetDef> sheets) {
        Sheet stat = wb.getSheet("Test Statistics");
        if (stat == null) return;

        // Clear existing rows 10 to 25
        for (int r = 9; r <= 30; r++) {
            Row row = stat.getRow(r);
            if (row != null) {
                for (int c = 0; c < 12; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) cell.setCellValue("");
                }
            }
        }

        CellStyle borderStyle = wb.createCellStyle();
        borderStyle.setBorderBottom(BorderStyle.THIN);
        borderStyle.setBorderTop(BorderStyle.THIN);
        borderStyle.setBorderLeft(BorderStyle.THIN);
        borderStyle.setBorderRight(BorderStyle.THIN);

        CellStyle intStyle = wb.createCellStyle();
        intStyle.cloneStyleFrom(borderStyle);
        intStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0"));

        CellStyle pctStyle = wb.createCellStyle();
        pctStyle.cloneStyleFrom(borderStyle);
        pctStyle.setDataFormat(wb.createDataFormat().getFormat("0.0%"));

        int rIdx = 9;
        for (int i = 0; i < sheets.size(); i++) {
            SheetDef def = sheets.get(i);
            Row row = stat.getRow(rIdx);
            if (row == null) row = stat.createRow(rIdx);

            String sName = "'" + def.sheetName + "'";
            int excelRow = rIdx + 1;

            Cell c1 = getOrCreateCell(row, 1); c1.setCellValue(i + 1); c1.setCellStyle(borderStyle);
            Cell c2 = getOrCreateCell(row, 2); c2.setCellFormula(sName + "!B2"); c2.setCellStyle(borderStyle);
            Cell c3 = getOrCreateCell(row, 3); c3.setCellFormula(sName + "!B6"); c3.setCellStyle(intStyle);
            Cell c4 = getOrCreateCell(row, 4); c4.setCellFormula(sName + "!C6"); c4.setCellStyle(intStyle);
            Cell c5 = getOrCreateCell(row, 5); c5.setCellFormula(sName + "!D6"); c5.setCellStyle(intStyle);
            Cell c6 = getOrCreateCell(row, 6); c6.setCellFormula(sName + "!E6"); c6.setCellStyle(intStyle);
            Cell c7 = getOrCreateCell(row, 7); c7.setCellFormula(sName + "!B4"); c7.setCellStyle(intStyle);
            Cell c8 = getOrCreateCell(row, 8); c8.setCellFormula("IFERROR(D" + excelRow + "/(H" + excelRow + "-G" + excelRow + "), 0)"); c8.setCellStyle(pctStyle);

            rIdx++;
        }

        // Sub total row
        int totalRow = rIdx + 1;
        Row rSub = stat.getRow(rIdx);
        if (rSub == null) rSub = stat.createRow(rIdx);
        Cell cSub = getOrCreateCell(rSub, 2); cSub.setCellValue("Sub total"); cSub.setCellStyle(borderStyle);
        for (int col = 3; col <= 7; col++) {
            char colLetter = (char) ('A' + col);
            Cell c = getOrCreateCell(rSub, col);
            c.setCellFormula("SUM(" + colLetter + "10:" + colLetter + (rIdx) + ")");
            c.setCellStyle(intStyle);
        }
        Cell cSubPct = getOrCreateCell(rSub, 8);
        cSubPct.setCellFormula("IFERROR(D" + totalRow + "/(H" + totalRow + "-G" + totalRow + "), 0)");
        cSubPct.setCellStyle(pctStyle);

        // Test coverage row
        rIdx += 2;
        int covRow = rIdx + 1;
        Row rCov = stat.getRow(rIdx);
        if (rCov == null) rCov = stat.createRow(rIdx);
        Cell cCovLbl = getOrCreateCell(rCov, 2); cCovLbl.setCellValue("Test coverage"); cCovLbl.setCellStyle(borderStyle);
        Cell cCovVal = getOrCreateCell(rCov, 8);
        cCovVal.setCellFormula("IFERROR((D" + totalRow + "+E" + totalRow + ")/(H" + totalRow + "-G" + totalRow + "), 0)");
        cCovVal.setCellStyle(pctStyle);
    }

    private void populateWorkflowSheet(Sheet sheet, SheetDef def) {
        Workbook wb = sheet.getWorkbook();

        // 1. Workflow Title in B2
        Row r2 = sheet.getRow(1);
        if (r2 != null && r2.getCell(1) != null) {
            r2.getCell(1).setCellValue(def.workflowTitle);
        }

        // 2. Test Requirement in B3
        Row r3 = sheet.getRow(2);
        if (r3 != null && r3.getCell(1) != null) {
            r3.getCell(1).setCellValue(def.testRequirement);
        }

        // 3. Fix Row 4: Number of TCs formula
        Row r4 = sheet.getRow(3);
        if (r4 != null && r4.getCell(1) != null) {
            r4.getCell(1).setCellFormula("COUNTIF($F$11:$F$500, \"<>\")");
        }

        // 4. Fix Rows 6, 7, 8: Counting formulas
        Row r6 = sheet.getRow(5);
        if (r6 != null) {
            getOrCreateCell(r6, 1).setCellFormula("COUNTIF($F$11:$F$500, B$5)");
            getOrCreateCell(r6, 2).setCellFormula("COUNTIF($F$11:$F$500, C$5)");
            getOrCreateCell(r6, 3).setCellFormula("COUNTIF($F$11:$F$500, D$5)");
            getOrCreateCell(r6, 4).setCellFormula("COUNTIF($F$11:$F$500, E$5)");
        }
        Row r7 = sheet.getRow(6);
        if (r7 != null) {
            getOrCreateCell(r7, 1).setCellFormula("COUNTIF($I$11:$I$500, B$5)");
            getOrCreateCell(r7, 2).setCellFormula("COUNTIF($I$11:$I$500, C$5)");
            getOrCreateCell(r7, 3).setCellFormula("COUNTIF($I$11:$I$500, D$5)");
            getOrCreateCell(r7, 4).setCellFormula("COUNTIF($I$11:$I$500, E$5)");
        }
        Row r8 = sheet.getRow(7);
        if (r8 != null) {
            getOrCreateCell(r8, 1).setCellFormula("COUNTIF($L$11:$L$500, B$5)");
            getOrCreateCell(r8, 2).setCellFormula("COUNTIF($L$11:$L$500, C$5)");
            getOrCreateCell(r8, 3).setCellFormula("COUNTIF($L$11:$L$500, D$5)");
            getOrCreateCell(r8, 4).setCellFormula("COUNTIF($L$11:$L$500, E$5)");
        }

        // Clear existing rows 11 to 200
        for (int r = 10; r <= 200; r++) {
            Row row = sheet.getRow(r);
            if (row != null) {
                for (int c = 0; c < 16; c++) {
                    Cell cell = row.getCell(c);
                    if (cell != null) cell.setCellValue("");
                }
            }
        }

        // Styles
        CellStyle scenarioStyle = wb.createCellStyle();
        scenarioStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        scenarioStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        org.apache.poi.ss.usermodel.Font sFont = wb.createFont();
        sFont.setBold(true);
        scenarioStyle.setFont(sFont);
        setThinBorders(scenarioStyle);

        CellStyle cellStyle = wb.createCellStyle();
        setThinBorders(cellStyle);
        cellStyle.setWrapText(true);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP);

        CellStyle centerStyle = wb.createCellStyle();
        centerStyle.cloneStyleFrom(cellStyle);
        centerStyle.setAlignment(HorizontalAlignment.CENTER);

        int curRow = 10;
        for (ScenarioGroup group : def.scenarios) {
            // Scenario header row
            Row sRow = sheet.getRow(curRow);
            if (sRow == null) sRow = sheet.createRow(curRow);
            Cell sCell = getOrCreateCell(sRow, 0);
            sCell.setCellValue(group.scenarioName);
            sCell.setCellStyle(scenarioStyle);
            for (int c = 1; c <= 14; c++) {
                Cell emptyC = getOrCreateCell(sRow, c);
                emptyC.setCellStyle(scenarioStyle);
            }
            curRow++;

            // Test cases
            for (TestCaseData tc : group.cases) {
                Row r = sheet.getRow(curRow);
                if (r == null) r = sheet.createRow(curRow);

                getOrCreateCell(r, 0).setCellValue(tc.id); r.getCell(0).setCellStyle(centerStyle);
                getOrCreateCell(r, 1).setCellValue(tc.description); r.getCell(1).setCellStyle(cellStyle);
                getOrCreateCell(r, 2).setCellValue(tc.procedure); r.getCell(2).setCellStyle(cellStyle);
                getOrCreateCell(r, 3).setCellValue(tc.expected); r.getCell(3).setCellStyle(cellStyle);
                getOrCreateCell(r, 4).setCellValue(tc.preConditions); r.getCell(4).setCellStyle(cellStyle);

                getOrCreateCell(r, 5).setCellValue(tc.round1); r.getCell(5).setCellStyle(centerStyle);
                getOrCreateCell(r, 6).setCellValue(tc.testDate); r.getCell(6).setCellStyle(centerStyle);
                getOrCreateCell(r, 7).setCellValue(tc.tester); r.getCell(7).setCellStyle(centerStyle);

                getOrCreateCell(r, 8).setCellValue("Pending"); r.getCell(8).setCellStyle(centerStyle);
                getOrCreateCell(r, 9).setCellValue(""); r.getCell(9).setCellStyle(centerStyle);
                getOrCreateCell(r, 10).setCellValue(""); r.getCell(10).setCellStyle(centerStyle);

                getOrCreateCell(r, 11).setCellValue("Pending"); r.getCell(11).setCellStyle(centerStyle);
                getOrCreateCell(r, 12).setCellValue(""); r.getCell(12).setCellStyle(centerStyle);
                getOrCreateCell(r, 13).setCellValue(""); r.getCell(13).setCellStyle(centerStyle);

                getOrCreateCell(r, 14).setCellValue(tc.note); r.getCell(14).setCellStyle(cellStyle);

                curRow++;
            }
        }
    }

    private void setThinBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void setEnvCell(Sheet s, int row, int col, String val) {
        Row r = s.getRow(row);
        if (r == null) r = s.createRow(row);
        Cell c = r.getCell(col);
        if (c == null) c = r.createCell(col);
        c.setCellValue(val);
    }

    private Cell getOrCreateCell(Row r, int cIdx) {
        Cell c = r.getCell(cIdx);
        if (c == null) c = r.createCell(cIdx);
        return c;
    }
}
