package com.crosspay.common.observability;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/** Operational counters only; the ledger remains the source of financial truth. */
@Component
public class FinancialOperationMetrics {

    private final MeterRegistry meterRegistry;

    public FinancialOperationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void depositSuccess() { increment("crosspay.deposit.success"); }
    public void depositFailure() { increment("crosspay.deposit.failure"); }
    public void withdrawalSuccess() { increment("crosspay.withdrawal.success"); }
    public void withdrawalFailure() { increment("crosspay.withdrawal.failure"); }
    public void transferSuccess() { increment("crosspay.transfer.success"); }
    public void transferFailure() { increment("crosspay.transfer.failure"); }
    public void quoteCreated() { increment("crosspay.fx.quote.created"); }
    public void quoteUsed() { increment("crosspay.fx.quote.used"); }
    public void quoteExpired() { increment("crosspay.fx.quote.expired"); }

    private void increment(String name) {
        meterRegistry.counter(name).increment();
    }
}
