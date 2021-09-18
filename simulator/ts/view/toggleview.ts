import { Paraglider } from "../paraglider"

export class ToggleView {
  private readonly leftTarget = document.getElementById("toggle-left-target")!
  private readonly rightTarget = document.getElementById("toggle-right-target")!
  private readonly leftPosition = document.getElementById("toggle-left-position")!
  private readonly rightPosition = document.getElementById("toggle-right-position")!
  private readonly leftActual = document.getElementById("toggle-left-actual")!
  private readonly rightActual = document.getElementById("toggle-right-actual")!
  private readonly leftSpeed = document.getElementById("toggle-left-speed")!
  private readonly rightSpeed = document.getElementById("toggle-right-speed")!
  private readonly leftToggle = document.getElementById("toggle-left")!
  private readonly rightToggle = document.getElementById("toggle-right")!

  public update(para: Paraglider) {
    this.leftTarget.innerText = para.toggles.left.target.toFixed(0)
    this.rightTarget.innerText = para.toggles.right.target.toFixed(0)
    this.leftPosition.innerText = para.toggles.left.position.toFixed(0)
    this.rightPosition.innerText = para.toggles.right.position.toFixed(0)
    this.leftActual.innerText = para.toggles.left.actualPosition.toFixed(0)
    this.rightActual.innerText = para.toggles.right.actualPosition.toFixed(0)
    this.leftActual.style.color = para.toggles.left.actualPosition < 0 ? "#d11" : "black"
    this.rightActual.style.color = para.toggles.right.actualPosition < 0 ? "#d11" : "black"
    this.leftSpeed.innerText = para.toggles.left.speed.toFixed(0)
    this.rightSpeed.innerText = para.toggles.right.speed.toFixed(0)

    const height = 55
    this.leftToggle.style.top = para.toggles.left.position * height / 255 + "px"
    this.rightToggle.style.top = para.toggles.right.position * height / 255 + "px"
  }
}
