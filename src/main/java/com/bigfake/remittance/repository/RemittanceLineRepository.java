package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.domain.enums.LineStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface RemittanceLineRepository extends JpaRepository<RemittanceLine, Long> {
    Page<RemittanceLine> findByStatus(LineStatus status, Pageable pageable);
    Page<RemittanceLine> findByStatusAndPayerCode(LineStatus status, String payerCode, Pageable pageable);
    List<RemittanceLine> findByRemittanceFileIdAndStatus(Long fileId, LineStatus status);
}
