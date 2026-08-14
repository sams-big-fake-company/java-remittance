package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.domain.enums.AdviceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
public interface RemittanceAdviceRepository extends JpaRepository<RemittanceAdvice, Long> {
    Optional<RemittanceAdvice> findByAdviceNumber(String number);
    Page<RemittanceAdvice> findByPayerCodeAndStatus(String payer, AdviceStatus status, Pageable page);
    Page<RemittanceAdvice> findByPayerCode(String payer, Pageable page);
    long countByRemittanceDate(LocalDate remittanceDate);
}
