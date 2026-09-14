import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useRef, useState, type CSSProperties } from "react";
import {
  ArrowRight,
  BriefcaseBusiness,
  Building2,
  Check,
  ChevronDown,
  Clock3,
  Headphones,
  Instagram,
  Landmark,
  LockKeyhole,
  Menu,
  ReceiptText,
  Search,
  ShieldCheck,
  Users,
  X,
  Youtube,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import globeImage from "@/assets/globe-coins.jpg";
import lockImage from "@/assets/security-lock.jpg";
import phoneImage from "@/assets/phone-travel.jpg";
import paidImage from "@/assets/get-paid.jpg";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Slash Pay: The international account" },
      {
        name: "description",
        content:
          "Send, spend and receive money internationally with Slash Pay.",
      },
      { property: "og:title", content: "Slash Pay: The international account" },
      {
        property: "og:description",
        content:
          "Send, spend and receive money internationally with Slash Pay.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: WisePage,
});

const trustItems = [
  {
    icon: Users,
    title: "Trusted by millions moving billions",
    text: "We move €14 billion worldwide every month",
  },
  {
    icon: Landmark,
    title: "Built for clarity",
    text: "Clear exchange details and transfer costs before you send",
  },
  {
    icon: Headphones,
    title: "24/7 customer support",
    text: "Get help from thousands of specialists any time over email, phone and chat",
  },
];

const providers = [
  {
    name: "Slash Pay",
    mark: "SP",
    recipient: "Calculated",
    exchangeRate: "Slash Pay rate",
    markup: "Your actual pricing",
    fee: "Your actual fee",
    total: "Calculated",
    best: true,
  },
  {
    name: "PayPal",
    mark: "P",
    recipient: "Calculated",
    exchangeRate: "PayPal transaction rate",
    markup: "3% or 4%, depending on conversion type",
    fee: "4.40% + fixed fee for international commercial receipts",
    total: "Calculated",
  },
  {
    name: "Skydo",
    mark: "S",
    recipient: "Calculated",
    exchangeRate: "Mid-market",
    markup: "0%",
    fee: "$19 / $29 / 0.3% + GST depending on amount",
    total: "Calculated",
  },
  {
    name: "Wise",
    mark: "W",
    recipient: "Calculated",
    exchangeRate: "Mid-market",
    markup: "0%",
    fee: "From 0.33%, varies by currency",
    total: "Calculated",
  },
];

const currencies = ["🇪🇺", "🇬🇧", "🇺🇸", "🇮🇳", "🇲🇼", "🇩🇰", "🇷🇸", "🇨🇲"];
const ribbonFlags = [
  "eu",
  "gb",
  "us",
  "in",
  "mw",
  "dk",
  "rs",
  "cm",
  "ca",
  "au",
];

type TransferCurrency = {
  code: string;
  name: string;
  flag: string;
  usdRate: number;
};

const transferCurrencies: TransferCurrency[] = [
  { code: "USD", name: "United States dollar", flag: "🇺🇸", usdRate: 1 },
  { code: "EUR", name: "Euro", flag: "🇪🇺", usdRate: 1.08 },
  { code: "GBP", name: "British pound", flag: "🇬🇧", usdRate: 1.27 },
  { code: "INR", name: "Indian rupee", flag: "🇮🇳", usdRate: 0.0104651 },
  {
    code: "AED",
    name: "United Arab Emirates dirham",
    flag: "🇦🇪",
    usdRate: 0.2723,
  },
  { code: "ARS", name: "Argentine peso", flag: "🇦🇷", usdRate: 0.00094 },
  { code: "AUD", name: "Australian dollar", flag: "🇦🇺", usdRate: 0.66 },
  { code: "BDT", name: "Bangladeshi taka", flag: "🇧🇩", usdRate: 0.0082 },
  { code: "BRL", name: "Brazilian real", flag: "🇧🇷", usdRate: 0.2 },
  { code: "CAD", name: "Canadian dollar", flag: "🇨🇦", usdRate: 0.74 },
  { code: "CHF", name: "Swiss franc", flag: "🇨🇭", usdRate: 1.13 },
  { code: "CNY", name: "Chinese yuan", flag: "🇨🇳", usdRate: 0.138 },
  { code: "DKK", name: "Danish krone", flag: "🇩🇰", usdRate: 0.145 },
  { code: "HKD", name: "Hong Kong dollar", flag: "🇭🇰", usdRate: 0.128 },
  { code: "IDR", name: "Indonesian rupiah", flag: "🇮🇩", usdRate: 0.000061 },
  { code: "JPY", name: "Japanese yen", flag: "🇯🇵", usdRate: 0.0067 },
  { code: "KRW", name: "South Korean won", flag: "🇰🇷", usdRate: 0.00074 },
  { code: "MXN", name: "Mexican peso", flag: "🇲🇽", usdRate: 0.058 },
  { code: "MYR", name: "Malaysian ringgit", flag: "🇲🇾", usdRate: 0.225 },
  { code: "NGN", name: "Nigerian naira", flag: "🇳🇬", usdRate: 0.00064 },
  { code: "NOK", name: "Norwegian krone", flag: "🇳🇴", usdRate: 0.094 },
  { code: "NZD", name: "New Zealand dollar", flag: "🇳🇿", usdRate: 0.61 },
  { code: "PHP", name: "Philippine peso", flag: "🇵🇭", usdRate: 0.0173 },
  { code: "PLN", name: "Polish zloty", flag: "🇵🇱", usdRate: 0.25 },
  { code: "SEK", name: "Swedish krona", flag: "🇸🇪", usdRate: 0.097 },
  { code: "SGD", name: "Singapore dollar", flag: "🇸🇬", usdRate: 0.74 },
  { code: "THB", name: "Thai baht", flag: "🇹🇭", usdRate: 0.029 },
  { code: "TRY", name: "Turkish lira", flag: "🇹🇷", usdRate: 0.03 },
  { code: "ZAR", name: "South African rand", flag: "🇿🇦", usdRate: 0.055 },
];

function scrollTo(id: string) {
  document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
}

function WisePage() {
  const [menuOpen, setMenuOpen] = useState(false);
  const [amount, setAmount] = useState("1000");
  useEffect(() => {
    const elements = Array.from(
      document.querySelectorAll<HTMLElement>("[data-reveal]"),
    );
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
      elements.forEach((element) => element.classList.add("is-visible"));
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("is-visible");
            observer.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.14, rootMargin: "0px 0px -7% 0px" },
    );
    elements.forEach((element) => observer.observe(element));
    return () => observer.disconnect();
  }, []);

  return (
    <main className="bg-background text-brand-ink">
      <header className="sticky top-0 z-20 bg-background motion-header">
        <nav
          className="page-shell flex h-16 items-center justify-between"
          aria-label="Main navigation"
        >
          <div className="flex items-center gap-8">
            <a
              href="#top"
              className="text-[28px] font-black italic text-brand-ink"
              aria-label="Slash Pay home"
            >
              ➤Slash Pay
            </a>
          </div>
          <div className="hidden items-center gap-4 text-sm font-medium md:flex">
            <Button variant="ghost" size="sm">
              Log in
            </Button>
            <Button variant="outline" size="sm">
              Sign up
            </Button>
          </div>
          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="bg-brand-mist md:hidden"
            onClick={() => setMenuOpen((v) => !v)}
            aria-label="Toggle menu"
          >
            {menuOpen ? <X size={20} /> : <Menu size={20} />}
          </Button>
        </nav>
        {menuOpen && (
          <div className="page-shell grid gap-4 border-t border-border py-5 text-sm font-semibold md:hidden">
            <a href="#personal">Personal</a>
            <a href="#business">Business</a>
            <a href="#platform">Platform</a>
            <a href="#security">Help</a>
            <Button className="w-fit">Sign up</Button>
          </div>
        )}
      </header>

      <section
        id="top"
        className="overflow-hidden bg-background pt-12 text-center sm:pt-16"
      >
        <div className="page-shell">
          <h1 className="display-shout motion-hero-title mx-auto max-w-[1120px] text-[52px] text-obsidian sm:text-[89px] lg:text-[105px]">
            Money for here,
            <br />
            there and everywhere
          </h1>
          <p className="motion-hero-copy mx-auto mt-8 max-w-xl text-lg text-charcoal">
            160 countries and territories. 40 currencies. Get the account built
            to save you money round the world.
          </p>
          <div className="motion-hero-actions mt-7 flex flex-wrap items-center justify-center gap-7">
            <Button onClick={() => scrollTo("personal")}>
              Open an account
            </Button>
            <button
              type="button"
              onClick={() => scrollTo("send")}
              className="text-link"
            >
              Send money now
            </button>
          </div>
          <div className="mx-auto mt-12 h-[340px] max-w-[780px] overflow-hidden sm:h-[460px]">
            <img
              src={globeImage}
              width={1536}
              height={1024}
              alt="A turquoise globe surrounded by gold coins"
              className="motion-globe mx-auto w-full object-cover object-top"
            />
          </div>
        </div>
      </section>

      <section className="bg-background py-16">
        <div className="page-shell grid gap-9 md:grid-cols-3">
          {trustItems.map(({ icon: Icon, title, text }, index) => (
            <div
              key={title}
              data-reveal
              style={
                { "--reveal-delay": `${index * 90}ms` } as React.CSSProperties
              }
            >
              <Icon size={24} strokeWidth={1.8} />
              <h2 className="mt-6 text-lg font-bold text-obsidian">{title}</h2>
              <p className="mt-2 max-w-sm text-base text-pebble">{text}</p>
            </div>
          ))}
        </div>
      </section>

      <section id="send" className="bg-primary py-16 sm:py-20">
        <div className="page-shell grid items-center gap-12 lg:grid-cols-[1fr_440px]">
          <div data-reveal="left">
            <h2 className="text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]">
              Send money globally for less
            </h2>
            <p className="mt-6 max-w-md text-lg">
              <span className="text-link">Save up to 8x</span> on international
              transfers —<br />
              <span className="text-link">with fees as low as 0.1%.</span>
            </p>
            <Button
              variant="forest"
              className="mt-8"
              onClick={() => scrollTo("comparison")}
            >
              Learn how to send money
            </Button>
          </div>
          <div data-reveal="right">
            <TransferCard amount={amount} setAmount={setAmount} />
          </div>
        </div>

        <div id="comparison" className="page-shell pt-24 text-center">
          <h2
            data-reveal
            className="display-shout text-[45px] text-obsidian sm:text-[61px]"
          >
            Never pay a hidden fee again
          </h2>
          <p className="mx-auto mt-4 max-w-lg text-lg leading-6">
            Banks and other providers add markups to the exchange rate to make
            you pay more. Not us — see for yourself.
          </p>
          <div className="mt-7 flex flex-wrap justify-center gap-8">
            <Button variant="forest">Send money now</Button>
            <button type="button" className="text-link">
              Learn how to send money
            </button>
          </div>
          <div data-reveal="scale">
            <ComparisonTable amount={amount} setAmount={setAmount} />
          </div>
          <p className="mt-7 text-xs">
            This applies when you pay in via bank transfer or ACH payments.{" "}
            <span className="text-link">How do we collect this data?</span>
          </p>
        </div>
      </section>

      <section id="business" className="bg-background py-20">
        <div
          data-reveal="scale"
          className="page-shell rounded-[28px] bg-brand-ink px-6 py-16 text-center text-brand-lime sm:px-16"
        >
          <div className="mx-auto grid h-12 w-12 place-items-center rounded-full bg-spruce">
            <BriefcaseBusiness />
          </div>
          <h2 className="display-shout mt-7 text-[45px] sm:text-[61px]">
            Built for business too
          </h2>
          <p className="mx-auto mt-5 max-w-2xl text-base font-medium leading-6 text-paper">
            Go global with our international business account. Make payments and
            get paid in 40+ currencies. Join over 700,000 businesses thriving
            with Slash Pay.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-8">
            <Button>Try demo</Button>
            <Button variant="link" className="text-brand-lime">
              Learn more
            </Button>
          </div>
        </div>
      </section>

      <section id="security" className="bg-background py-20">
        <div className="page-shell">
          <div className="grid items-center gap-10 md:grid-cols-2">
            <div data-reveal="left">
              <h2 className="text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]">
                Disappoint thieves
              </h2>
              <p className="mt-6 max-w-md text-lg text-slate">
                Every month, millions of our personal and business customers
                trust us to move over €14 billion of their money.
              </p>
              <Button className="mt-8">How we keep your money safe</Button>
            </div>
            <img
              data-reveal="right"
              src={lockImage}
              loading="lazy"
              width={1024}
              height={1024}
              alt="Turquoise security padlock"
              className="motion-float mx-auto w-full max-w-[390px]"
            />
          </div>
          <div className="mt-16 grid gap-10 md:grid-cols-3">
            <SecurityItem
              icon={LockKeyhole}
              text="Our dedicated fraud and security teams work to keep your money safe"
            />
            <SecurityItem
              icon={ShieldCheck}
              text="We use 2-factor authentication to protect your account"
            />
            <SecurityItem
              icon={Building2}
              text="We hold your money with established financial institutions"
            />
          </div>
        </div>
      </section>

      <FlagRibbon />

      <section id="platform" className="bg-background py-20">
        <div className="page-shell space-y-28">
          <Story
            image={phoneImage}
            title="Move your money worldwide"
            text="Save money when you send, spend and get paid in different currencies. All you need, in one account, wherever you need it."
          />
          <Story
            image={paidImage}
            title="Get paid in different currencies, fast"
            text="Request and receive money with Slash Pay, then keep your balances organised in one clear account."
            reverse
          />
        </div>
      </section>

      <footer className="border-t border-border bg-surface py-16">
        <div className="page-shell">
          <div className="flex items-center justify-between">
            <span className="text-5xl font-black italic">➤Slash Pay</span>
            <div className="flex gap-5">
              <span className="font-black">f</span>
              <X />
              <Instagram />
              <Youtube />
              <span className="font-black">in</span>
            </div>
          </div>
          <div className="mt-16 grid gap-5 text-base md:grid-cols-3">
            <div className="space-y-5">
              <p>Legal</p>
              <p>Research privacy policy</p>
              <p>Modern slavery statement</p>
            </div>
            <div className="space-y-5">
              <p>Privacy policy</p>
              <p>Complaints</p>
              <p>Accessibility</p>
            </div>
            <div className="space-y-5">
              <p>Cookie policy</p>
              <p>Country site map</p>
              <p>Intellectual property</p>
            </div>
          </div>
          <div className="mt-16 space-y-6 text-sm leading-6 text-charcoal">
            <p>© 2026 Slash Pay</p>
            <p>
              Slash Pay is a product concept. Review all transfer details, fees
              and availability before confirming a transaction.
            </p>
          </div>
        </div>
      </footer>
    </main>
  );
}

function TransferCard({
  amount,
  setAmount,
}: {
  amount: string;
  setAmount: (value: string) => void;
}) {
  const [source, setSource] = useState<TransferCurrency>(
    transferCurrencies[3]!,
  );
  const [target, setTarget] = useState<TransferCurrency>(
    transferCurrencies[0]!,
  );
  const [pickerFor, setPickerFor] = useState<"source" | "target" | null>(null);
  const [recipientDraft, setRecipientDraft] = useState("");
  const [isEditingRecipient, setIsEditingRecipient] = useState(false);
  const numericAmount = Math.max(0, Number(amount.replace(/,/g, "")) || 0);
  const rate = source.usdRate / target.usdRate;
  const fee = Math.min(numericAmount * 0.014, numericAmount);
  const received = Math.max(0, (numericAmount - fee) * rate);
  const formattedAmount = new Intl.NumberFormat("en-US", {
    maximumFractionDigits: 2,
  }).format(numericAmount);
  const formattedReceived = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(received);
  const formattedRate = new Intl.NumberFormat("en-US", {
    minimumFractionDigits: 4,
    maximumFractionDigits: 6,
  }).format(rate);

  function selectCurrency(currency: TransferCurrency) {
    if (pickerFor === "source") setSource(currency);
    if (pickerFor === "target") setTarget(currency);
    setPickerFor(null);
  }

  function updateRecipient(value: string) {
    setRecipientDraft(value);
    const requestedAmount = Number(value.replace(/,/g, ""));
    if (!Number.isFinite(requestedAmount) || requestedAmount < 0) return;

    const sourceAmount = requestedAmount / (rate * (1 - 0.014));
    setAmount(sourceAmount.toFixed(2));
  }

  return (
    <div className="relative rounded-[10px] bg-card p-6 text-left text-card-foreground shadow-[var(--shadow-card)] sm:p-8">
      <div className="flex justify-end">
        <button
          type="button"
          className="inline-flex items-center gap-3 rounded-full bg-surface px-4 py-2 text-xs font-semibold transition hover:bg-brand-mist sm:text-sm"
          aria-label={`Exchange rate: 1 ${source.code} equals ${formattedRate} ${target.code}`}
        >
          <LockKeyhole size={18} aria-hidden="true" />
          <span className="border-l border-pebble/40 pl-3">
            1 {source.code} = {formattedRate} {target.code}
          </span>
          <span aria-hidden="true" className="text-lg leading-none">
            ›
          </span>
        </button>
      </div>
      <label className="mt-6 block text-sm text-pebble">You send exactly</label>
      <div className="mt-1 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3">
        <CurrencyChip
          currency={source}
          onClick={() => setPickerFor("source")}
        />
        <input
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          inputMode="decimal"
          aria-label="Amount to send"
          className="min-w-0 border-0 bg-transparent text-right text-4xl font-black outline-none transition-all duration-200 focus:text-5xl focus:text-brand-ink sm:text-5xl sm:focus:text-6xl"
        />
      </div>
      <p className="mt-3 rounded-[10px] bg-brand-mist px-3 py-2 text-xs sm:text-sm">
        Sending over 22,000 {source.code} or equivalent?{" "}
        <u>We’ll discount our fee</u>
      </p>
      <label className="mt-7 block text-sm text-pebble">Recipient gets</label>
      <div className="mt-1 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3">
        <CurrencyChip
          currency={target}
          onClick={() => setPickerFor("target")}
        />
        <input
          value={isEditingRecipient ? recipientDraft : formattedReceived}
          onFocus={() => {
            setRecipientDraft(formattedReceived);
            setIsEditingRecipient(true);
          }}
          onBlur={() => setIsEditingRecipient(false)}
          onChange={(event) => updateRecipient(event.target.value)}
          inputMode="decimal"
          aria-label="Amount recipient gets"
          className="min-w-0 border-0 bg-transparent text-right text-4xl font-black tabular-nums outline-none transition-all duration-200 focus:text-5xl focus:text-brand-ink sm:text-5xl sm:focus:text-6xl"
        />
      </div>
      <div className="mt-5 border-t border-border pt-5">
        <InfoRow icon={Clock3} label="Arrives" value="by Friday" />
        <InfoRow
          icon={ReceiptText}
          label="Estimated fees"
          value={`Included in ${source.code} amount`}
          end={`${new Intl.NumberFormat("en-US", { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(fee)} ${source.code} ›`}
        />
      </div>
      <p className="mt-5 text-xs text-pebble">
        Your amount and recipient total update as you edit.
      </p>
      <Button className="mt-5 w-full">Send money</Button>
      {pickerFor && (
        <CurrencyPicker
          selected={pickerFor === "source" ? source : target}
          onSelect={selectCurrency}
          onClose={() => setPickerFor(null)}
        />
      )}
    </div>
  );
}

function FlagRibbon() {
  const ribbonRef = useRef<HTMLDivElement>(null);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    let animationFrame = 0;
    const updateProgress = () => {
      animationFrame = 0;
      const element = ribbonRef.current;
      if (!element) return;
      const rect = element.getBoundingClientRect();
      const viewportHeight = window.innerHeight;
      const nextProgress = Math.max(
        0,
        Math.min(
          1,
          (viewportHeight - rect.top) / (viewportHeight + rect.height * 0.35),
        ),
      );
      setProgress((current) =>
        Math.abs(current - nextProgress) > 0.01 ? nextProgress : current,
      );
    };
    const onScroll = () => {
      if (!animationFrame)
        animationFrame = window.requestAnimationFrame(updateProgress);
    };
    updateProgress();
    window.addEventListener("scroll", onScroll, { passive: true });
    window.addEventListener("resize", onScroll);
    return () => {
      window.removeEventListener("scroll", onScroll);
      window.removeEventListener("resize", onScroll);
      if (animationFrame) window.cancelAnimationFrame(animationFrame);
    };
  }, []);

  const style = { "--ribbon-progress": progress } as CSSProperties &
    Record<string, number>;

  return (
    <div
      ref={ribbonRef}
      className="flag-ribbon"
      style={style}
      aria-label="Currencies available with Slash Pay"
    >
      <div className="flag-ribbon__runway" aria-hidden="true">
        <span className="flag-ribbon__arrow">
          <ArrowRight size={44} />
        </span>
      </div>
      <div className="flag-ribbon__flags" aria-hidden="true">
        {ribbonFlags.map((code, index) => (
          <span
            className="flag-ribbon__flag"
            key={code}
            style={
              { "--flag-index": index } as CSSProperties &
                Record<string, number>
            }
          >
            <img src={`https://flagcdn.com/w160/${code}.png`} alt="" />
          </span>
        ))}
      </div>
    </div>
  );
}

function CurrencyChip({
  currency,
  onClick,
}: {
  currency: TransferCurrency;
  onClick: () => void;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-haspopup="dialog"
      className="inline-flex items-center gap-1 rounded-full bg-surface px-3 py-2 text-lg font-bold transition hover:bg-brand-mist focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-ink"
    >
      <span>{currency.flag}</span>
      <span>{currency.code}</span>
      <ChevronDown size={18} aria-hidden="true" />
    </button>
  );
}

function CurrencyPicker({
  selected,
  onSelect,
  onClose,
}: {
  selected: TransferCurrency;
  onSelect: (currency: TransferCurrency) => void;
  onClose: () => void;
}) {
  const [query, setQuery] = useState("");
  const popular = ["EUR", "INR", "USD"];
  const matches = transferCurrencies.filter((currency) =>
    `${currency.code} ${currency.name}`
      .toLowerCase()
      .includes(query.toLowerCase()),
  );
  const popularMatches = matches.filter((currency) =>
    popular.includes(currency.code),
  );
  const allMatches = matches.filter(
    (currency) => !popular.includes(currency.code),
  );
  return (
    <div
      className="absolute inset-x-5 top-16 z-30 max-h-[min(460px,calc(100vh-7rem))] overflow-auto rounded-xl bg-card p-3 shadow-[var(--shadow-card)] sm:left-5 sm:right-auto sm:top-20 sm:w-[360px]"
      role="dialog"
      aria-label="Choose a currency"
    >
      <div className="flex items-center gap-2 rounded-lg border-2 border-brand-ink px-2.5 py-1.5">
        <Search size={18} aria-hidden="true" />
        <input
          autoFocus
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Type a currency / country"
          className="min-w-0 flex-1 bg-transparent text-base outline-none"
          aria-label="Search currencies"
        />
        <button
          type="button"
          onClick={onClose}
          aria-label="Close currency menu"
          className="text-xl leading-none"
        >
          ×
        </button>
      </div>
      {popularMatches.length > 0 && (
        <CurrencyGroup
          title="Popular currencies"
          currencies={popularMatches}
          selected={selected}
          onSelect={onSelect}
        />
      )}
      {allMatches.length > 0 && (
        <CurrencyGroup
          title="All currencies"
          currencies={allMatches}
          selected={selected}
          onSelect={onSelect}
        />
      )}
      {matches.length === 0 && (
        <p className="p-5 text-sm text-pebble">
          No currencies match that search.
        </p>
      )}
    </div>
  );
}

function CurrencyGroup({
  title,
  currencies: groupCurrencies,
  selected,
  onSelect,
}: {
  title: string;
  currencies: TransferCurrency[];
  selected: TransferCurrency;
  onSelect: (currency: TransferCurrency) => void;
}) {
  return (
    <div className="mt-3">
      <p className="border-b border-border pb-2 text-base font-medium text-charcoal">
        {title}
      </p>
      <div>
        {groupCurrencies.map((currency) => (
          <button
            key={currency.code}
            type="button"
            onClick={() => onSelect(currency)}
            className="flex w-full items-center gap-2 px-2 py-2 text-left text-sm transition hover:bg-brand-mist"
          >
            <span className="text-lg">{currency.flag}</span>
            <span className="font-semibold">{currency.code}</span>
            <span className="text-charcoal">{currency.name}</span>
            {selected.code === currency.code && (
              <span className="ml-auto font-bold text-brand-ink">✓</span>
            )}
          </button>
        ))}
      </div>
    </div>
  );
}

function InfoRow({
  icon: Icon,
  label,
  value,
  end,
}: {
  icon: typeof Clock3;
  label: string;
  value: string;
  end?: string;
}) {
  return (
    <div className="grid grid-cols-[24px_minmax(0,1fr)_auto] items-center gap-3 py-3">
      <Icon size={20} />
      <div>
        <span className="block text-xs text-muted-foreground">{label}</span>
        <strong className="text-sm">{value}</strong>
      </div>
      {end && <span className="text-xs underline">{end}</span>}
    </div>
  );
}

function ComparisonTable({
  amount,
  setAmount,
}: {
  amount: string;
  setAmount: (value: string) => void;
}) {
  return (
    <div className="mt-10 overflow-x-auto rounded-[10px] bg-card p-5 text-left text-card-foreground shadow-[var(--shadow-elevated)] sm:p-8">
      <div className="grid min-w-[850px] grid-cols-3 gap-5">
        <label className="text-xs font-bold">
          Amount
          <input
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            className="mt-2 h-12 w-full rounded-[10px] border border-input px-4 text-base font-normal outline-none focus:border-brand-ink"
          />
        </label>
        <label className="text-xs font-bold">
          From
          <div className="mt-2 flex h-12 items-center justify-between rounded-[10px] border border-input px-4 text-base font-normal">
            🇪🇺 EUR Euro <ChevronDown size={16} />
          </div>
        </label>
        <label className="text-xs font-bold">
          To
          <div className="mt-2 flex h-12 items-center justify-between rounded-[10px] border border-input px-4 text-base font-normal">
            🇺🇸 USD United States dollar <ChevronDown size={16} />
          </div>
        </label>
      </div>
      <div className="mt-7 grid min-w-[960px] grid-cols-[165px_repeat(4,1fr)]">
        <div className="space-y-7 pt-[87px] text-sm text-pebble">
          <p>
            Recipient gets
            <br />
            <span className="text-xs">(Total after fees)</span>
          </p>
          <p>Exchange rate</p>
          <p>Exchange rate markup</p>
          <p>Transfer fee</p>
          <p>Total transfer cost</p>
        </div>
        {providers.map((provider) => (
          <div
            key={provider.name}
            className={`space-y-7 px-4 py-5 text-center text-sm ${provider.best ? "rounded-[10px] bg-primary" : ""}`}
          >
            <div className="h-10 font-semibold">
              <span className="mx-auto mb-2 grid h-8 w-8 place-items-center rounded-full bg-brand-ink text-xs text-brand-lime">
                {provider.mark}
              </span>
              {provider.name}
            </div>
            <strong
              className={provider.best ? "text-brand-ink" : "text-brand-red"}
            >
              {provider.recipient}
            </strong>
            <p>{provider.exchangeRate}</p>
            <p>{provider.markup}</p>
            <p>{provider.fee}</p>
            <p>{provider.total}</p>
          </div>
        ))}
      </div>
      <Button
        variant="link"
        className="mt-6 w-full border-t border-border pt-5 text-sm"
      >
        Show more providers
      </Button>
    </div>
  );
}

function ProductCard({
  art,
  title,
  text,
  detail,
  action,
}: {
  art: React.ReactNode;
  title: string;
  text: string;
  detail: string;
  action: string;
}) {
  return (
    <article className="flex min-h-[430px] flex-col items-center rounded-[10px] bg-card p-6 text-card-foreground">
      <div className="grid h-28 place-items-center">{art}</div>
      <h3 className="mt-6 text-2xl font-bold text-obsidian">{title}</h3>
      <p className="mt-4 max-w-xs text-slate">{text}</p>
      <p className="mt-5 font-semibold underline">
        <Check
          className="mr-2 inline rounded-full bg-brand-ink p-0.5 text-brand-lime"
          size={18}
        />
        {detail}
      </p>
      <Button variant="outline" className="mt-auto">
        {action}
      </Button>
    </article>
  );
}
function SecurityItem({
  icon: Icon,
  text,
}: {
  icon: typeof LockKeyhole;
  text: string;
}) {
  return (
    <div>
      <span className="grid h-14 w-14 place-items-center rounded-full bg-surface">
        <Icon size={24} />
      </span>
      <p className="mt-5 max-w-sm font-semibold leading-6">{text}</p>
    </div>
  );
}
function Story({
  image,
  title,
  text,
  reverse = false,
}: {
  image: string;
  title: string;
  text: string;
  reverse?: boolean;
}) {
  return (
    <article className="grid items-center gap-14 md:grid-cols-2">
      <img
        src={image}
        loading="lazy"
        width={1024}
        height={1280}
        alt="International money account in use"
        className={`aspect-[4/5] w-full rounded-[28px] object-cover ${reverse ? "md:order-2" : ""}`}
      />
      <div className={reverse ? "md:order-1" : ""}>
        <h2 className="text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]">
          {title}
        </h2>
        <p className="mt-6 max-w-lg text-lg text-slate">{text}</p>
        <div className="mt-8 flex flex-wrap items-center gap-8">
          <Button>Get Started</Button>
          <Button variant="link" className="text-link px-0">
            Explore the Slash Pay Account
          </Button>
        </div>
      </div>
    </article>
  );
}
