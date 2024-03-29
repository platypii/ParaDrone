import { max } from "d3-array"
import { Selection } from "d3-selection"
import { line } from "d3-shape"
import { SimStep } from "../sim"
import { BaseChart } from "./base-chart"

export class ErrorChart extends BaseChart {
  private errorPath: Selection<SVGPathElement, unknown, HTMLElement, unknown>
  private left: Selection<SVGGElement, unknown, HTMLElement, unknown>
  private right: Selection<SVGGElement, unknown, HTMLElement, unknown>
  private focusLine: Selection<SVGLineElement, unknown, HTMLElement, unknown>
  private timeSteps: number = 107

  constructor() {
    super()

    // Add error line
    this.errorPath = this.layers.append("path")
      .attr("class", "plan-error")
      .style("fill", "none")
      .style("stroke", "#22f")
      .style("stroke-width", 1.5)
      .style("stroke-linecap", "round")
      .style("stroke-linejoin", "round")

    // Add left/right rects for each data point
    this.left = this.svg.append("g")
      .attr("class", "left")
    this.right = this.svg.append("g")
      .attr("class", "right")

    // Focus line
    this.focusLine = this.layers.append("line")
      .attr("class", "focus")
      .attr("x1", 0)
      .attr("x2", this.width - 30)
      .style("stroke", "#aaa")
      .style("stroke-width", 1)
      .style("opacity", 0)
  }

  public setFocus(i: number): void {
    if (i === 0) {
      this.focusLine.style("opacity", 0)
    } else {
      const y = Math.floor(this.yAxis.scale(i)) + 0.5
      this.focusLine
        .attr("y1", y)
        .attr("y2", y)
      this.focusLine.style("opacity", 1)
    }
  }

  public update(steps: SimStep[]): void {
    const errors = steps.map((s) => s.score.score)
    const errorBound = Math.max(max(errors) || 1, 200)
    this.timeSteps = Math.max(this.timeSteps, steps.length)
    this.xAxis.scale.domain([0, errorBound])
    this.yAxis.scale.domain([0, this.timeSteps])
    this.updateAxes()

    // Update error line
    const errorLine = line<number>()
      .x((d: number) => this.xAxis.scale(d))
      .y((d: number, i: number) => this.yAxis.scale(steps.length - i))
    this.errorPath
      .attr("d", errorLine(errors) || "")

    // Update left/right toggle controls
    const segmentSize = Math.ceil(this.height / steps.length)

    // Left red
    const left: Selection<any, SimStep, SVGGElement, unknown> = this.left.selectAll("rect")
      .data(steps)
    left.enter()
      .append("rect")
      .attr("width", 15)
      .attr("height", segmentSize)
      .style("fill", "#d11")
      .merge(left)
      .attr("y", (d: SimStep, i: number) => Math.floor(this.yAxis.scale(steps.length - i)))
      .style("opacity", (d: SimStep) => d.controls.left / 255)

    // Right green
    const right: Selection<any, SimStep, SVGGElement, unknown> = this.right.selectAll("rect")
      .data(steps)
    right.enter()
      .append("rect")
      .attr("width", 15)
      .attr("height", segmentSize)
      .attr("x", this.width - 15)
      .style("fill", "#1b1")
      .merge(right)
      .attr("y", (d: SimStep, i: number) => Math.floor(this.yAxis.scale(steps.length - i)))
      .style("opacity", (d: SimStep) => d.controls.right / 255)

    left.exit().remove()
    right.exit().remove()

    super.updateAxes()
  }
}
