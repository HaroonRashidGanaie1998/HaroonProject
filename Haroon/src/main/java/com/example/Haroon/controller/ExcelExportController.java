package com.example.Haroon.controller;

import com.example.Haroon.service.ExcelExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class ExcelExportController {

    @Autowired
    private ExcelExportService excelExportService;

    // Define the date format pattern
    private static final String DATE_FORMAT = "yyyy/MM/dd_HH-mm-ss";
 
    @GetMapping("/downloadExcel")
    public void downloadExcel(HttpServletResponse response, 
                              @RequestParam(required = false) List<String> memberIds) {
        try {
            byte[] excelFile = excelExportService.generateExcelReport(memberIds);       
            String timestamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            String excelFileName = "IEEE_Mashery_Report_" + timestamp + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + excelFileName);
            response.getOutputStream().write(excelFile);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("Error generating the Excel report.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
}
