package com.example.Haroon.service;

import com.example.Haroon.model.IPGeolocation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;

@Service
public class IPGeolocationService {

    private static final Logger logger = LoggerFactory.getLogger(IPGeolocationService.class);

    @Value("${API_URL}")
    private String Geourl;

    private int successfulApiCalls = 0;
    private int failedApiCalls = 0;

    public String getCountryFromIP(String ipAddress) {
        logger.info("Fetching country for IP address: {}", ipAddress);
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = Geourl + ipAddress;
        int maxRetries = 5; 
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                logger.info("Making API call to: {}", apiUrl);
                ResponseEntity<IPGeolocation> response = restTemplate.getForEntity(apiUrl, IPGeolocation.class);
                if (response.getStatusCode().value() == 200) {
                    IPGeolocation geoData = response.getBody();
                    if (geoData != null && "success".equalsIgnoreCase(geoData.getStatus())) {
                        logger.info("Successfully fetched country for IP {}: {}", ipAddress, geoData.getCountry());
                        successfulApiCalls++;
                        return geoData.getCountry();
                    } else {
                        logger.warn("Failed to fetch region for IP: {}", ipAddress);
                        failedApiCalls++;
                        return null;
                    }
                }
            } catch (HttpClientErrorException.TooManyRequests e) {
                retryCount++;
                logger.warn("Received HTTP 429 (Too Many Requests). Retrying in 10 seconds... (Attempt {}/{})", retryCount, maxRetries);
                try {
                    Thread.sleep(10000); // Wait for 10 seconds before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    logger.error("Retry interrupted for IP: {}", ipAddress);
                    break;
                }
            } catch (Exception e) {
                logger.error("Error fetching data for IP: {} - {}", ipAddress, e.getMessage());
                failedApiCalls++;
                break; // Exit loop on other exceptions
            }
        }

        logger.error("Max retries reached for IP: {}. Skipping.", ipAddress);
        failedApiCalls++;
        return null;
    }

    public void fetchAndSaveRegions(String inputFilePath, String outputFilePath) {
        logger.info("Starting to process the Excel file: {}", inputFilePath);
        try (Workbook workbook = new XSSFWorkbook(new File(inputFilePath))) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            int countryColumnIndex = -1;
            if (rows.hasNext()) {
                Row headerRow = rows.next();
                for (Cell cell : headerRow) {
                    if ("Country of Origin".equalsIgnoreCase(cell.getStringCellValue())) {
                        countryColumnIndex = cell.getColumnIndex();
                        break;
                    }
                }
            }

            if (countryColumnIndex == -1) {
                throw new IllegalArgumentException("Country of Origin column not found in the Excel file");
            }

            // Process each row and fetch country from IP
            while (rows.hasNext()) {
                Row row = rows.next();
                Cell ipCell = row.getCell(countryColumnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                String ipAddress = ipCell.getStringCellValue();

                if (!ipAddress.isEmpty()) {
                    logger.info("Processing IP address: {}", ipAddress);
                    String resolvedCountry = getCountryFromIP(ipAddress);

                    // Update the Excel file with the resolved country
                    if (resolvedCountry != null) {
                        Cell countryCell = row.getCell(countryColumnIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        countryCell.setCellValue(resolvedCountry);
                        logger.info("Updated IP {} with country: {}", ipAddress, resolvedCountry);
                    } else {
                        logger.warn("API failed for IP: {}, keeping IP address unchanged", ipAddress);
                    }
                }
            }
            try (FileOutputStream fos = new FileOutputStream(new File(outputFilePath))) {
                workbook.write(fos);
                logger.info("Updated Excel file saved to: {}", outputFilePath);
            }

        } catch (Exception e) {
            logger.error("Error while processing the Excel file: {}", e.getMessage());
        }

        // Log API call statistics
        logger.info("Total successful API calls: {}", successfulApiCalls);
        logger.info("Total failed API calls: {}", failedApiCalls);
    }
}
