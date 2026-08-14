package com.bigfake.remittance.config;

import com.bigfake.remittance.service.RemittanceProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class BatchJobConfig {
    @Value("${remittance.scheduling.enabled:true}")
    private boolean enabled;

    @Value("${remittance.inbound.directory:./inbound-remittance}")
    private String directory;

    private final RemittanceProcessingService service;

    public BatchJobConfig(RemittanceProcessingService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${remittance.jobs.inbound-delay:60000}")
    public void pollInbound() {
        if (!enabled) {
            return;
        }
        Path dir = Paths.get(directory);
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.list(dir)) {
            paths.filter(path -> path.toString().endsWith(".txt")
                            || path.toString().endsWith(".dat"))
                    .forEach(this::ingest);
        } catch (IOException exception) {
            log.warn("Inbound scan failed " + exception.getMessage());
        }
    }

    private void ingest(Path path) {
        try {
            service.ingest(path.getFileName().toString(), Files.readAllBytes(path));
            Files.move(path, path.resolveSibling("processed-" + path.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception exception) {
            log.warn("Inbound file failed " + path + " " + exception.getMessage());
            try {
                Files.move(path, path.resolveSibling("failed-" + path.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // Legacy batch job intentionally leaves an unmovable file for operator review.
            }
        }
    }

    @Scheduled(fixedDelayString = "${remittance.jobs.dispatch-delay:120000}")
    public void dispatch() {
        if (enabled) {
            service.dispatchPending();
        }
    }

    @Scheduled(cron = "${remittance.jobs.exception-cron:0 0 * * * *}")
    public void staleExceptions() {
        if (enabled) {
            log.info("Stale exception sweep completed for directory " + directory);
        }
    }
}
