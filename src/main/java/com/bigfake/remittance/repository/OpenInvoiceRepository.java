package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.OpenInvoice; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param;
import java.math.BigDecimal; import java.util.*;
public interface OpenInvoiceRepository extends JpaRepository<OpenInvoice,Long> {
    @Query("select i from OpenInvoice i where i.normalizedReference=:reference and i.status <> 'CLOSED'") Optional<OpenInvoice> findOpenByNormalizedReference(@Param("reference") String reference);
    @Query("select i from OpenInvoice i where i.payerCode=:payer and i.outstandingAmount=:amount and i.status in ('OPEN','PARTIALLY_PAID')") List<OpenInvoice> findExactAmount(@Param("payer") String payer, @Param("amount") BigDecimal amount);
    @Query(value="select * from open_invoice where payer_code = :payer and status in ('OPEN','PARTIALLY_PAID') and outstanding_amount between :low and :high", nativeQuery=true)
    List<OpenInvoice> findWithinAmount(@Param("payer") String payer, @Param("low") BigDecimal low, @Param("high") BigDecimal high);
}
