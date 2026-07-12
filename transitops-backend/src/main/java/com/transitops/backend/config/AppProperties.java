package com.transitops.backend.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();
    private Storage storage = new Storage();
    private BootstrapAdmin bootstrapAdmin = new BootstrapAdmin();
    private LicenseReminder licenseReminder = new LicenseReminder();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private Duration accessTokenTtl = Duration.ofMinutes(30);
        private Duration refreshTokenTtl = Duration.ofDays(7);
        private String issuer = "transitops-api";
    }

    @Getter
    @Setter
    public static class Cors {
        private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:5173"));
    }

    @Getter
    @Setter
    public static class Storage {
        private Path uploadDir = Path.of("./uploads");
        private long maxFileSizeBytes = 10 * 1024 * 1024;
    }

    @Getter
    @Setter
    public static class BootstrapAdmin {
        private String name = "TransitOps Admin";
        private String email = "admin@transitops.com";
        private String password = "Admin@123";
    }

    @Getter
    @Setter
    public static class LicenseReminder {
        private boolean enabled = false;
        private int daysAhead = 30;
        private String recipient;
    }
}
