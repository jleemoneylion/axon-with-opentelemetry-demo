package com.example.axonsample.domain.account.command;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class DepositMoney {
    @TargetAggregateIdentifier
    String accountId;
    String transactionId;
    BigDecimal amount;
}
