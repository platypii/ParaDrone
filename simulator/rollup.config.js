import resolve from "@rollup/plugin-node-resolve"
import terser from "@rollup/plugin-terser"
import typescript from "@rollup/plugin-typescript"

export default {
  input: "ts/main.ts",
  output: {
    file: "js/bundle.js",
    format: "iife",
    name: "ParaDrone",
    sourcemap: true,
  },
  plugins: [
    typescript(),
    resolve(),
    terser(),
  ],
  external: ["cesium"],
}
