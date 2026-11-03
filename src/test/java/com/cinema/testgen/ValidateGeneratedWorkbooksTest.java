package com.cinema.testgen;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ValidateGeneratedWorkbooksTest {

    @Test
    void testValidateSystemTestWorkbook() throws Exception {
        File file = new File("D:\\SWP\\SWP391-Gr3\\docs\\testing\\SE2056-JV_Group3_System-Test_DungBD.xlsx");
        assertTrue(file.exists());
        validateWorkbook(file, 7); // Cover, Test Cases, Test Statistics, WF1, WF2, WF3, WF4
    }

    @Test
    void testValidateIntegrationTestWorkbook() throws Exception {
        File file = new File("D:\\SWP\\SWP391-Gr3\\docs\\testing\\SE2056-JV_Group3_Integration-Test_DungBD.xlsx");
        assertTrue(file.exists());
        validateWorkbook(file, 12); // Cover, Test Cases, Test Statistics, 9 screens
    }

    private void validateWorkbook(File file, int expectedSheetCount) throws Exception {
        try (FileInputStream fis = new FileInputStream(file);
             Workbook wb = new XSSFWorkbook(fis)) {

            assertEquals(expectedSheetCount, wb.getNumberOfSheets(), "Mismatch in sheet count for " + file.getName());
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();

            int errorCount = 0;
            for (int s = 0; s < wb.getNumberOfSheets(); s++) {
                Sheet sheet = wb.getSheetAt(s);
                System.out.println("Validating sheet: " + sheet.getSheetName() + " in " + file.getName());
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        if (cell.getCellType() == CellType.FORMULA) {
                            try {
                                CellValue val = evaluator.evaluate(cell);
                                if (val.getCellType() == CellType.ERROR) {
                                    System.err.println("Formula ERROR in " + sheet.getSheetName() + " at cell " + cell.getAddress() + ": " + cell.getCellFormula() + " -> Error code: " + val.getErrorValue());
                                    errorCount++;
                                }
                            } catch (Exception e) {
                                System.err.println("Formula EXCEPTION in " + sheet.getSheetName() + " at cell " + cell.getAddress() + ": " + cell.getCellFormula() + " -> " + e.getMessage());
                                errorCount++;
                            }
                        }
                    }
                }
            }
            assertEquals(0, errorCount, "Found " + errorCount + " formula errors in " + file.getName());
            System.out.println(">>> Verified " + file.getName() + ": ZERO formula errors, all sheets valid!");
        }
    }
}
