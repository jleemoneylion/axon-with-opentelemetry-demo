package com.example.axonsample.domain.transfer.aggregate;

import com.example.axonsample.domain.transfer.command.CompleteTransfer;
import com.example.axonsample.domain.transfer.command.InitiateTransfer;
import com.example.axonsample.domain.transfer.event.TransferCompleted;
import com.example.axonsample.domain.transfer.event.TransferInitiated;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate
public class TransferAggregate {
    @AggregateIdentifier
    String transferId;

    public TransferAggregate() {
    }

    @CommandHandler
    public TransferAggregate(InitiateTransfer cmd) {
        apply(TransferInitiated.builder()
                .transferId(cmd.getTransferId())
                .fromAccountId(cmd.getFromAccountId())
                .toAccountId(cmd.getToAccountId())
                .amount(cmd.getAmount())
                .timestamp(Instant.now())
                .build());
    }

    @CommandHandler
    public void handle(CompleteTransfer cmd) {
        apply(TransferCompleted.builder()
                .transferId(cmd.getTransferId())
                .status(cmd.getStatus())
                .timestamp(Instant.now())
                .build());
    }

    @EventSourcingHandler
    public void on(TransferInitiated evt) {
        this.transferId = evt.getTransferId();
    }
}
