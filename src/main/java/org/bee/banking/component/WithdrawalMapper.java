package org.bee.banking.component;

import org.bee.banking.domain.Address;
import org.bee.banking.domain.WithdrawalForm;
import org.bee.banking.request.WithdrawalRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface WithdrawalMapper {
    WithdrawalMapper INSTANCE = Mappers.getMapper(WithdrawalMapper.class);

    // accountType, firstName, lastName map implicitly (matching names/types)
    @Mapping(target = "AccountNumber", source = "accountNumber")
    @Mapping(target = "withdrawalAmount", source = "withdrawAmount")
    @Mapping(target = "withdrawalStatus", ignore = true) // set by business logic on processing
    @Mapping(target = "withdrawalDate", ignore = true) // keep entity's LocalDate.now() default
    @Mapping(target = "address", source = "request")
    WithdrawalForm toWithdrawalEntity(WithdrawalRequest request);


    @Mapping(target = "street", source = "street")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "state", source = "state")
    @Mapping(target = "zip", source = "zip")
    @Mapping(target = "addressLine1", source = "addressLine1")
    @Mapping(target = "addressLine2", source = "addressLine2")
    @Mapping(target = "country", ignore = true) // Handled by @Builder.Default in entity
    Address toWithdrawalCustomerAddressEntity(WithdrawalRequest request);
}
