import { useMemo, useState } from "react";
import { ChevronDown, Info } from "lucide-react";
import {
  calculateComparison,
  formatMoney,
  formatRate,
  parseAmount,
  type ComparisonResult,
} from "@/lib/comparisonCalculator";
import {
  comparisonProviders,
  supportedCurrencies,
  type Currency,
  type CurrencyCode,
  type ProviderPricing,
} from "@/lib/comparisonProviders";

const defaultSource: CurrencyCode = "INR";
const defaultDestination: CurrencyCode = "USD";

function currencyFor(code: CurrencyCode): Currency {
  const currency = supportedCurrencies.find((item) => item.code === code);
  if (!currency) throw new Error(`Unsupported currency: ${code}`);
  return currency;
}

function MoneyValue({
  value,
  currency,
}: {
  value: number;
  currency: CurrencyCode;
}) {
  return (
    <span className="font-bold tabular-nums">
      {formatMoney(value, currency)}
    </span>
  );
}

function ProviderColumn({
  provider,
  result,
  source,
  destination,
}: {
  provider: ProviderPricing;
  result: ComparisonResult;
  source: CurrencyCode;
  destination: CurrencyCode;
}) {
  return (
    <article
      className={`comparison-provider group min-w-[190px] px-4 py-7 text-center transition duration-300 hover:-translate-y-1 hover:shadow-[var(--shadow-elevated)] ${provider.highlighted ? "rounded-[22px] bg-primary text-brand-ink shadow-[var(--shadow-card)]" : "rounded-[18px] bg-card"}`}
      aria-label={`${provider.name} calculated comparison`}
    >
      <div className="grid h-10 w-10 place-items-center rounded-full bg-brand-ink text-xs font-black text-brand-lime mx-auto">
        {provider.shortCode}
      </div>
      <h3 className="mt-3 min-h-12 text-lg font-bold">{provider.name}</h3>
      <div className="comparison-value mt-4 min-h-14">
        <MoneyValue value={result.recipientGets} currency={destination} />
        <span className="block text-xs text-muted-foreground">
          {destination}
        </span>
      </div>
      <div className="comparison-value min-h-16 pt-3">
        <span className="font-semibold tabular-nums">
          {formatRate(result.effectiveRate)}
        </span>
        <span className="block text-xs text-muted-foreground">
          {destination}/{source}
        </span>
      </div>
      <div className="comparison-value min-h-20 pt-3">
        <MoneyValue value={result.markupAmount} currency={source} />
        <span className="block text-xs text-muted-foreground">
          {(result.markupPercent * 100).toFixed(1)}% markup
        </span>
      </div>
      <div className="comparison-value min-h-20 pt-3">
        <MoneyValue value={result.transferFee} currency={source} />
      </div>
      <div className="comparison-value min-h-16 pt-3">
        <MoneyValue value={result.totalTransferCost} currency={source} />
      </div>
      <p className="mt-5 border-t border-black/10 pt-4 text-left text-xs leading-5 text-charcoal">
        {provider.description}
      </p>
    </article>
  );
}

export function CostComparisonSection() {
  const [amount, setAmount] = useState("1000");
  const [sourceCode, setSourceCode] = useState<CurrencyCode>(defaultSource);
  const [destinationCode, setDestinationCode] =
    useState<CurrencyCode>(defaultDestination);
  const source = currencyFor(sourceCode);
  const destination = currencyFor(destinationCode);
  const parsedAmount = parseAmount(amount);

  const results = useMemo(
    () =>
      comparisonProviders.map((provider) =>
        calculateComparison(
          {
            amount: parsedAmount,
            source: sourceCode,
            destination: destinationCode,
          },
          source,
          destination,
          provider,
        ),
      ),
    [destination, destinationCode, parsedAmount, source, sourceCode],
  );

  return (
    <section
      id="comparison"
      className="bg-surface py-20 sm:py-28"
      aria-labelledby="comparison-heading"
    >
      <div className="page-shell">
        <div className="mx-auto max-w-3xl text-center" data-reveal>
          <p className="text-sm font-bold uppercase tracking-[0.16em] text-brand-ink">
            Cost comparison
          </p>
          <h2
            id="comparison-heading"
            className="display-shout mt-4 text-[45px] text-obsidian sm:text-[61px]"
          >
            See what you really pay.
          </h2>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-charcoal">
            Compare exchange rates, fees, and what your recipient actually gets
            before you send.
          </p>
          <p className="mt-4 text-sm text-pebble">
            Demo comparison based on a ₹1,000 INR transfer to USD. Amounts
            update as you change the inputs.
          </p>
        </div>

        <div
          className="mt-12 rounded-[22px] bg-card p-5 shadow-[var(--shadow-elevated)] sm:p-8"
          data-reveal="scale"
        >
          <div className="grid gap-5 md:grid-cols-3">
            <label className="text-sm font-bold">
              Send amount
              <input
                value={amount}
                onChange={(event) => setAmount(event.target.value)}
                inputMode="decimal"
                aria-label="Send amount"
                className="mt-2 h-14 w-full rounded-xl border border-input bg-card px-4 text-lg font-semibold outline-none transition focus:border-brand-ink focus:ring-2 focus:ring-brand-mist"
              />
            </label>
            <CurrencySelect
              label="From"
              value={sourceCode}
              onChange={setSourceCode}
            />
            <CurrencySelect
              label="To"
              value={destinationCode}
              onChange={setDestinationCode}
            />
          </div>
          <p className="mt-4 flex items-center gap-2 text-xs text-pebble">
            <Info size={15} aria-hidden="true" /> Demo pricing assumptions only.
            Provider fees and availability vary by route, payment method and
            product.
          </p>
        </div>

        <div className="mt-8 overflow-x-auto pb-3" data-reveal="scale">
          <div className="grid min-w-[980px] grid-cols-[180px_repeat(4,minmax(190px,1fr))] items-start gap-3">
            <div className="pt-[112px] text-sm font-semibold text-pebble">
              <RowLabel title="Recipient gets" detail="Total after fees" />
              <RowLabel title="Exchange rate" />
              <RowLabel
                title="Exchange rate markup"
                detail="Money value + rate"
              />
              <RowLabel title="Transfer fee" />
              <RowLabel title="Total transfer cost" />
            </div>
            {comparisonProviders.map((provider, index) => (
              <ProviderColumn
                key={provider.id}
                provider={provider}
                result={results[index]!}
                source={sourceCode}
                destination={destinationCode}
              />
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}

function RowLabel({ title, detail }: { title: string; detail?: string }) {
  return (
    <div className="min-h-16 py-3 first:min-h-14">
      <span className="block">{title}</span>
      {detail && (
        <span className="mt-1 block text-xs font-normal">{detail}</span>
      )}
    </div>
  );
}

function CurrencySelect({
  label,
  value,
  onChange,
}: {
  label: string;
  value: CurrencyCode;
  onChange: (value: CurrencyCode) => void;
}) {
  return (
    <label className="relative text-sm font-bold">
      {label}
      <select
        value={value}
        onChange={(event) => onChange(event.target.value as CurrencyCode)}
        aria-label={`${label} currency`}
        className="mt-2 h-14 w-full appearance-none rounded-xl border border-input bg-card px-4 pr-12 text-lg font-semibold outline-none transition focus:border-brand-ink focus:ring-2 focus:ring-brand-mist"
      >
        {supportedCurrencies.map((currency) => (
          <option key={currency.code} value={currency.code}>
            {currency.flag} {currency.code} {currency.name}
          </option>
        ))}
      </select>
      <ChevronDown
        className="pointer-events-none absolute bottom-5 right-4"
        size={18}
        aria-hidden="true"
      />
    </label>
  );
}
