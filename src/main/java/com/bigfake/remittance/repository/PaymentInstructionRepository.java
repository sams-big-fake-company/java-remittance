package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.PaymentInstruction; import com.bigfake.remittance.domain.enums.StatusEnums.InstructionStatus;
import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface PaymentInstructionRepository extends JpaRepository<PaymentInstruction,Long> {
    List<PaymentInstruction> findByStatus(InstructionStatus status);
}
