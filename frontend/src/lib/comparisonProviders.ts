export type CurrencyCode =
  "USD" | "EUR" | "GBP" | "INR" | "CAD" | "AUD" | "SGD" | "JPY";

export interface Currency {
  code: CurrencyCode;
  name: string;
  flag: string;
  usdRate: number;
}

export type ProviderId = "slash-pay" | "paypal" | "skydo" | "wise";

export interface ProviderPricing {
  id: ProviderId;
  name: string;
  shortCode: string;
  highlighted?: boolean;
  markupPercent: number;
  feeModel: "fixed" | "paypal" | "skydo" | "percentage";
  fixedFeeInr?: number;
  feePercent?: number;
  description: string;
}

export const supportedCurrencies: Currency[] = [
  { code: "USD", name: "US dollar", flag: "🇺🇸", usdRate: 1 },
  { code: "EUR", name: "Euro", flag: "🇪🇺", usdRate: 1.08 },
  { code: "GBP", name: "British pound", flag: "🇬🇧", usdRate: 1.27 },
  { code: "INR", name: "Indian rupee", flag: "🇮🇳", usdRate: 0.0104651 },
  { code: "CAD", name: "Canadian dollar", flag: "🇨🇦", usdRate: 0.74 },
  { code: "AUD", name: "Australian dollar", flag: "🇦🇺", usdRate: 0.66 },
  { code: "SGD", name: "Singapore dollar", flag: "🇸🇬", usdRate: 0.74 },
  { code: "JPY", name: "Japanese yen", flag: "🇯🇵", usdRate: 0.0067 },
];

export const comparisonProviders: ProviderPricing[] = [
  {
    id: "slash-pay",
    name: "Slash Pay",
    shortCode: "SP",
    highlighted: true,
    markupPercent: 0,
    feeModel: "fixed",
    fixedFeeInr: 5,
    description: "Demo pricing: ₹5.00 transfer fee",
  },
  {
    id: "paypal",
    name: "PayPal",
    shortCode: "P",
    markupPercent: 0.035,
    feeModel: "paypal",
    feePercent: 0.044,
    description:
      "Demo assumptions: 4.40% commercial-receipt fee, ₹25.00 fixed fee and 3.5% FX markup.",
  },
  {
    id: "skydo",
    name: "Skydo",
    shortCode: "S",
    markupPercent: 0,
    feeModel: "skydo",
    description:
      "Demo assumptions: India receiving-payment tiers with 18% GST included.",
  },
  {
    id: "wise",
    name: "Wise",
    shortCode: "W",
    markupPercent: 0,
    feeModel: "percentage",
    feePercent: 0.0045,
    description:
      "Demo fee: 0.45%. Actual Wise fees vary by currency, payment method, amount and transfer type.",
  },
];
