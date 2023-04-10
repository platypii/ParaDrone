import commonjs from "@rollup/plugin-commonjs"
import resolve from "@rollup/plugin-node-resolve"
import terser from "@rollup/plugin-terser"
import serve from "rollup-plugin-serve"
import livereload from "rollup-plugin-livereload"

const production = !process.env.ROLLUP_WATCH

export default {
  input: "render.js",
  output: {
    file: "html/js/bundle.js",
    format: "iife",
    name: "ParaDrone",
    sourcemap: true,
  },
  plugins: [
    commonjs(),
    resolve(),
    !production && serve({ contentBase: "html", port: 8080 }),
    !production && livereload({ watch: ["html"] }),
    production && terser(),
  ],
}
