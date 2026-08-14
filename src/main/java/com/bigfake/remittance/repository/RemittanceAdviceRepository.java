package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.RemittanceAdvice; import com.bigfake.remittance.domain.enums.StatusEnums.AdviceStatus;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import java.util.*;
public interface RemittanceAdviceRepository extends JpaRepository<RemittanceAdvice,Long> {
    Optional<RemittanceAdvice> findByAdviceNumber(String number);
    Page<RemittanceAdvice> findByPayerCodeAndStatus(String payer, AdviceStatus status, Pageable page);
    Page<RemittanceAdvice> findByPayerCode(String payer, Pageable page);
}
