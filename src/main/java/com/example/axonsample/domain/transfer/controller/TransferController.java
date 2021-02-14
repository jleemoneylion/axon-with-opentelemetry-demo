package com.example.axonsample.domain.transfer.controller;

import com.example.axonsample.domain.transfer.command.InitiateTransfer;
import com.example.axonsample.domain.transfer.exception.TransferException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/transfers")
public class TransferController {
    private final CommandGateway commandGateway;

    public TransferController(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @PostMapping("/{transferId}")
    public CompletableFuture<ResponseEntity<Object>> initiateTransfer(@PathVariable String transferId,
                                                                      @RequestParam String fromAccountId,
                                                                      @RequestParam String toAccountId,
                                                                      @RequestParam BigDecimal amount) {
        return commandGateway.send(InitiateTransfer.builder()
                .transferId(transferId)
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build())
                .thenApply(c -> ResponseEntity.accepted().build())
                .exceptionally(this::createErrorResponse);
    }

    private ResponseEntity<Object> createErrorResponse(Throwable e) {
        if (e instanceof TransferException)
            return ResponseEntity.badRequest().build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
