package com.bigfake.remittance.repository;
import com.bigfake.remittance.domain.RemittanceFile; import com.bigfake.remittance.domain.enums.StatusEnums.FileStatus;
import org.springframework.data.domain.*; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import java.util.*;
public interface RemittanceFileRepository extends JpaRepository<RemittanceFile,Long> {
    Optional<RemittanceFile> findByChecksum(String checksum);
    Page<RemittanceFile> findByStatus(FileStatus status, Pageable pageable);
    @Query("select f from RemittanceFile f left join fetch f.lines where f.id = :id") Optional<RemittanceFile> findWithLines(@Param("id") Long id);
}
