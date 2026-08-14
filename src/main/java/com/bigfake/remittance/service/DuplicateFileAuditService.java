package com.bigfake.remittance.service;

import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.repository.RemittanceFileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DuplicateFileAuditService {
    private final RemittanceFileRepository files;

    public DuplicateFileAuditService(RemittanceFileRepository files) {
        this.files = files;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RemittanceFile saveRejectedDuplicate(RemittanceFile rejected) {
        return files.save(rejected);
    }
}
