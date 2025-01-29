package com.example.Haroon.controller;

import com.example.Haroon.service.EmailService;
import com.example.Haroon.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class ExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private EmailService emailService;

    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
   
    @Value("${excel.export.path}")
    private String exportPath;

    @GetMapping("/sendExcelEmail")
    public void sendExcelEmail(@RequestParam(required = false) List<String> memberIds) {
        try {
            // Generate the Excel report
            byte[] excelFile = excelExportService.generateExcelReport(memberIds);
            String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String excelFileName = "IEEE_Mashery_Report_" + timestamp + ".xlsx";

            // Save the Excel file locally
            File file = new File(exportPath + excelFileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(excelFile);
            }
            String recipient = "Haroon.rashid@isteer.com";  
            String subject = "IEEE Mashery Report";
            String body = "Please find the attached Excel report.";

            emailService.sendEmailWithAttachment(recipient, body, subject, file.getAbsolutePath(), excelFileName);

            System.out.println("Excel file saved at: " + file.getAbsolutePath());
            System.out.println("Email sent successfully with the attachment!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
