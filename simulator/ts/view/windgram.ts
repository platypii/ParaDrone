import * as d3 from "d3"

const MPH = 0.44704 // m/s
const windMax = 10 * MPH // m/s

export class Windgram {
  public vE: number = -0
  public vN: number = -0

  private line: d3.Selection<SVGLineElement, unknown, HTMLElement, unknown>
  private readonly speedLabel = document.getElementById("wind-speed")!
  private readonly directionLabel = document.getElementById("wind-direction")!

  private readonly listeners: Array<() => void> = []

  private readonly radius: number

  constructor() {
    const width = 100
    const height = 100
    this.radius = Math.min(width, height) / 2

    const svg: d3.Selection<Element, unknown, HTMLElement, unknown> = d3.select("svg#windgram")
    svg.attr("width", width)
    svg.attr("height", height)

    // Add reticle target
    svg.append("circle")
      .attr("cx", this.radius)
      .attr("cy", this.radius)
      .attr("r", (this.radius - 5) / 2)
      .style("stroke-width", "1px")
      .style("stroke", "#bbb")
      .style("fill", "none")
    svg.append("circle")
      .attr("cx", this.radius)
      .attr("cy", this.radius)
      .attr("r", this.radius - 5)
      .style("stroke-width", "1px")
      .style("stroke", "#bbb")
      .style("fill", "none")
    this.line = svg.append("line")
      .attr("id", "windgram-line")
      .attr("x1", this.radius)
      .attr("y1", this.radius)
      .attr("x2", this.radius)
      .attr("y2", this.radius)
      .style("stroke-width", "3px")
      .style("stroke", "#eee")

    svg.on("click", (e) => this.onClick(e.offsetX, e.offsetY))

    const dragHandler = d3.drag().on("drag", (e) => this.onClick(e.x, e.y))
    svg.call(dragHandler)

    this.update()
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

  private onClick(x: number, y: number) {
    this.vE = windMax * (this.radius - x) / this.radius
    this.vN = windMax * (y - this.radius) / this.radius
    this.update()
    this.listeners.forEach((l) => l())
  }

  private update() {
    // Update SVGElement
    const scale = this.radius / windMax
    this.line.attr("x2", (this.radius - this.vE * scale).toString())
    this.line.attr("y2", (this.radius + this.vN * scale).toString())
    // Update labels
    const speed = Math.sqrt(this.vE * this.vE + this.vN * this.vN) / MPH // mph
    const direction = (Math.atan2(-this.vE, -this.vN) * 180 / Math.PI + 360) % 360 // degrees
    this.speedLabel.innerText = speed.toFixed(1) + " mph"
    this.directionLabel.innerText = direction.toFixed(0) + "°"
  }
}
