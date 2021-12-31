
let showing = true

export function init() {
  // Dismiss on click anywhere
  document.body.addEventListener("mousedown", () => dismiss())
  document.body.addEventListener("click", () => dismiss())
}
function dismiss() {
  if (showing) {
    document.getElementById("welcome")!.style.display = "none"
    showing = false
  }
}
