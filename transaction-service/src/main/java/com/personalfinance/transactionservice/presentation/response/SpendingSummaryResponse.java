package com.personalfinance.transactionservice.presentation.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SpendingSummaryResponse {

    private BigDecimal spentAmount;
    private OffsetDateTime from;
    private OffsetDateTime to;

    public BigDecimal getSpentAmount() { return spentAmount; }
    public void setSpentAmount(BigDecimal spentAmount) { this.spentAmount = spentAmount; }

    public OffsetDateTime getFrom() { return from; }
    public void setFrom(OffsetDateTime from) { this.from = from; }

    public OffsetDateTime getTo() { return to; }
    public void setTo(OffsetDateTime to) { this.to = to; }
}

