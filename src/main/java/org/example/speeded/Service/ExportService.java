package org.example.speeded.Service;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.example.speeded.Entity.Run;
import org.example.speeded.Repository.RunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExportService {

    @Autowired
    private RunRepository runRepo;

    public void exportExcel(Long userId, HttpServletResponse response) throws Exception {
        List<Run> runs = runRepo.findByUserId(userId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Runs");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Distance");
        header.createCell(1).setCellValue("Time");
        header.createCell(2).setCellValue("Calories");

        int rowNum = 1;
        for (Run r : runs) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(r.getDistance());
            row.createCell(1).setCellValue(r.getTime());
            row.createCell(2).setCellValue(r.getCalories());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=runs.xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportWord(Long userId, HttpServletResponse response) throws Exception {
        List<Run> runs = runRepo.findByUserId(userId);

        XWPFDocument doc = new XWPFDocument();

        for (Run r : runs) {
            XWPFParagraph p = doc.createParagraph();
            XWPFRun run = p.createRun();
            run.setText("Distance: " + r.getDistance() +
                    " | Time: " + r.getTime() +
                    " | Calories: " + r.getCalories());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition", "attachment; filename=runs.docx");

        doc.write(response.getOutputStream());
        doc.close();
    }
}
