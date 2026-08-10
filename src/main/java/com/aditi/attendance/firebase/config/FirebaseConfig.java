package com.aditi.attendance.firebase.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Profile("!local")
public class FirebaseConfig {

    private final boolean enabled;
    private final String credentialsPath;

    public FirebaseConfig(
            @Value("${firebase.enabled:true}") boolean enabled,
            @Value("${firebase.credentials-path:}") String credentialsPath) {
        this.enabled = enabled;
        this.credentialsPath = credentialsPath;
    }

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            return;
        }

        if (credentialsPath.isBlank()) {
            throw new IllegalStateException(
                    "Firebase is enabled but firebase.credentials-path is not configured. "
                            + "Set it to the absolute path of your service-account JSON file."
            );
        }

        try (InputStream serviceAccount = new FileInputStream(credentialsPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Could not load Firebase credentials from " + credentialsPath,
                    exception
            );
        }
    }
}
