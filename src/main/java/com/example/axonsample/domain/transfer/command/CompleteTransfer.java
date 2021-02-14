package com.example.axonsample.domain.transfer.command;

import com.example.axonsample.domain.transfer.constant.TransferStatus;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
@Builder
@Jacksonized
public class CompleteTransfer {
    @TargetAggregateIdentifier
    String transferId;
    TransferStatus status;
}
