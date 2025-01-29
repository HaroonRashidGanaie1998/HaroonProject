package com.example.Haroon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.Haroon.service.MemberService;
import com.example.Haroon.service.TokenGenerationService;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.example.Haroon")
public class ExportInExcelApplication {
    
 @Autowired
  TokenGenerationService tokenService;
 @Autowired
   MemberService memberService;
    
    public ExportInExcelApplication(TokenGenerationService tokenService, MemberService memberService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
    }

    public static void main(String[] args) {
        SpringApplication.run(ExportInExcelApplication.class, args);
    }

     //Scheduled method to run periodically based on cron expression
    @Scheduled(cron = "${schedule.cron}")
    public void runCronJob() {
        String token = tokenService.getToken();
        memberService.fetchMembersInBatches(token);
    }
}
