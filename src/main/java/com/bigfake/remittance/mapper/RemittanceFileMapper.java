package com.bigfake.remittance.mapper;
import com.bigfake.remittance.domain.RemittanceFile;
import com.bigfake.remittance.dto.FileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemittanceFileMapper {
    @Mapping(target = "status", expression = "java(file.getStatus() == null ? null : file.getStatus().name())")
    @Mapping(target = "rejectReason", expression = "java(file.getRejectReason() == null ? null : file.getRejectReason().name())")
    FileDto toDto(RemittanceFile file);
}
