package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.RemittanceLine; import com.bigfake.remittance.domain.enums.StatusEnums.LineStatus;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import java.time.*; import java.util.*;
public interface RemittanceLineRepository extends JpaRepository<RemittanceLine,Long> {
    Page<RemittanceLine> findByStatus(LineStatus status, Pageable pageable);
    Page<RemittanceLine> findByStatusAndPayerCode(LineStatus status, String payerCode, Pageable pageable);
    List<RemittanceLine> findByRemittanceFileIdAndStatus(Long fileId, LineStatus status);
}
