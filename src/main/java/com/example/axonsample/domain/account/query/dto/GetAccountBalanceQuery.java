package com.example.axonsample.domain.account.query.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GetAccountBalanceQuery {
    String accountId;
}
