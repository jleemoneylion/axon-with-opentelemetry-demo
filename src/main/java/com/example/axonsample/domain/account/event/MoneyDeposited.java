package com.example.axonsample.domain.account.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
@Jacksonized
public class MoneyDeposited {
    String accountId;
    String transactionId;
    BigDecimal amount;
    Instant timestamp;
}
