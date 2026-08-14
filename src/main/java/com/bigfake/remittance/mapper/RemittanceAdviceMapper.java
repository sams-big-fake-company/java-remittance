package com.bigfake.remittance.mapper;

import com.bigfake.remittance.domain.RemittanceAdvice;
import com.bigfake.remittance.dto.AdviceDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RemittanceAdviceMapper {
    @Mapping(target = "status", expression = "java(advice.getStatus() == null ? null : advice.getStatus().name())")
    AdviceDto toDto(RemittanceAdvice advice);
}
