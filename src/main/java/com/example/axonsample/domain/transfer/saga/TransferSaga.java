package com.example.axonsample.domain.transfer.saga;

import com.example.axonsample.domain.account.command.DepositMoney;
import com.example.axonsample.domain.account.command.WithdrawMoney;
import com.example.axonsample.domain.account.event.MoneyDeposited;
import com.example.axonsample.domain.account.event.MoneyWithdrawn;
import com.example.axonsample.domain.transfer.command.CompleteTransfer;
import com.example.axonsample.domain.transfer.constant.TransferStatus;
import com.example.axonsample.domain.transfer.event.TransferCompleted;
import com.example.axonsample.domain.transfer.event.TransferInitiated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Saga
@Data // required for saga state persistence
@Jacksonized
public class TransferSaga {
    @JsonIgnore
    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @JsonIgnore
    private transient CommandGateway commandGateway;

    private String transferId;
    private String fromAccountId;
    private String toAccountId;
    private BigDecimal amount;
    private boolean withdrawn = false;
    private boolean deposited = false;

    public TransferSaga() {
    }

    @SagaEventHandler(associationProperty = "transferId")
    @StartSaga
    public void on(TransferInitiated evt) {
        transferId = evt.getTransferId();
        fromAccountId = evt.getFromAccountId();
        toAccountId = evt.getToAccountId();
        amount = evt.getAmount();

        SagaLifecycle.associateWith("transactionId", transferId);
        commandGateway.send(WithdrawMoney.builder()
                .accountId(fromAccountId)
                .transactionId(transferId)
                .amount(amount)
                .build())
                .exceptionally(e -> commandGateway.send(CompleteTransfer.builder()
                        .transferId(transferId)
                        .status(TransferStatus.FAILURE)
                        .build()));
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void on(MoneyWithdrawn evt) {
        withdrawn = true;
        commandGateway.send(DepositMoney.builder()
                .accountId(toAccountId)
                .transactionId(transferId)
                .amount(amount)
                .build())
                .exceptionally(e -> {
                    return commandGateway.send(DepositMoney.builder()
                            .accountId(fromAccountId)
                            .transactionId(transferId)
                            .amount(amount)
                            .build());
                });
    }

    @SagaEventHandler(associationProperty = "transactionId")
    public void on(MoneyDeposited evt) {
        if (evt.getAccountId().equalsIgnoreCase(toAccountId)) {
            deposited = true;
            commandGateway.send(CompleteTransfer.builder()
                    .transferId(transferId)
                    .status(TransferStatus.SUCCESS)
                    .build());
        } else if (evt.getAccountId().equalsIgnoreCase(fromAccountId)) {
            withdrawn = false;
            commandGateway.send(CompleteTransfer.builder()
                    .transferId(transferId)
                    .status(TransferStatus.FAILURE)
                    .build());
        }
    }

    @SagaEventHandler(associationProperty = "transferId")
    public void on(TransferCompleted evt) {
        logger.info("transferId = {}, " +
                        "status = {}, " +
                        "withdrawn = {}, " +
                        "deposited = {}",
                evt.getTransferId(),
                evt.getStatus(),
                withdrawn,
                deposited);
        SagaLifecycle.end();
    }
}
