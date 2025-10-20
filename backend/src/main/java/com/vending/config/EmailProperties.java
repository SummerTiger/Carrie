package com.vending.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    private String from;
    private String fromName;
    private boolean enabled = true;
    private Report report = new Report();

    @Data
    public static class Report {
        private boolean enabled = true;
        private String defaultRecipient;
        private String[] recipients;
        private String dailyCron = "0 0 8 * * *";  // 8 AM daily
        private String weeklyCron = "0 0 8 * * MON";  // 8 AM Monday
        private String monthlyCron = "0 0 8 1 * *";  // 8 AM 1st of month
    }
}
