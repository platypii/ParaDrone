import javascript from "@eslint/js"
import typescript from "@typescript-eslint/eslint-plugin"
import globals from "globals"

export default [
  {
    plugins: {
      "@typescript-eslint": typescript,
    },

    ignores: ["js/**"],

    languageOptions: {
      globals: {
        ...globals.browser,
        ...globals.node,
      },
    },

    rules: {
      ...javascript.configs.recommended.rules,
      ...typescript.configs.recommended.rules,
      "@typescript-eslint/array-type": ["error", {"default": "array-simple"}],
      "@typescript-eslint/no-explicit-any": "off",
      "@typescript-eslint/no-inferrable-types": ["error", { "ignoreProperties": true }],
      "@typescript-eslint/no-non-null-assertion": "off",
      "@typescript-eslint/no-shadow": "error",
      "comma-dangle": ["error", "always-multiline"],
      "indent": ["error", 2],
      "no-redeclare": "error",
      "no-shadow": "off",
      "no-throw-literal": "error",
      "no-trailing-spaces": "error",
      "quotes": "error",
      "semi": ["error", "never"],
    },
  },
]
