import js from "@eslint/js";
import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import tseslint from "typescript-eslint";

export default tseslint.config(
	{ ignores: ["dist", "build", "*.cjs", "*.mjs"] },
	{
		extends: [js.configs.recommended, ...tseslint.configs.recommended],
		files: ["**/*.{ts,tsx}"],
		languageOptions: {
			ecmaVersion: 2020,
			globals: globals.browser,
			parserOptions: {
				ecmaVersion: "latest",
				sourceType: "module",
			},
		},
		plugins: {
			"react-hooks": reactHooks,
			"react-refresh": reactRefresh,
		},
		rules: {
			...reactHooks.configs.recommended.rules,
			"react-refresh/only-export-components": [
				"warn",
				{ allowConstantExport: true },
			],
			// Disable the problematic rule
			"no-unused-expressions": "off",
			"@typescript-eslint/no-unused-expressions": "off",
			// Other disabled rules
			"@typescript-eslint/no-unused-vars": "off",
			"@typescript-eslint/no-unused-imports": "off",
			"@typescript-eslint/ban-ts-comment": "off",
			"@typescript-eslint/prefer-ts-expect-error": "off",
			"@typescript-eslint/no-explicit-any": "off",
		},
	}
);