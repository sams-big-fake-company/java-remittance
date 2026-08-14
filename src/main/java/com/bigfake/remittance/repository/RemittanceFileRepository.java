package com.bigfake.remittance.repository;

import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.domain.enums.FileStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface RemittanceFileRepository extends JpaRepository<RemittanceFile, Long> {
    Optional<RemittanceFile> findByChecksum(String checksum);
    List<RemittanceFile> findAllByChecksum(String checksum);
    Page<RemittanceFile> findByStatus(FileStatus status, Pageable pageable);
    @Query("select f from RemittanceFile f left join fetch f.lines where f.id = :id")
    Optional<RemittanceFile> findWithLines(@Param("id") Long id);
}
