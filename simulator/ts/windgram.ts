
const MPH = 0.44704 // m/s
const windMax = 10 * MPH // m/s
const outerRing = 45 // pixels

export class Windgram {
  public vE: number = 0
  public vN: number = 0

  private readonly svg = document.getElementById("windgram")!
  private readonly line = document.getElementById("windgram-line") as any as SVGLineElement
  private readonly speedLabel = document.getElementById("wind-speed")!
  private readonly directionLabel = document.getElementById("wind-direction")!

  constructor() {
    this.update()
    this.svg.addEventListener("click", (e) => this.onClick(e))
  }

  private onClick(e: any) {
    this.vE = windMax * (e.offsetX - 50) / outerRing
    this.vN = windMax * (50 - e.offsetY) / outerRing
    this.update()
  }

  private update() {
    // Update SVGElement
    const scale = outerRing / windMax
    this.line.setAttribute("x2", (50 + this.vE * scale).toString())
    this.line.setAttribute("y2", (50 - this.vN * scale).toString())
    // Update labels
    const speed = Math.sqrt(this.vE * this.vE + this.vN * this.vN) / MPH // mph
    const direction = Math.atan2(this.vN, this.vE) * 180 / Math.PI // degrees
    this.speedLabel.innerText = speed.toFixed(1) + " mph"
    this.directionLabel.innerText = direction.toFixed(0) + "°"
  }
}
