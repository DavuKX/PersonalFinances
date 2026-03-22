package com.personalfinance.transactionservice.presentation.controller;

import com.personalfinance.transactionservice.application.dto.CreateTransactionCommand;
import com.personalfinance.transactionservice.application.dto.TransactionDto;
import com.personalfinance.transactionservice.application.dto.TransactionFilterCommand;
import com.personalfinance.transactionservice.application.dto.TransactionPageDto;
import com.personalfinance.transactionservice.application.dto.UpdateTransactionCommand;
import com.personalfinance.transactionservice.application.usecase.TransactionUseCase;
import com.personalfinance.transactionservice.domain.model.TransactionType;
import com.personalfinance.transactionservice.presentation.request.CreateTransactionRequest;
import com.personalfinance.transactionservice.presentation.request.UpdateTransactionRequest;
import com.personalfinance.transactionservice.presentation.response.TransactionPageResponse;
import com.personalfinance.transactionservice.presentation.response.TransactionResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TransactionController {

    private final TransactionUseCase transactionUseCase;

    public TransactionController(TransactionUseCase transactionUseCase) {
        this.transactionUseCase = transactionUseCase;
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody @Valid CreateTransactionRequest request) {
        TransactionDto dto = transactionUseCase.create(userId, new CreateTransactionCommand(
                request.getWalletId(), request.getType(), request.getAmount(), request.getCurrency(),
                request.getCategory(), request.getSubCategory(), request.getDescription(), request.getTransactionDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(dto));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(transactionUseCase.getById(userId, id)));
    }

    @GetMapping("/wallets/{walletId}/transactions")
    public ResponseEntity<TransactionPageResponse> getTransactionsByWallet(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        TransactionPageDto pageDto = transactionUseCase.listByWallet(userId, walletId, PageRequest.of(page, size, sort));
        return ResponseEntity.ok(toPageResponse(pageDto));
    }

    @GetMapping("/transactions")
    public ResponseEntity<TransactionPageResponse> getTransactions(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        TransactionPageDto pageDto = transactionUseCase.listByUser(new TransactionFilterCommand(
                userId, null, type, category, from, to, PageRequest.of(page, size, sort)));
        return ResponseEntity.ok(toPageResponse(pageDto));
    }

    @PutMapping("/transactions/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @RequestBody @Valid UpdateTransactionRequest request) {
        TransactionDto dto = transactionUseCase.update(userId, id, new UpdateTransactionCommand(
                request.getType(), request.getAmount(), request.getCategory(),
                request.getSubCategory(), request.getDescription(), request.getTransactionDate()));
        return ResponseEntity.ok(toResponse(dto));
    }

    @DeleteMapping("/transactions/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id) {
        transactionUseCase.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/transactions/health")
    public String health() {
        return "OK";
    }

    private TransactionResponse toResponse(TransactionDto dto) {
        TransactionResponse response = new TransactionResponse();
        response.setId(dto.id());
        response.setUserId(dto.userId());
        response.setWalletId(dto.walletId());
        response.setType(dto.type());
        response.setAmount(dto.amount());
        response.setCurrency(dto.currency());
        response.setCategory(dto.category());
        response.setSubCategory(dto.subCategory());
        response.setDescription(dto.description());
        response.setTransactionDate(dto.transactionDate());
        response.setCreatedAt(dto.createdAt());
        response.setUpdatedAt(dto.updatedAt());
        return response;
    }

    private TransactionPageResponse toPageResponse(TransactionPageDto pageDto) {
        List<TransactionResponse> content = pageDto.content().stream().map(this::toResponse).toList();
        return new TransactionPageResponse(content, pageDto.page(), pageDto.size(), pageDto.totalElements(), pageDto.totalPages());
    }
}
