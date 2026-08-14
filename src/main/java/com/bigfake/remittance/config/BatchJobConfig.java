package com.bigfake.remittance.config;
import com.bigfake.remittance.client.CashApplicationClient; import com.bigfake.remittance.service.RemittanceProcessingService; import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*; import org.springframework.context.annotation.Configuration; import org.springframework.scheduling.annotation.Scheduled;
import java.io.*; import java.nio.file.*; import java.util.stream.*;
@Configuration @Slf4j
public class BatchJobConfig {
    @Value("${remittance.scheduling.enabled:true}") private boolean enabled; @Value("${remittance.inbound.directory:./inbound-remittance}") private String directory;
    private final RemittanceProcessingService service; public BatchJobConfig(RemittanceProcessingService service){this.service=service;}
    @Scheduled(fixedDelayString="${remittance.jobs.inbound-delay:60000}") public void pollInbound(){if(!enabled)return;Path dir=Paths.get(directory);if(!Files.exists(dir))return;try(Stream<Path> paths=Files.list(dir)){paths.filter(p->p.toString().endsWith(".txt")||p.toString().endsWith(".dat")).forEach(this::ingest);}catch(IOException e){log.warn("Inbound scan failed "+e.getMessage());}}
    private void ingest(Path p){try{service.ingest(p.getFileName().toString(),Files.readAllBytes(p));Files.move(p,p.resolveSibling("processed-"+p.getFileName()),StandardCopyOption.REPLACE_EXISTING);}catch(Exception e){log.warn("Inbound file failed "+p+" "+e.getMessage());try{Files.move(p,p.resolveSibling("failed-"+p.getFileName()),StandardCopyOption.REPLACE_EXISTING);}catch(IOException ignored){}}}
    @Scheduled(fixedDelayString="${remittance.jobs.dispatch-delay:120000}") public void dispatch(){if(enabled)service.dispatchPending();}
    @Scheduled(cron="${remittance.jobs.exception-cron:0 0 * * * *}") public void staleExceptions(){if(enabled)log.info("Stale exception sweep completed for directory "+directory);}
}
