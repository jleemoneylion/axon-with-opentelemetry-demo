package com.example.axonsample.domain.account.projection.accountbalance;

import com.example.axonsample.domain.account.event.AccountCreated;
import com.example.axonsample.domain.account.event.MoneyDeposited;
import com.example.axonsample.domain.account.event.MoneyWithdrawn;
import com.example.axonsample.domain.account.repository.AccountBalanceRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class AccountBalanceProjection {
    private final AccountBalanceRepository accountBalanceRepository;

    public AccountBalanceProjection(AccountBalanceRepository accountBalanceRepository) {
        this.accountBalanceRepository = accountBalanceRepository;
    }

    @EventHandler
    public void on(AccountCreated evt) {
        accountBalanceRepository.createAccountBalance(evt.getAccountId());
    }

    @EventHandler
    public void on(MoneyDeposited evt) {
        accountBalanceRepository.updateAccountBalance(evt.getAccountId(), evt.getAmount());
    }

    @EventHandler
    public void on(MoneyWithdrawn evt) {
        accountBalanceRepository.updateAccountBalance(evt.getAccountId(), evt.getAmount().negate());
    }
}
