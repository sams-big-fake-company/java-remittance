package com.bigfake.remittance.mapper;

import com.bigfake.remittance.domain.RemittanceLine;
import com.bigfake.remittance.dto.LineDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemittanceLineMapper {
    @Mapping(target = "status", expression = "java(line.getStatus() == null ? null : line.getStatus().name())")
    @Mapping(target = "rejectReason", expression = "java(line.getRejectReason() == null ? null : line.getRejectReason().name())")
    @Mapping(target = "matchType", expression = "java(line.getMatchType() == null ? null : line.getMatchType().name())")
    LineDto toDto(RemittanceLine line);
}
