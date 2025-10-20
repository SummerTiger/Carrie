# Email Configuration Guide

This guide explains how to configure and use the SMTP email services in the Vending Inventory Management System for automated reporting.

## Overview

The system supports:
- Automated scheduled email reports (daily, weekly, monthly)
- Custom email templates using Thymeleaf
- Gmail, SendGrid, Amazon SES, or any SMTP provider
- Async email sending (non-blocking)

## Quick Start

### 1. Configure SMTP Settings

Add these environment variables:

```bash
# SMTP Server Configuration
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password

# Email Settings
export EMAIL_ENABLED=true
export EMAIL_FROM=noreply@vendingsystem.com
export EMAIL_FROM_NAME="Vending Inventory System"

# Report Settings
export EMAIL_REPORTS_ENABLED=true
export EMAIL_DEFAULT_RECIPIENT=admin@example.com
export EMAIL_RECIPIENTS=user1@example.com,user2@example.com
```

### 2. Gmail Configuration

For Gmail, you need to create an **App Password**:

1. Go to your Google Account settings
2. Navigate to Security > 2-Step Verification
3. Scroll to "App passwords"
4. Generate a new app password
5. Use this password (not your regular Gmail password) for `SMTP_PASSWORD`

**Gmail Settings:**
```
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-email@gmail.com
SMTP_PASSWORD=your-16-char-app-password
```

### 3. Other SMTP Providers

**SendGrid:**
```
SMTP_HOST=smtp.sendgrid.net
SMTP_PORT=587
SMTP_USERNAME=apikey
SMTP_PASSWORD=your-sendgrid-api-key
```

**Amazon SES:**
```
SMTP_HOST=email-smtp.us-east-1.amazonaws.com
SMTP_PORT=587
SMTP_USERNAME=your-ses-smtp-username
SMTP_PASSWORD=your-ses-smtp-password
```

**Mailgun:**
```
SMTP_HOST=smtp.mailgun.org
SMTP_PORT=587
SMTP_USERNAME=your-mailgun-username
SMTP_PASSWORD=your-mailgun-password
```

## Email Templates

Email templates are located in `src/main/resources/templates/emails/`.

### Available Templates

1. **daily-inventory-report.html** - Daily inventory summary with low stock alerts

### Template Variables

Daily report template accepts:
- `reportDate` - Date of the report
- `totalProducts` - Total number of products
- `totalMachines` - Total vending machines
- `lowStockCount` - Number of low stock items
- `totalValue` - Total inventory value
- `lowStockProducts` - List of products with low stock
- `recentTransactions` - Recent activity log

### Creating Custom Templates

Create a new HTML file in `src/main/resources/templates/emails/`:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>My Custom Report</title>
</head>
<body>
    <h1 th:text="${title}">Report Title</h1>
    <p th:text="${message}">Report content</p>

    <!-- Loop through data -->
    <ul>
        <li th:each="item : ${items}" th:text="${item.name}">Item</li>
    </ul>
</body>
</html>
```

## Using EmailService

### Send Simple Email

```java
@Autowired
private EmailService emailService;

public void sendNotification() {
    emailService.sendSimpleEmail(
        "user@example.com",
        "Low Stock Alert",
        "Product XYZ is running low on stock."
    );
}
```

### Send Template Email

```java
@Autowired
private EmailService emailService;

public void sendInventoryReport() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("reportDate", LocalDate.now());
    variables.put("totalProducts", 100);
    variables.put("lowStockCount", 5);
    // ... add more variables

    emailService.sendTemplateEmail(
        "admin@example.com",
        "Daily Inventory Report",
        "emails/daily-inventory-report",
        variables
    );
}
```

### Send to Multiple Recipients

```java
String[] recipients = {"user1@example.com", "user2@example.com"};
emailService.sendTemplateEmailToMultiple(
    recipients,
    "Weekly Report",
    "emails/weekly-report",
    variables
);
```

## Scheduled Reports

### Configuration

Report scheduling is configured via cron expressions in `application.yml`:

```yaml
app:
  email:
    report:
      enabled: true
      daily-cron: "0 0 8 * * *"      # 8 AM daily
      weekly-cron: "0 0 8 * * MON"   # 8 AM Monday
      monthly-cron: "0 0 8 1 * *"    # 8 AM 1st of month
```

### Cron Expression Examples

```
"0 0 8 * * *"       - 8:00 AM every day
"0 30 9 * * MON"    - 9:30 AM every Monday
"0 0 8 1 * *"       - 8:00 AM first day of month
"0 0 8 * * MON-FRI" - 8:00 AM weekdays only
"0 0 */6 * * *"     - Every 6 hours
```

## Testing Email Configuration

### 1. Enable Email in Development

```bash
EMAIL_ENABLED=true
SMTP_HOST=smtp.gmail.com
SMTP_PORT=587
SMTP_USERNAME=your-test-email@gmail.com
SMTP_PASSWORD=your-app-password
```

### 2. Check Logs

When email is disabled, you'll see:
```
Email disabled. Would have sent to user@example.com: Test Subject
```

When email succeeds:
```
Email sent successfully to user@example.com: Test Subject
```

When email fails:
```
Failed to send email to user@example.com: Test Subject
```

### 3. Test with MailHog (Development)

For local testing without real SMTP:

```bash
# Run MailHog
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog

# Configure
SMTP_HOST=localhost
SMTP_PORT=1025
SMTP_USERNAME=
SMTP_PASSWORD=
```

View emails at: http://localhost:8025

## Production Deployment

### Docker Environment Variables

Add to `docker-compose.yml`:

```yaml
backend:
  environment:
    # SMTP Configuration
    SMTP_HOST: smtp.gmail.com
    SMTP_PORT: 587
    SMTP_USERNAME: ${SMTP_USERNAME}
    SMTP_PASSWORD: ${SMTP_PASSWORD}

    # Email Configuration
    EMAIL_ENABLED: "true"
    EMAIL_FROM: noreply@yourcompany.com
    EMAIL_FROM_NAME: "Vending System"

    # Reports
    EMAIL_REPORTS_ENABLED: "true"
    EMAIL_DEFAULT_RECIPIENT: admin@yourcompany.com
    EMAIL_RECIPIENTS: "user1@example.com,user2@example.com"
```

### Security Best Practices

1. **Never commit credentials** - Use environment variables or secrets management
2. **Use app passwords** - For Gmail, never use your account password
3. **Enable TLS/SSL** - Always use secure connections (configured by default)
4. **Limit recipients** - Don't expose customer emails
5. **Rate limiting** - Most SMTP providers have sending limits

### Common SMTP Limits

- **Gmail**: 500 emails/day (free), 2000/day (Google Workspace)
- **SendGrid**: 100 emails/day (free), unlimited (paid)
- **Amazon SES**: 62,000 emails/month (free tier)
- **Mailgun**: 5,000 emails/month (free)

## Troubleshooting

### Email not sending

1. Check `EMAIL_ENABLED=true`
2. Verify SMTP credentials
3. Check firewall/network allows port 587
4. Review application logs for errors

### Gmail "Less secure app" error

- Use App Passwords (recommended)
- Enable 2-Step Verification first

### Emails going to spam

- Configure SPF, DKIM, and DMARC DNS records
- Use a verified sending domain
- Avoid spam trigger words
- Include unsubscribe links

### Template not found

- Ensure template path is correct: `emails/template-name` (no .html extension)
- Check template is in `src/main/resources/templates/emails/`
- Verify Thymeleaf dependency is included

## Next Steps

For implementation of advanced reporting and analytics with charting:
- Dashboard with Charts (Chart.js / Recharts)
- Sales Analytics
- Inventory Trends
- Machine Performance Metrics
- Revenue Reports

**Reminder**: Advanced Reporting & Analytics with charting is a planned feature for future implementation.
