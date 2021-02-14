package com.example.axonsample.domain.account.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class AccountCreated {
    String accountId;
    Instant timestamp;
}
