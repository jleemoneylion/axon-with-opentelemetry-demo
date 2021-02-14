package com.example.axonsample.domain.transfer.event;

import com.example.axonsample.domain.transfer.constant.TransferStatus;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

@Value
@Builder
@Jacksonized
public class TransferCompleted {
    String transferId;
    TransferStatus status;
    Instant timestamp;
}
