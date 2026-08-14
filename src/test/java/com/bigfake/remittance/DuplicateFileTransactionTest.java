package com.bigfake.remittance;

import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.enums.FileStatus;
import com.bigfake.remittance.domain.enums.RejectReason;
import com.bigfake.remittance.exception.ConflictException;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import com.bigfake.remittance.service.RemittanceProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class DuplicateFileTransactionTest {

    @Autowired
    private RemittanceProcessingService service;

    @Autowired
    private RemittanceFileRepository files;

    @Test
    void duplicateUploadLeavesDurableRejectedAuditRow() {
        byte[] content = ("H|BANK|20260810|TX-duplicate-audit|1|5.00\n"
                + "D|UNKNOWN-DUP|INV-DUP|5.00|5.00||20260810|duplicate audit\n"
                + "T|1|5.00").getBytes(StandardCharsets.UTF_8);

        long originalId = service.ingest("duplicate-audit.txt", content).getId();

        assertThrows(ConflictException.class,
                () -> service.ingest("duplicate-audit-second.txt", content));

        List<RemittanceFile> duplicateRows = files.findAllByChecksum(
                        com.bigfake.remittance.util.ChecksumUtils.sha256(content))
                .stream()
                .filter(file -> file.getRejectReason() == RejectReason.DUPLICATE_FILE)
                .collect(Collectors.toList());

        assertEquals(1, duplicateRows.size());
        assertEquals(FileStatus.REJECTED, duplicateRows.get(0).getStatus());
        assertEquals(originalId, duplicateRows.get(0).getOriginalFileId());
    }
}
