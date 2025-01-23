package com.example.Haroon.controller;

import com.example.Haroon.service.EmailService;
import com.example.Haroon.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
            File excelFileToZip = new File(exportPath + excelFileName);
            try (FileOutputStream fos = new FileOutputStream(excelFileToZip)) {
                fos.write(excelFile);
            }

            // Compress the Excel file to a .7z format
            String sevenZipFilePath = exportPath; // Base path for the 7z file
            String sevenZipFileName = createSevenZipFile(sevenZipFilePath, excelFileToZip);

            // Send the email with the 7z file as an attachment
            String recipient = "Haroon.rashid@isteer.com";
            String subject = "IEEE Mashery Report";
            String body = "Please find the attached Excel report in compressed format.";

            emailService.sendEmailWithAttachment(recipient, body, subject, sevenZipFileName, new File(sevenZipFileName).getName());

            System.out.println("Excel file saved at: " + excelFileToZip.getAbsolutePath());
            System.out.println("Compressed file saved at: " + sevenZipFileName);
            System.out.println("Email sent successfully with the compressed attachment!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String createSevenZipFile(String sevenZipFilePath, File fileToZip) throws IOException {
        String outputSevenZipFile = Paths.get(sevenZipFilePath, fileToZip.getName().replace(".xlsx", "_" + getCurrentDateTime() + ".7z")).toString();
        try (FileOutputStream fos = new FileOutputStream(outputSevenZipFile);
             ZipOutputStream zipOut = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
        return outputSevenZipFile;
    }

    private String getCurrentDateTime() {
        return new SimpleDateFormat(DATE_FORMAT).format(new Date());
    }
}
