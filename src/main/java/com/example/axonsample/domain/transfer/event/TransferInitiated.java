package com.example.axonsample.domain.transfer.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TransferInitiated {
    String transferId;
    String fromAccountId;
    String toAccountId;
    BigDecimal amount;
    Instant timestamp;
}
