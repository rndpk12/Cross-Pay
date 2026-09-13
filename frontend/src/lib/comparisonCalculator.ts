import type {
  Currency,
  CurrencyCode,
  ProviderPricing,
} from "@/lib/comparisonProviders";

export interface ComparisonInput {
  amount: number;
  source: CurrencyCode;
  destination: CurrencyCode;
}

export interface ComparisonResult {
  providerId: string;
  recipientGets: number;
  baseRate: number;
  effectiveRate: number;
  markupAmount: number;
  markupPercent: number;
  transferFee: number;
  totalTransferCost: number;
}

const GST_RATE = 0.18;
const PAYPAL_FIXED_FEE_INR = 25;

export function roundMoney(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

export function roundRate(value: number): number {
  return Math.round((value + Number.EPSILON) * 1_000_000) / 1_000_000;
}

export function parseAmount(value: string): number {
  const parsed = Number(value.replace(/,/g, ""));
  return Number.isFinite(parsed) && parsed > 0 ? parsed : 0;
}

export function getBaseRate(source: Currency, destination: Currency): number {
  return roundRate(source.usdRate / destination.usdRate);
}

function getTransferFee(
  provider: ProviderPricing,
  amount: number,
  source: Currency,
): number {
  switch (provider.feeModel) {
    case "fixed":
      return provider.fixedFeeInr
        ? provider.fixedFeeInr / source.usdRate / 0.0104651
        : 0;
    case "paypal":
      return (
        amount * (provider.feePercent ?? 0) +
        PAYPAL_FIXED_FEE_INR / source.usdRate / 0.0104651
      );
    case "skydo": {
      const amountInUsd = amount * source.usdRate;
      const feeInUsd =
        amountInUsd <= 2_000
          ? 19
          : amountInUsd <= 10_000
            ? 29
            : amountInUsd * 0.003;
      return (feeInUsd / source.usdRate) * (1 + GST_RATE);
    }
    case "percentage":
      return amount * (provider.feePercent ?? 0);
  }
}

export function calculateComparison(
  input: ComparisonInput,
  source: Currency,
  destination: Currency,
  provider: ProviderPricing,
): ComparisonResult {
  const baseAmount = Math.max(0, input.amount);
  const baseRate = getBaseRate(source, destination);
  const markupAmount = roundMoney(baseAmount * provider.markupPercent);
  const effectiveRate = roundRate(baseRate * (1 - provider.markupPercent));
  const transferFee = roundMoney(getTransferFee(provider, baseAmount, source));
  const convertedAmount = baseAmount * effectiveRate;
  const feeInTargetCurrency = transferFee * baseRate;
  const recipientGets = roundMoney(
    Math.max(0, convertedAmount - feeInTargetCurrency),
  );

  return {
    providerId: provider.id,
    recipientGets,
    baseRate,
    effectiveRate,
    markupAmount,
    markupPercent: provider.markupPercent,
    transferFee,
    totalTransferCost: roundMoney(markupAmount + transferFee),
  };
}

export function formatMoney(value: number, currency: CurrencyCode): string {
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(roundMoney(value));
}

export function formatRate(value: number): string {
  return new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 4,
    maximumFractionDigits: 6,
  }).format(roundRate(value));
}
