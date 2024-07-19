import typescriptEslint from "@typescript-eslint/eslint-plugin";
import prettier from "eslint-plugin-prettier";
import simpleImportSort from "eslint-plugin-simple-import-sort";
import globals from "globals";
import tsParser from "@typescript-eslint/parser";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});
export default [...compat.extends(
  "eslint:recommended",
  "plugin:@typescript-eslint/recommended",
).map(config => ({files: ["**/*.ts"], ...config})), {
    plugins: {
        "@typescript-eslint": typescriptEslint,
        prettier,
        "simple-import-sort": simpleImportSort,
    },

    languageOptions: {
        globals: {
            ...Object.fromEntries(Object.entries(globals.browser).map(([key]) => [key, "off"])),
            self: true,
        },

        parser: tsParser,
    },

    settings: {
        "import/resolver": {
            node: {
                extensions: [".js", ".json", ".ts"],
            },
        },
    },

    rules: {
        "no-console": 0,

        "max-len": ["error", {
            code: 140,
            ignoreUrls: true,
        }],

        "prefer-template": "off",
        "arrow-body-style": "off",
        "padded-blocks": "off",
        "class-methods-use-this": "off",
        "no-else-return": "off",
        "lines-between-class-members": "off",
        "no-param-reassign": "off",
        "no-return-assign": "off",
        "no-plusplus": "off",
        "prefer-destructuring": "off",
        "sort-imports": "off",
        "simple-import-sort/imports": "warn",
        "no-restricted-globals": "off",

        "@typescript-eslint/explicit-function-return-type": ["warn", {
            allowExpressions: true,
        }],

        "@typescript-eslint/interface-name-prefix": "off",
        "@typescript-eslint/explicit-member-accessibility": "error",

        "@typescript-eslint/no-inferrable-types": ["error", {
            ignoreParameters: true,
            ignoreProperties: true,
        }],

        "@typescript-eslint/no-explicit-any": ["warn", {
            fixToUnknown: true,
        }],

        "@typescript-eslint/no-unused-vars": ["warn", {
            argsIgnorePattern: "^_",
        }],

        "prettier/prettier": "warn",
    },
}, {
    files: ["**/*.test.ts"],

    languageOptions: {
        globals: {
            window: true,
        },
    },

    rules: {
        "import/first": 0,
        "@typescript-eslint/no-explicit-any": "off",
        "@typescript-eslint/no-unused-vars": "off",
    },
}];
