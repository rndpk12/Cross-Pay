package com.crosspay.validation;

import com.crosspay.fx.dto.FxQuoteRequest;
import com.crosspay.ledger.dto.DepositRequest;
import com.crosspay.transfer.dto.TransferRequest;
import com.crosspay.withdrawal.dto.WithdrawalRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void acceptsMoneyAtDatabasePrecisionBoundary() {
        assertTrue(validator.validate(new DepositRequest(
                new BigDecimal("999999999999999.9999"), "USD"
        )).isEmpty());
    }

    @Test
    void rejectsMoneyBeyondDatabaseIntegerPrecision() {
        assertFalse(validator.validate(new WithdrawalRequest(
                new BigDecimal("1000000000000000.0000"), "USD"
        )).isEmpty());
    }

    @Test
    void rejectsMoneyWithMoreThanFourDecimalPlaces() {
        assertFalse(validator.validate(new TransferRequest(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1.23456")
        )).isEmpty());
    }

    @Test
    void acceptsMoneyWithExactlyFourDecimalPlaces() {
        assertTrue(validator.validate(new TransferRequest(
                UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1.2345")
        )).isEmpty());
    }

    @Test
    void rejectsNullAndNonPositiveMoney() {
        assertFalse(validator.validate(new DepositRequest(null, "USD")).isEmpty());
        assertFalse(validator.validate(new DepositRequest(BigDecimal.ZERO, "USD")).isEmpty());
        assertFalse(validator.validate(new DepositRequest(new BigDecimal("-0.01"), "USD")).isEmpty());
    }

    @Test
    void rejectsMalformedCurrencyBeforeServiceValidation() {
        assertFalse(validator.validate(new DepositRequest(BigDecimal.ONE, "US1")).isEmpty());
        assertFalse(validator.validate(new WithdrawalRequest(BigDecimal.ONE, "US")).isEmpty());
        assertFalse(validator.validate(new FxQuoteRequest(BigDecimal.ONE, "   ", "USD")).isEmpty());
    }

    @Test
    void acceptsAlphabeticCurrencyForServiceNormalization() {
        assertTrue(validator.validate(new FxQuoteRequest(
                new BigDecimal("1.0000"), "usd", "EUR"
        )).isEmpty());
    }

    @Test
    void rejectsMissingTransferIdentifiers() {
        var violations = validator.validate(new TransferRequest(
                null, null, BigDecimal.ONE
        ));
        assertEquals(2, violations.size());
    }
}
