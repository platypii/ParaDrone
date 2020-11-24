
const MPH = 0.44704 // m/s
const windMax = 10 * MPH // m/s
const outerRing = 45 // pixels

export class Windgram {
  public vE: number = -0
  public vN: number = -0

  private readonly svg = document.getElementById("windgram")!
  private readonly line = document.getElementById("windgram-line") as any as SVGLineElement
  private readonly speedLabel = document.getElementById("wind-speed")!
  private readonly directionLabel = document.getElementById("wind-direction")!

  private readonly listeners: Array<() => void> = []

  constructor() {
    this.update()
    this.svg.addEventListener("click", (e) => this.onClick(e))
  }

  public vel(): number {
    return Math.sqrt(this.vE * this.vE + this.vN * this.vN)
  }

  /**
   * Wind bearing in radians
   */
  public bear(): number {
    return Math.atan2(this.vE, this.vN)
  }

  public onChange(cb: () => void) {
    this.listeners.push(cb)
  }

  private onClick(e: any) {
    this.vE = windMax * (50 - e.offsetX) / outerRing
    this.vN = windMax * (e.offsetY - 50) / outerRing
    this.update()
    this.listeners.forEach((l) => l())
  }

  private update() {
    // Update SVGElement
    const scale = outerRing / windMax
    this.line.setAttribute("x2", (50 - this.vE * scale).toString())
    this.line.setAttribute("y2", (50 + this.vN * scale).toString())
    // Update labels
    const speed = Math.sqrt(this.vE * this.vE + this.vN * this.vN) / MPH // mph
    const direction = (Math.atan2(-this.vE, -this.vN) * 180 / Math.PI + 360) % 360 // degrees
    this.speedLabel.innerText = speed.toFixed(1) + " mph"
    this.directionLabel.innerText = direction.toFixed(0) + "°"
  }
}
