package com.example.axonsample.domain.account.command;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
@Jacksonized
public class CreateAccount {
    @TargetAggregateIdentifier
    String accountId;
}
