package com.personalfinance.transactionservice.application.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SpendingSummaryDto(
        BigDecimal spentAmount,
        OffsetDateTime from,
        OffsetDateTime to
) {}

