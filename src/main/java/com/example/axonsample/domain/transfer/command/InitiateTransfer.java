package com.example.axonsample.domain.transfer.command;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class InitiateTransfer {
    @TargetAggregateIdentifier
    String transferId;
    String fromAccountId;
    String toAccountId;
    BigDecimal amount;
}
