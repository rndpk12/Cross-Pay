import { n as __toESM } from "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { n as require_jsx_runtime } from "../_libs/react+tanstack__react-query.mjs";
import { t as Button } from "./button-CCKletB4.mjs";
import { a as Search, c as LockKeyhole, d as Headphones, f as Clock3, g as ArrowRight, h as BriefcaseBusiness, i as ShieldCheck, l as Landmark, m as Building2, n as X, o as ReceiptText, p as ChevronDown, r as Users, s as Menu, t as Youtube, u as Instagram } from "../_libs/lucide-react.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/routes-B6C8FvEG.js
var import_react = /* @__PURE__ */ __toESM(require_react());
var import_jsx_runtime = require_jsx_runtime();
var globe_coins_default = "/assets/globe-coins-BAPHEfkI.jpg";
var security_lock_default = "/assets/security-lock-BC3xdrKs.jpg";
var phone_travel_default = "/assets/phone-travel-Y3B-rRqk.jpg";
var get_paid_default = "/assets/get-paid-C2AcRzBW.jpg";
var trustItems = [
	{
		icon: Users,
		title: "Trusted by millions moving billions",
		text: "We move €14 billion worldwide every month"
	},
	{
		icon: Landmark,
		title: "Built for clarity",
		text: "Clear exchange details and transfer costs before you send"
	},
	{
		icon: Headphones,
		title: "24/7 customer support",
		text: "Get help from thousands of specialists any time over email, phone and chat"
	}
];
var providers = [
	{
		name: "Slash Pay",
		mark: "SP",
		recipient: "Calculated",
		exchangeRate: "Slash Pay rate",
		markup: "Your actual pricing",
		fee: "Your actual fee",
		total: "Calculated",
		best: true
	},
	{
		name: "PayPal",
		mark: "P",
		recipient: "Calculated",
		exchangeRate: "PayPal transaction rate",
		markup: "3% or 4%, depending on conversion type",
		fee: "4.40% + fixed fee for international commercial receipts",
		total: "Calculated"
	},
	{
		name: "Skydo",
		mark: "S",
		recipient: "Calculated",
		exchangeRate: "Mid-market",
		markup: "0%",
		fee: "$19 / $29 / 0.3% + GST depending on amount",
		total: "Calculated"
	},
	{
		name: "Wise",
		mark: "W",
		recipient: "Calculated",
		exchangeRate: "Mid-market",
		markup: "0%",
		fee: "From 0.33%, varies by currency",
		total: "Calculated"
	}
];
var ribbonFlags = [
	"eu",
	"gb",
	"us",
	"in",
	"mw",
	"dk",
	"rs",
	"cm",
	"ca",
	"au"
];
var transferCurrencies = [
	{
		code: "USD",
		name: "United States dollar",
		flag: "🇺🇸",
		usdRate: 1
	},
	{
		code: "EUR",
		name: "Euro",
		flag: "🇪🇺",
		usdRate: 1.08
	},
	{
		code: "GBP",
		name: "British pound",
		flag: "🇬🇧",
		usdRate: 1.27
	},
	{
		code: "INR",
		name: "Indian rupee",
		flag: "🇮🇳",
		usdRate: .0104651
	},
	{
		code: "AED",
		name: "United Arab Emirates dirham",
		flag: "🇦🇪",
		usdRate: .2723
	},
	{
		code: "ARS",
		name: "Argentine peso",
		flag: "🇦🇷",
		usdRate: 94e-5
	},
	{
		code: "AUD",
		name: "Australian dollar",
		flag: "🇦🇺",
		usdRate: .66
	},
	{
		code: "BDT",
		name: "Bangladeshi taka",
		flag: "🇧🇩",
		usdRate: .0082
	},
	{
		code: "BRL",
		name: "Brazilian real",
		flag: "🇧🇷",
		usdRate: .2
	},
	{
		code: "CAD",
		name: "Canadian dollar",
		flag: "🇨🇦",
		usdRate: .74
	},
	{
		code: "CHF",
		name: "Swiss franc",
		flag: "🇨🇭",
		usdRate: 1.13
	},
	{
		code: "CNY",
		name: "Chinese yuan",
		flag: "🇨🇳",
		usdRate: .138
	},
	{
		code: "DKK",
		name: "Danish krone",
		flag: "🇩🇰",
		usdRate: .145
	},
	{
		code: "HKD",
		name: "Hong Kong dollar",
		flag: "🇭🇰",
		usdRate: .128
	},
	{
		code: "IDR",
		name: "Indonesian rupiah",
		flag: "🇮🇩",
		usdRate: 61e-6
	},
	{
		code: "JPY",
		name: "Japanese yen",
		flag: "🇯🇵",
		usdRate: .0067
	},
	{
		code: "KRW",
		name: "South Korean won",
		flag: "🇰🇷",
		usdRate: 74e-5
	},
	{
		code: "MXN",
		name: "Mexican peso",
		flag: "🇲🇽",
		usdRate: .058
	},
	{
		code: "MYR",
		name: "Malaysian ringgit",
		flag: "🇲🇾",
		usdRate: .225
	},
	{
		code: "NGN",
		name: "Nigerian naira",
		flag: "🇳🇬",
		usdRate: 64e-5
	},
	{
		code: "NOK",
		name: "Norwegian krone",
		flag: "🇳🇴",
		usdRate: .094
	},
	{
		code: "NZD",
		name: "New Zealand dollar",
		flag: "🇳🇿",
		usdRate: .61
	},
	{
		code: "PHP",
		name: "Philippine peso",
		flag: "🇵🇭",
		usdRate: .0173
	},
	{
		code: "PLN",
		name: "Polish zloty",
		flag: "🇵🇱",
		usdRate: .25
	},
	{
		code: "SEK",
		name: "Swedish krona",
		flag: "🇸🇪",
		usdRate: .097
	},
	{
		code: "SGD",
		name: "Singapore dollar",
		flag: "🇸🇬",
		usdRate: .74
	},
	{
		code: "THB",
		name: "Thai baht",
		flag: "🇹🇭",
		usdRate: .029
	},
	{
		code: "TRY",
		name: "Turkish lira",
		flag: "🇹🇷",
		usdRate: .03
	},
	{
		code: "ZAR",
		name: "South African rand",
		flag: "🇿🇦",
		usdRate: .055
	}
];
function scrollTo(id) {
	document.getElementById(id)?.scrollIntoView({ behavior: "smooth" });
}
function WisePage() {
	const [menuOpen, setMenuOpen] = (0, import_react.useState)(false);
	const [amount, setAmount] = (0, import_react.useState)("1000");
	(0, import_react.useEffect)(() => {
		const elements = Array.from(document.querySelectorAll("[data-reveal]"));
		if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
			elements.forEach((element) => element.classList.add("is-visible"));
			return;
		}
		const observer = new IntersectionObserver((entries) => {
			entries.forEach((entry) => {
				if (entry.isIntersecting) {
					entry.target.classList.add("is-visible");
					observer.unobserve(entry.target);
				}
			});
		}, {
			threshold: .14,
			rootMargin: "0px 0px -7% 0px"
		});
		elements.forEach((element) => observer.observe(element));
		return () => observer.disconnect();
	}, []);
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("main", {
		className: "bg-background text-brand-ink",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("header", {
				className: "sticky top-0 z-20 bg-background motion-header",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("nav", {
					className: "page-shell flex h-16 items-center justify-between",
					"aria-label": "Main navigation",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
							className: "flex items-center gap-8",
							children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)("a", {
								href: "#top",
								className: "text-[28px] font-black italic text-brand-ink",
								"aria-label": "Slash Pay home",
								children: "➤Slash Pay"
							})
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "hidden items-center gap-4 text-sm font-medium md:flex",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "ghost",
								size: "sm",
								children: "Log in"
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "outline",
								size: "sm",
								children: "Sign up"
							})]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
							type: "button",
							variant: "ghost",
							size: "icon",
							className: "bg-brand-mist md:hidden",
							onClick: () => setMenuOpen((v) => !v),
							"aria-label": "Toggle menu",
							children: menuOpen ? /* @__PURE__ */ (0, import_jsx_runtime.jsx)(X, { size: 20 }) : /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Menu, { size: 20 })
						})
					]
				}), menuOpen && /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell grid gap-4 border-t border-border py-5 text-sm font-semibold md:hidden",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("a", {
							href: "#personal",
							children: "Personal"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("a", {
							href: "#business",
							children: "Business"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("a", {
							href: "#platform",
							children: "Platform"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("a", {
							href: "#security",
							children: "Help"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
							className: "w-fit",
							children: "Sign up"
						})
					]
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("section", {
				id: "top",
				className: "overflow-hidden bg-background pt-12 text-center sm:pt-16",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("h1", {
							className: "display-shout motion-hero-title mx-auto max-w-[1120px] text-[52px] text-obsidian sm:text-[89px] lg:text-[105px]",
							children: [
								"Money for here,",
								/* @__PURE__ */ (0, import_jsx_runtime.jsx)("br", {}),
								"there and everywhere"
							]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "motion-hero-copy mx-auto mt-8 max-w-xl text-lg text-charcoal",
							children: "160 countries and territories. 40 currencies. Get the account built to save you money round the world."
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "motion-hero-actions mt-7 flex flex-wrap items-center justify-center gap-7",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								onClick: () => scrollTo("personal"),
								children: "Open an account"
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("button", {
								type: "button",
								onClick: () => scrollTo("send"),
								className: "text-link",
								children: "Send money now"
							})]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
							className: "mx-auto mt-12 h-[340px] max-w-[780px] overflow-hidden sm:h-[460px]",
							children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)("img", {
								src: globe_coins_default,
								width: 1536,
								height: 1024,
								alt: "A turquoise globe surrounded by gold coins",
								className: "motion-globe mx-auto w-full object-cover object-top"
							})
						})
					]
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("section", {
				className: "bg-background py-16",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
					className: "page-shell grid gap-9 md:grid-cols-3",
					children: trustItems.map(({ icon: Icon, title, text }, index) => /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						"data-reveal": true,
						style: { "--reveal-delay": `${index * 90}ms` },
						children: [
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Icon, {
								size: 24,
								strokeWidth: 1.8
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
								className: "mt-6 text-lg font-bold text-obsidian",
								children: title
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
								className: "mt-2 max-w-sm text-base text-pebble",
								children: text
							})
						]
					}, title))
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("section", {
				id: "send",
				className: "bg-primary py-16 sm:py-20",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell grid items-center gap-12 lg:grid-cols-[1fr_440px]",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						"data-reveal": "left",
						children: [
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
								className: "text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]",
								children: "Send money globally for less"
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", {
								className: "mt-6 max-w-md text-lg",
								children: [
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
										className: "text-link",
										children: "Save up to 8x"
									}),
									" on international transfers —",
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)("br", {}),
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
										className: "text-link",
										children: "with fees as low as 0.1%."
									})
								]
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "forest",
								className: "mt-8",
								onClick: () => scrollTo("comparison"),
								children: "Learn how to send money"
							})
						]
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
						"data-reveal": "right",
						children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(TransferCard, {
							amount,
							setAmount
						})
					})]
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					id: "comparison",
					className: "page-shell pt-24 text-center",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
							"data-reveal": true,
							className: "display-shout text-[45px] text-obsidian sm:text-[61px]",
							children: "Never pay a hidden fee again"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "mx-auto mt-4 max-w-lg text-lg leading-6",
							children: "Banks and other providers add markups to the exchange rate to make you pay more. Not us — see for yourself."
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-7 flex flex-wrap justify-center gap-8",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "forest",
								children: "Send money now"
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("button", {
								type: "button",
								className: "text-link",
								children: "Learn how to send money"
							})]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
							"data-reveal": "scale",
							children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ComparisonTable, {
								amount,
								setAmount
							})
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", {
							className: "mt-7 text-xs",
							children: [
								"This applies when you pay in via bank transfer or ACH payments.",
								" ",
								/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
									className: "text-link",
									children: "How do we collect this data?"
								})
							]
						})
					]
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("section", {
				id: "business",
				className: "bg-background py-20",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					"data-reveal": "scale",
					className: "page-shell rounded-[28px] bg-brand-ink px-6 py-16 text-center text-brand-lime sm:px-16",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
							className: "mx-auto grid h-12 w-12 place-items-center rounded-full bg-spruce",
							children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(BriefcaseBusiness, {})
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
							className: "display-shout mt-7 text-[45px] sm:text-[61px]",
							children: "Built for business too"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
							className: "mx-auto mt-5 max-w-2xl text-base font-medium leading-6 text-paper",
							children: "Go global with our international business account. Make payments and get paid in 40+ currencies. Join over 700,000 businesses thriving with Slash Pay."
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-8 flex flex-wrap justify-center gap-8",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, { children: "Try demo" }), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
								variant: "link",
								className: "text-brand-lime",
								children: "Learn more"
							})]
						})
					]
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("section", {
				id: "security",
				className: "bg-background py-20",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "grid items-center gap-10 md:grid-cols-2",
						children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							"data-reveal": "left",
							children: [
								/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
									className: "text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]",
									children: "Disappoint thieves"
								}),
								/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
									className: "mt-6 max-w-md text-lg text-slate",
									children: "Every month, millions of our personal and business customers trust us to move over €14 billion of their money."
								}),
								/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
									className: "mt-8",
									children: "How we keep your money safe"
								})
							]
						}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("img", {
							"data-reveal": "right",
							src: security_lock_default,
							loading: "lazy",
							width: 1024,
							height: 1024,
							alt: "Turquoise security padlock",
							className: "motion-float mx-auto w-full max-w-[390px]"
						})]
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
						className: "mt-16 grid gap-10 md:grid-cols-3",
						children: [
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)(SecurityItem, {
								icon: LockKeyhole,
								text: "Our dedicated fraud and security teams work to keep your money safe"
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)(SecurityItem, {
								icon: ShieldCheck,
								text: "We use 2-factor authentication to protect your account"
							}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)(SecurityItem, {
								icon: Building2,
								text: "We hold your money with established financial institutions"
							})
						]
					})]
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)(FlagRibbon, {}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("section", {
				id: "platform",
				className: "bg-background py-20",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell space-y-28",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Story, {
						image: phone_travel_default,
						title: "Move your money worldwide",
						text: "Save money when you send, spend and get paid in different currencies. All you need, in one account, wherever you need it."
					}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Story, {
						image: get_paid_default,
						title: "Get paid in different currencies, fast",
						text: "Request and receive money with Slash Pay, then keep your balances organised in one clear account.",
						reverse: true
					})]
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("footer", {
				className: "border-t border-border bg-surface py-16",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "page-shell",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "flex items-center justify-between",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "text-5xl font-black italic",
								children: "➤Slash Pay"
							}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
								className: "flex gap-5",
								children: [
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
										className: "font-black",
										children: "f"
									}),
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)(X, {}),
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Instagram, {}),
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Youtube, {}),
									/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
										className: "font-black",
										children: "in"
									})
								]
							})]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-16 grid gap-5 text-base md:grid-cols-3",
							children: [
								/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
									className: "space-y-5",
									children: [
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Legal" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Research privacy policy" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Modern slavery statement" })
									]
								}),
								/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
									className: "space-y-5",
									children: [
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Privacy policy" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Complaints" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Accessibility" })
									]
								}),
								/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
									className: "space-y-5",
									children: [
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Cookie policy" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Country site map" }),
										/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Intellectual property" })
									]
								})
							]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-16 space-y-6 text-sm leading-6 text-charcoal",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "© 2026 Slash Pay" }), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Slash Pay is a product concept. Review all transfer details, fees and availability before confirming a transaction." })]
						})
					]
				})
			})
		]
	});
}
function TransferCard({ amount, setAmount }) {
	const [source, setSource] = (0, import_react.useState)(transferCurrencies[3]);
	const [target, setTarget] = (0, import_react.useState)(transferCurrencies[0]);
	const [pickerFor, setPickerFor] = (0, import_react.useState)(null);
	const [recipientDraft, setRecipientDraft] = (0, import_react.useState)("");
	const [isEditingRecipient, setIsEditingRecipient] = (0, import_react.useState)(false);
	const numericAmount = Math.max(0, Number(amount.replace(/,/g, "")) || 0);
	const rate = source.usdRate / target.usdRate;
	const fee = Math.min(numericAmount * .014, numericAmount);
	const received = Math.max(0, (numericAmount - fee) * rate);
	new Intl.NumberFormat("en-US", { maximumFractionDigits: 2 }).format(numericAmount);
	const formattedReceived = new Intl.NumberFormat("en-US", {
		minimumFractionDigits: 2,
		maximumFractionDigits: 2
	}).format(received);
	const formattedRate = new Intl.NumberFormat("en-US", {
		minimumFractionDigits: 4,
		maximumFractionDigits: 6
	}).format(rate);
	function selectCurrency(currency) {
		if (pickerFor === "source") setSource(currency);
		if (pickerFor === "target") setTarget(currency);
		setPickerFor(null);
	}
	function updateRecipient(value) {
		setRecipientDraft(value);
		const requestedAmount = Number(value.replace(/,/g, ""));
		if (!Number.isFinite(requestedAmount) || requestedAmount < 0) return;
		setAmount((requestedAmount / (rate * .986)).toFixed(2));
	}
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "relative rounded-[10px] bg-card p-6 text-left text-card-foreground shadow-[var(--shadow-card)] sm:p-8",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
				className: "flex justify-end",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("button", {
					type: "button",
					className: "inline-flex items-center gap-3 rounded-full bg-surface px-4 py-2 text-xs font-semibold transition hover:bg-brand-mist sm:text-sm",
					"aria-label": `Exchange rate: 1 ${source.code} equals ${formattedRate} ${target.code}`,
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)(LockKeyhole, {
							size: 18,
							"aria-hidden": "true"
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("span", {
							className: "border-l border-pebble/40 pl-3",
							children: [
								"1 ",
								source.code,
								" = ",
								formattedRate,
								" ",
								target.code
							]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
							"aria-hidden": "true",
							className: "text-lg leading-none",
							children: "›"
						})
					]
				})
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("label", {
				className: "mt-6 block text-sm text-pebble",
				children: "You send exactly"
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "mt-1 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(CurrencyChip, {
					currency: source,
					onClick: () => setPickerFor("source")
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("input", {
					value: amount,
					onChange: (e) => setAmount(e.target.value),
					inputMode: "decimal",
					"aria-label": "Amount to send",
					className: "min-w-0 border-0 bg-transparent text-right text-4xl font-black outline-none transition-all duration-200 focus:text-5xl focus:text-brand-ink sm:text-5xl sm:focus:text-6xl"
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", {
				className: "mt-3 rounded-[10px] bg-brand-mist px-3 py-2 text-xs sm:text-sm",
				children: [
					"Sending over 22,000 ",
					source.code,
					" or equivalent?",
					" ",
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("u", { children: "We’ll discount our fee" })
				]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("label", {
				className: "mt-7 block text-sm text-pebble",
				children: "Recipient gets"
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "mt-1 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(CurrencyChip, {
					currency: target,
					onClick: () => setPickerFor("target")
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("input", {
					value: isEditingRecipient ? recipientDraft : formattedReceived,
					onFocus: () => {
						setRecipientDraft(formattedReceived);
						setIsEditingRecipient(true);
					},
					onBlur: () => setIsEditingRecipient(false),
					onChange: (event) => updateRecipient(event.target.value),
					inputMode: "decimal",
					"aria-label": "Amount recipient gets",
					className: "min-w-0 border-0 bg-transparent text-right text-4xl font-black tabular-nums outline-none transition-all duration-200 focus:text-5xl focus:text-brand-ink sm:text-5xl sm:focus:text-6xl"
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "mt-5 border-t border-border pt-5",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(InfoRow, {
					icon: Clock3,
					label: "Arrives",
					value: "by Friday"
				}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(InfoRow, {
					icon: ReceiptText,
					label: "Estimated fees",
					value: `Included in ${source.code} amount`,
					end: `${new Intl.NumberFormat("en-US", {
						minimumFractionDigits: 2,
						maximumFractionDigits: 2
					}).format(fee)} ${source.code} ›`
				})]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
				className: "mt-5 text-xs text-pebble",
				children: "Your amount and recipient total update as you edit."
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
				className: "mt-5 w-full",
				children: "Send money"
			}),
			pickerFor && /* @__PURE__ */ (0, import_jsx_runtime.jsx)(CurrencyPicker, {
				selected: pickerFor === "source" ? source : target,
				onSelect: selectCurrency,
				onClose: () => setPickerFor(null)
			})
		]
	});
}
function FlagRibbon() {
	const ribbonRef = (0, import_react.useRef)(null);
	const [progress, setProgress] = (0, import_react.useState)(0);
	(0, import_react.useEffect)(() => {
		let animationFrame = 0;
		const updateProgress = () => {
			animationFrame = 0;
			const element = ribbonRef.current;
			if (!element) return;
			const rect = element.getBoundingClientRect();
			const viewportHeight = window.innerHeight;
			const nextProgress = Math.max(0, Math.min(1, (viewportHeight - rect.top) / (viewportHeight + rect.height * .35)));
			setProgress((current) => Math.abs(current - nextProgress) > .01 ? nextProgress : current);
		};
		const onScroll = () => {
			if (!animationFrame) animationFrame = window.requestAnimationFrame(updateProgress);
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
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		ref: ribbonRef,
		className: "flag-ribbon",
		style: { "--ribbon-progress": progress },
		"aria-label": "Currencies available with Slash Pay",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
			className: "flag-ribbon__runway",
			"aria-hidden": "true",
			children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
				className: "flag-ribbon__arrow",
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ArrowRight, { size: 44 })
			})
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", {
			className: "flag-ribbon__flags",
			"aria-hidden": "true",
			children: ribbonFlags.map((code, index) => /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
				className: "flag-ribbon__flag",
				style: { "--flag-index": index },
				children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)("img", {
					src: `https://flagcdn.com/w160/${code}.png`,
					alt: ""
				})
			}, code))
		})]
	});
}
function CurrencyChip({ currency, onClick }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("button", {
		type: "button",
		onClick,
		"aria-haspopup": "dialog",
		className: "inline-flex items-center gap-1 rounded-full bg-surface px-3 py-2 text-lg font-bold transition hover:bg-brand-mist focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-ink",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", { children: currency.flag }),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", { children: currency.code }),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)(ChevronDown, {
				size: 18,
				"aria-hidden": "true"
			})
		]
	});
}
function CurrencyPicker({ selected, onSelect, onClose }) {
	const [query, setQuery] = (0, import_react.useState)("");
	const popular = [
		"EUR",
		"INR",
		"USD"
	];
	const matches = transferCurrencies.filter((currency) => `${currency.code} ${currency.name}`.toLowerCase().includes(query.toLowerCase()));
	const popularMatches = matches.filter((currency) => popular.includes(currency.code));
	const allMatches = matches.filter((currency) => !popular.includes(currency.code));
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "absolute inset-x-5 top-16 z-30 max-h-[min(460px,calc(100vh-7rem))] overflow-auto rounded-xl bg-card p-3 shadow-[var(--shadow-card)] sm:left-5 sm:right-auto sm:top-20 sm:w-[360px]",
		role: "dialog",
		"aria-label": "Choose a currency",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "flex items-center gap-2 rounded-lg border-2 border-brand-ink px-2.5 py-1.5",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Search, {
						size: 18,
						"aria-hidden": "true"
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("input", {
						autoFocus: true,
						value: query,
						onChange: (event) => setQuery(event.target.value),
						placeholder: "Type a currency / country",
						className: "min-w-0 flex-1 bg-transparent text-base outline-none",
						"aria-label": "Search currencies"
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsx)("button", {
						type: "button",
						onClick: onClose,
						"aria-label": "Close currency menu",
						className: "text-xl leading-none",
						children: "×"
					})
				]
			}),
			popularMatches.length > 0 && /* @__PURE__ */ (0, import_jsx_runtime.jsx)(CurrencyGroup, {
				title: "Popular currencies",
				currencies: popularMatches,
				selected,
				onSelect
			}),
			allMatches.length > 0 && /* @__PURE__ */ (0, import_jsx_runtime.jsx)(CurrencyGroup, {
				title: "All currencies",
				currencies: allMatches,
				selected,
				onSelect
			}),
			matches.length === 0 && /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
				className: "p-5 text-sm text-pebble",
				children: "No currencies match that search."
			})
		]
	});
}
function CurrencyGroup({ title, currencies: groupCurrencies, selected, onSelect }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "mt-3",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
			className: "border-b border-border pb-2 text-base font-medium text-charcoal",
			children: title
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("div", { children: groupCurrencies.map((currency) => /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("button", {
			type: "button",
			onClick: () => onSelect(currency),
			className: "flex w-full items-center gap-2 px-2 py-2 text-left text-sm transition hover:bg-brand-mist",
			children: [
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
					className: "text-lg",
					children: currency.flag
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
					className: "font-semibold",
					children: currency.code
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
					className: "text-charcoal",
					children: currency.name
				}),
				selected.code === currency.code && /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
					className: "ml-auto font-bold text-brand-ink",
					children: "✓"
				})
			]
		}, currency.code)) })]
	});
}
function InfoRow({ icon: Icon, label, value, end }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "grid grid-cols-[24px_minmax(0,1fr)_auto] items-center gap-3 py-3",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Icon, { size: 20 }),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", { children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
				className: "block text-xs text-muted-foreground",
				children: label
			}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("strong", {
				className: "text-sm",
				children: value
			})] }),
			end && /* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
				className: "text-xs underline",
				children: end
			})
		]
	});
}
function ComparisonTable({ amount, setAmount }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
		className: "mt-10 overflow-x-auto rounded-[10px] bg-card p-5 text-left text-card-foreground shadow-[var(--shadow-elevated)] sm:p-8",
		children: [
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "grid min-w-[850px] grid-cols-3 gap-5",
				children: [
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("label", {
						className: "text-xs font-bold",
						children: ["Amount", /* @__PURE__ */ (0, import_jsx_runtime.jsx)("input", {
							value: amount,
							onChange: (e) => setAmount(e.target.value),
							className: "mt-2 h-12 w-full rounded-[10px] border border-input px-4 text-base font-normal outline-none focus:border-brand-ink"
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("label", {
						className: "text-xs font-bold",
						children: ["From", /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-2 flex h-12 items-center justify-between rounded-[10px] border border-input px-4 text-base font-normal",
							children: ["🇪🇺 EUR Euro ", /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ChevronDown, { size: 16 })]
						})]
					}),
					/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("label", {
						className: "text-xs font-bold",
						children: ["To", /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "mt-2 flex h-12 items-center justify-between rounded-[10px] border border-input px-4 text-base font-normal",
							children: ["🇺🇸 USD United States dollar ", /* @__PURE__ */ (0, import_jsx_runtime.jsx)(ChevronDown, { size: 16 })]
						})]
					})
				]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
				className: "mt-7 grid min-w-[960px] grid-cols-[165px_repeat(4,1fr)]",
				children: [/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "space-y-7 pt-[87px] text-sm text-pebble",
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("p", { children: [
							"Recipient gets",
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)("br", {}),
							/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "text-xs",
								children: "(Total after fees)"
							})
						] }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Exchange rate" }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Exchange rate markup" }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Transfer fee" }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: "Total transfer cost" })
					]
				}), providers.map((provider) => /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: `space-y-7 px-4 py-5 text-center text-sm ${provider.best ? "rounded-[10px] bg-primary" : ""}`,
					children: [
						/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
							className: "h-10 font-semibold",
							children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
								className: "mx-auto mb-2 grid h-8 w-8 place-items-center rounded-full bg-brand-ink text-xs text-brand-lime",
								children: provider.mark
							}), provider.name]
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("strong", {
							className: provider.best ? "text-brand-ink" : "text-brand-red",
							children: provider.recipient
						}),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: provider.exchangeRate }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: provider.markup }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: provider.fee }),
						/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", { children: provider.total })
					]
				}, provider.name))]
			}),
			/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
				variant: "link",
				className: "mt-6 w-full border-t border-border pt-5 text-sm",
				children: "Show more providers"
			})
		]
	});
}
function SecurityItem({ icon: Icon, text }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", { children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("span", {
		className: "grid h-14 w-14 place-items-center rounded-full bg-surface",
		children: /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Icon, { size: 24 })
	}), /* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
		className: "mt-5 max-w-sm font-semibold leading-6",
		children: text
	})] });
}
function Story({ image, title, text, reverse = false }) {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("article", {
		className: "grid items-center gap-14 md:grid-cols-2",
		children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)("img", {
			src: image,
			loading: "lazy",
			width: 1024,
			height: 1280,
			alt: "International money account in use",
			className: `aspect-[4/5] w-full rounded-[28px] object-cover ${reverse ? "md:order-2" : ""}`
		}), /* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
			className: reverse ? "md:order-1" : "",
			children: [
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("h2", {
					className: "text-[45px] font-bold leading-[1.1] text-obsidian sm:text-[61px]",
					children: title
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsx)("p", {
					className: "mt-6 max-w-lg text-lg text-slate",
					children: text
				}),
				/* @__PURE__ */ (0, import_jsx_runtime.jsxs)("div", {
					className: "mt-8 flex flex-wrap items-center gap-8",
					children: [/* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, { children: "Get Started" }), /* @__PURE__ */ (0, import_jsx_runtime.jsx)(Button, {
						variant: "link",
						className: "text-link px-0",
						children: "Explore the Slash Pay Account"
					})]
				})
			]
		})]
	});
}
//#endregion
export { WisePage as component };
