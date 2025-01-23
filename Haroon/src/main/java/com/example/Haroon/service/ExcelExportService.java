package com.example.Haroon.service;

import com.example.Haroon.model.ApplicationUsers;
import com.example.Haroon.model.Members;
import com.example.Haroon.model.PackageUsers;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    @Autowired
    private MemberService memberService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private PackageKeyService packageKeyService;
    
    @Autowired
    private TokenGenerationService tokenGenerationService;

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    public byte[] generateExcelReport(List<String> memberIds) throws Exception {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(); 
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            
            String token = tokenGenerationService.getToken();
            List<Members> membersList = fetchMembers(token, memberIds);
            Sheet sheet = workbook.createSheet("Combined Data");
            createHeaderRow(sheet);

            for (Members member : membersList) {
                populateMemberData(sheet, member, token);
            }

            workbook.write(byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        }
    }

    private List<Members> fetchMembers(String token, List<String> memberIds) {
        if (memberIds != null && !memberIds.isEmpty()) {
            return memberService.fetchMembersInBatches(token);
        } else {
            return memberService.fetchMembersInBatches(token);
        }
    }

    private void populateMemberData(Sheet sheet, Members member, String token) {
        String memberId = member.getId();
        List<ApplicationUsers> applicationData = applicationService.fetchApplicationDetailsForMember(memberId, token);

        if (applicationData.isEmpty()) {
            Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
            insertMemberBasicData(row, member);
            insertEmptyApplicationData(row);
            insertEmptyPackageData(row);
        } else {
            for (ApplicationUsers application : applicationData) {
                List<PackageUsers> packageData = packageKeyService.fetchPackageDetailsForMember(token, memberId, application.getId());

                if (packageData.isEmpty()) {
                    Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                    insertMemberBasicData(row, member);
                    insertApplicationData(row, application);
                    insertEmptyPackageData(row);
                } else {
                    for (PackageUsers packageUser : packageData) {
                        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                        insertMemberBasicData(row, member);
                        insertApplicationData(row, application);
                        insertPackageData(row, packageUser);
                    }
                }
            }
        }
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(1).setCellValue("Date");
        headerRow.createCell(2).setCellValue("Email");
        headerRow.createCell(3).setCellValue("Customer Name");
        headerRow.createCell(4).setCellValue("Institution/Organization");
        headerRow.createCell(5).setCellValue("Country of Origin");
        headerRow.createCell(6).setCellValue("Use Case");
        headerRow.createCell(7).setCellValue("API Key Status");
        headerRow.createCell(8).setCellValue("Type of Institution");
        headerRow.createCell(9).setCellValue("User Name");
        headerRow.createCell(10).setCellValue("API Key");
    }

    private void insertMemberBasicData(Row row, Members member) {
        String customerName = getOrDefault(member.getFirstName()) + " " + getOrDefault(member.getLastName());
        String date = formatDate(getOrDefault(member.getCreated()));
        String countryOfOrigin_IpAddress = getOrDefault(member.getCountryCode(), member.getRegistrationIpaddr());
        String username = getOrDefault(member.getUsername());

        row.createCell(1).setCellValue(date);
        row.createCell(2).setCellValue(getOrDefault(member.getEmail()));
        row.createCell(3).setCellValue(customerName);
        row.createCell(5).setCellValue(countryOfOrigin_IpAddress);
        row.createCell(9).setCellValue(username);
    }

    private void insertApplicationData(Row row, ApplicationUsers application) {
        String organizationType = application.getOrganization_type();
        String typeOfInstitution = parseOrganizationType(organizationType);
        String useCase = getOrDefault(application.getDescription());
        String institutionOrOrganization = getOrDefault(application.getCompany());
        row.createCell(4).setCellValue(institutionOrOrganization);
        row.createCell(6).setCellValue(useCase);
        row.createCell(8).setCellValue(typeOfInstitution);
    }
    private void insertEmptyApplicationData(Row row) {
    	row.createCell(4).setCellValue("");
        row.createCell(6).setCellValue(""); 
        row.createCell(8).setCellValue(""); 
    }

    private void insertPackageData(Row row, PackageUsers packageUser) {
        String APIKey = getOrDefault(packageUser.getApikey());
        String APIKeyStatus = getOrDefault(packageUser.getStatus());
        row.createCell(7).setCellValue(APIKeyStatus);
        row.createCell(10).setCellValue(APIKey);
    }
    private void insertEmptyPackageData(Row row) {
        row.createCell(7).setCellValue("");  
        row.createCell(10).setCellValue(""); 
    }

    private String getOrDefault(String value) {
        return value != null ? value : "N/A";
    }

    private String getOrDefault(String value, String fallbackValue) {
        return value != null && !value.isEmpty() ? value : (fallbackValue != null ? fallbackValue : "N/A");
    }

    private String formatDate(String date) {
        try {
            return Instant.parse(date)
                    .atZone(ZoneOffset.UTC)
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        } catch (Exception e) {
            return date != null ? date : "N/A";
        }
    }

    private String parseOrganizationType(String organizationType) {
        if (organizationType != null && !organizationType.trim().isEmpty()) {
            try {
                int organizationTypeInt = Integer.parseInt(organizationType.trim());
                return getTypeOfInstitution(organizationTypeInt);
            } catch (NumberFormatException e) {
                return "Invalid format";
            }
        }
        return "N/A";
    }

    private String getTypeOfInstitution(int organizationType) {
        switch (organizationType) {
            case 1: return "Academic";
            case 2: return "Corporate";
            case 3: return "Government";
            default: return "N/A";
        }
    }
}
