import resolve from "@rollup/plugin-node-resolve"
import terser from "@rollup/plugin-terser"
import typescript from "@rollup/plugin-typescript"
import serve from "rollup-plugin-serve"
import livereload from "rollup-plugin-livereload"

const production = !process.env.ROLLUP_WATCH;

export default {
  input: "ts/main.ts",
  output: {
    file: "js/bundle.js",
    format: "iife",
    name: "ParaDrone",
    sourcemap: true,
    globals: {
      cesium: "Cesium",
    }
  },
  plugins: [
    resolve(),
    typescript(),
    !production && serve({ contentBase: ".", port: 8080 }),
    !production && livereload({ watch: ["index.html", "js", "css"] }),
    production && terser(),
  ],
  external: ["cesium"],
}
