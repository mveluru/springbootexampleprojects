package org.bee.banking.component;

import org.bee.banking.domain.Customer;
import org.bee.banking.domain.Withdrawal;
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
    Withdrawal toWithdrawalEntity(WithdrawalRequest request);

    // address maps implicitly; WithdrawalRequest carries no date of birth
    @Mapping(target = "dateOfBirth", ignore = true)
    Customer toWithdrawalCustomerEntity(WithdrawalRequest request);
}
