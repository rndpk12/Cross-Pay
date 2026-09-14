import { n as __toESM } from "../_runtime.mjs";
import { n as require_react } from "../_libs/@radix-ui/react-compose-refs+[...].mjs";
import { n as require_jsx_runtime } from "../_libs/react+tanstack__react-query.mjs";
import { t as Slot } from "../_libs/radix-ui__react-slot.mjs";
import { n as clsx, t as cva } from "../_libs/class-variance-authority+clsx.mjs";
import { t as twMerge } from "../_libs/tailwind-merge.mjs";
//#region node_modules/.nitro/vite/services/ssr/assets/button-CCKletB4.js
var import_react = /* @__PURE__ */ __toESM(require_react());
var import_jsx_runtime = require_jsx_runtime();
function cn(...inputs) {
	return twMerge(clsx(inputs));
}
var buttonVariants = cva("inline-flex shrink-0 items-center justify-center gap-2 rounded-full text-base font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50", {
	variants: {
		variant: {
			default: "bg-primary text-primary-foreground hover:bg-primary/85",
			lime: "bg-primary text-primary-foreground hover:bg-primary/85",
			forest: "bg-brand-ink text-brand-lime hover:bg-brand-ink/90",
			destructive: "bg-destructive text-destructive-foreground hover:bg-destructive/90",
			outline: "border border-brand-ink bg-transparent text-brand-ink hover:bg-brand-mist",
			secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
			ghost: "hover:bg-accent hover:text-accent-foreground",
			link: "text-primary underline-offset-4 hover:underline"
		},
		size: {
			default: "h-[46px] px-6",
			sm: "h-9 px-4",
			lg: "h-14 px-9",
			icon: "size-10 p-0",
			"icon-sm": "size-8 p-0",
			"icon-lg": "size-12 p-0"
		}
	},
	defaultVariants: {
		variant: "default",
		size: "default"
	}
});
var Button = import_react.forwardRef(({ className, variant, size, asChild = false, ...props }, ref) => {
	return /* @__PURE__ */ (0, import_jsx_runtime.jsx)(asChild ? Slot : "button", {
		ref,
		className: cn(buttonVariants({
			variant,
			size,
			className
		})),
		...props
	});
});
Button.displayName = "Button";
//#endregion
export { Button as t };
