package com.bigfake.remittance.mapper;
import com.bigfake.remittance.domain.RemittanceFile; import com.bigfake.remittance.dto.FileSummaryDto; import org.mapstruct.Mapper;
@Mapper(componentModel = "spring")
public interface RemittanceFileMapper { FileSummaryDto toSummary(RemittanceFile file); }
