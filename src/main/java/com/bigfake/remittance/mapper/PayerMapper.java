package com.bigfake.remittance.mapper;

import com.bigfake.remittance.domain.Payer;
import com.bigfake.remittance.domain.PostalAddress;
import com.bigfake.remittance.dto.PayerDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PayerMapper {
    @Mapping(target = "status", expression = "java(payer.getStatus() == null ? null : payer.getStatus().name())")
    @Mapping(target = "addressLine1", source = "address.addressLine1")
    @Mapping(target = "addressLine2", source = "address.addressLine2")
    @Mapping(target = "city", source = "address.city")
    @Mapping(target = "state", source = "address.state")
    @Mapping(target = "postalCode", source = "address.postalCode")
    @Mapping(target = "country", source = "address.country")
    PayerDto toDto(Payer payer);

    default PostalAddress address(PayerDto dto) {
        PostalAddress address = new PostalAddress();
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        return address;
    }
}
