package com.bigfake.remittance.repository;

import com.bigfake.remittance.domain.Payer;
import com.bigfake.remittance.domain.enums.PayerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayerRepository extends JpaRepository<Payer, Long> {
    Optional<Payer> findByPayerCode(String payerCode);
    Page<Payer> findByStatus(PayerStatus status, Pageable pageable);
}
