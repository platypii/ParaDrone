import { Paraglider } from "../paraglider"

export class ToggleView {
  private readonly leftTarget = document.getElementById("toggle-left-target")!
  private readonly rightTarget = document.getElementById("toggle-right-target")!
  private readonly leftPosition = document.getElementById("toggle-left-position")!
  private readonly rightPosition = document.getElementById("toggle-right-position")!
  private readonly leftSpeed = document.getElementById("toggle-left-speed")!
  private readonly rightSpeed = document.getElementById("toggle-right-speed")!

  public update(para: Paraglider) {
    this.leftTarget.innerText = para.toggles.left.target.toFixed(0)
    this.rightTarget.innerText = para.toggles.right.target.toFixed(0)
    this.leftPosition.innerText = para.toggles.left.position.toFixed(0)
    this.rightPosition.innerText = para.toggles.right.position.toFixed(0)
    this.leftSpeed.innerText = para.toggles.left.speed.toFixed(0)
    this.rightSpeed.innerText = para.toggles.right.speed.toFixed(0)
  }
}
