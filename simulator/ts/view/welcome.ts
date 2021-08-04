
let showing = true

export function init() {
  // Dismiss on click anywhere
  document.body.addEventListener("click", () => {
    if (showing) {
      document.getElementById("welcome")!.style.display = "none"
      showing = false
    }
  })
}
