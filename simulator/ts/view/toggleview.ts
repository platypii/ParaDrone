import { Paraglider } from "../paraglider"

export class ToggleView {
  private readonly left = document.getElementById("toggle-left")!
  private readonly right = document.getElementById("toggle-right")!
  private readonly leftTarget = document.getElementById("toggle-left-target")!
  private readonly rightTarget = document.getElementById("toggle-right-target")!

  public update(para: Paraglider) {
    this.left.innerText = para.toggles.currentPosition.left.toFixed(0)
    this.right.innerText = para.toggles.currentPosition.right.toFixed(0)
    this.leftTarget.innerText = para.toggles.targetPosition.left.toFixed(0)
    this.rightTarget.innerText = para.toggles.targetPosition.right.toFixed(0)
  }
}
