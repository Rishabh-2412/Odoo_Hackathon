package com.transitops.backend.notification.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.transitops.backend.config.AppProperties;
import com.transitops.backend.driver.model.Driver;
import com.transitops.backend.driver.repository.DriverRepository;
import com.transitops.backend.notification.dto.LicenseExpiryAlert;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LicenseReminderService {
    private static final Logger log = LoggerFactory.getLogger(LicenseReminderService.class);

    private final DriverRepository driverRepository;
    private final JavaMailSender mailSender;
    private final AppProperties properties;

    @Transactional(readOnly = true)
    public List<LicenseExpiryAlert> expiringWithin(int days) {
        LocalDate today = LocalDate.now();
        return driverRepository.findByLicenseExpiryDateBetween(today, today.plusDays(days)).stream()
                .map(driver -> toAlert(driver, today))
                .toList();
    }

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        if (!properties.getLicenseReminder().isEnabled()) {
            return;
        }
        List<Driver> drivers = driverRepository.findByLicenseExpiryDateBetween(
                LocalDate.now(), LocalDate.now().plusDays(properties.getLicenseReminder().getDaysAhead()));
        for (Driver driver : drivers) {
            String recipient = driver.getEmail() != null && !driver.getEmail().isBlank()
                    ? driver.getEmail()
                    : properties.getLicenseReminder().getRecipient();
            if (recipient == null || recipient.isBlank()) {
                log.warn("No email recipient configured for driver license reminder: {}", driver.getId());
                continue;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(recipient);
                message.setSubject("TransitOps: driving license expiry reminder");
                message.setText("Driver " + driver.getName() + " (license " + driver.getLicenseNumber()
                        + ") has a license expiry date of " + driver.getLicenseExpiryDate() + ".");
                mailSender.send(message);
            } catch (RuntimeException ex) {
                log.error("Unable to send license reminder for driver {}", driver.getId(), ex);
            }
        }
    }

    private LicenseExpiryAlert toAlert(Driver driver, LocalDate today) {
        return new LicenseExpiryAlert(
                driver.getId(), driver.getName(), driver.getLicenseNumber(), driver.getLicenseExpiryDate(),
                ChronoUnit.DAYS.between(today, driver.getLicenseExpiryDate()), driver.getEmail());
    }
}
