import commonjs from "@rollup/plugin-commonjs"
import resolve from "@rollup/plugin-node-resolve"
import terser from "@rollup/plugin-terser"

export default {
  input: "render.js",
  output: {
    file: "html/js/bundle.js",
    format: "iife",
    name: "ParaDrone",
    sourcemap: true,
  },
  plugins: [
    resolve(),
    commonjs(),
    terser(),
  ],
}
