package com.crosspay.transfer.controller;

import com.crosspay.transaction.entity.Transaction;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.transfer.dto.TransferResponse;
import com.crosspay.transfer.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(
            Authentication authentication,
            @Valid @RequestBody TransferRequest request
    ) {

        UUID senderUserId = (UUID) authentication.getPrincipal();

        Transaction transaction = transferService.transfer(
                senderUserId,
                request
        );

        TransferResponse response = new TransferResponse(
                transaction.getId(),
                request.recipientUserId(),
                transaction.getCurrency(),
                request.amount(),
                transaction.getStatus(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );

        return ResponseEntity.ok(response);
    }
}