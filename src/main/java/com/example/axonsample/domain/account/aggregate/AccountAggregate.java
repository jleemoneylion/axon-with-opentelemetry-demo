package com.example.axonsample.domain.account.aggregate;

import com.example.axonsample.domain.account.command.CreateAccount;
import com.example.axonsample.domain.account.command.DepositMoney;
import com.example.axonsample.domain.account.command.WithdrawMoney;
import com.example.axonsample.domain.account.event.AccountCreated;
import com.example.axonsample.domain.account.event.MoneyDeposited;
import com.example.axonsample.domain.account.event.MoneyWithdrawn;
import com.example.axonsample.domain.account.exception.InsufficientBalanceException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class AccountAggregate {
    @AggregateIdentifier
    String accountId;
    BigDecimal balance = BigDecimal.ZERO;

    public AccountAggregate() {
    }

    @CommandHandler
    public AccountAggregate(CreateAccount cmd) {
        apply(AccountCreated.builder()
                .accountId(cmd.getAccountId())
                .timestamp(Instant.now())
                .build());
    }

    @CommandHandler
    public void handle(DepositMoney cmd) {
        apply(MoneyDeposited.builder()
                .accountId(accountId)
                .transactionId(cmd.getTransactionId())
                .amount(cmd.getAmount())
                .timestamp(Instant.now())
                .build());
    }

    @CommandHandler
    public void handle(WithdrawMoney cmd)
            throws InsufficientBalanceException {
        if (balance.compareTo(cmd.getAmount()) < 0)
            throw new InsufficientBalanceException();

        apply(MoneyWithdrawn.builder()
                .accountId(accountId)
                .transactionId(cmd.getTransactionId())
                .amount(cmd.getAmount())
                .timestamp(Instant.now())
                .build());
    }

    @EventSourcingHandler
    public void on(AccountCreated evt) {
        this.accountId = evt.getAccountId();
    }

    @EventSourcingHandler
    public void on(MoneyDeposited evt) {
        this.balance = this.balance.add(evt.getAmount());
    }

    @EventSourcingHandler
    public void on(MoneyWithdrawn evt) {
        this.balance = this.balance.subtract(evt.getAmount());
    }
}
