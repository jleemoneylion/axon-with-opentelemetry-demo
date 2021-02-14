package com.example.axonsample.domain.account.controller;

import com.example.axonsample.domain.account.command.CreateAccount;
import com.example.axonsample.domain.account.command.DepositMoney;
import com.example.axonsample.domain.account.command.WithdrawMoney;
import com.example.axonsample.domain.account.exception.AccountException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final CommandGateway commandGateway;

    public AccountController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/{accountId}")
    public CompletableFuture<ResponseEntity<Object>> createAccount(@PathVariable String accountId) {
        return commandGateway.send(CreateAccount.builder()
                .accountId(accountId)
                .build())
                .thenApply(c -> ResponseEntity.ok().build())
                .exceptionally(this::createErrorResponse);
    }

    @PostMapping("/{accountId}/deposit")
    public CompletableFuture<ResponseEntity<Object>> deposit(@PathVariable String accountId,
                                                             @RequestParam String transactionId,
                                                             @RequestParam BigDecimal amount) {
        return commandGateway.send(DepositMoney.builder()
                .accountId(accountId)
                .transactionId(transactionId)
                .amount(amount)
                .build())
                .thenApply(c -> ResponseEntity.ok().build())
                .exceptionally(this::createErrorResponse);
    }

    @PostMapping("/{accountId}/withdraw")
    public CompletableFuture<ResponseEntity<Object>> withdraw(@PathVariable String accountId,
                                                              @RequestParam String transactionId,
                                                              @RequestParam BigDecimal amount) {
        return commandGateway.send(WithdrawMoney.builder()
                .accountId(accountId)
                .transactionId(transactionId)
                .amount(amount)
                .build())
                .thenApply(c -> ResponseEntity.ok().build())
                .exceptionally(this::createErrorResponse);
    }

    private ResponseEntity<Object> createErrorResponse(Throwable e) {
        if (e instanceof AccountException)
            return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
