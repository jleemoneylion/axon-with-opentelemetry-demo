package com.example.axonsample.domain.account.query;

import com.example.axonsample.domain.account.query.dto.GetAccountBalanceQuery;
import com.example.axonsample.domain.account.repository.AccountBalanceRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class GetAccountBalanceQueryHandler {
    private final AccountBalanceRepository accountBalanceRepository;

    public GetAccountBalanceQueryHandler(AccountBalanceRepository accountBalanceRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @QueryHandler
    public BigDecimal handle(GetAccountBalanceQuery query) {
        return accountBalanceRepository.getAccountBalance(query.getAccountId());
    }
}
