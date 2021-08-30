import * as d3 from "d3"
import { SimStep } from "../sim"
import { BaseChart } from "./base-chart"

export class ErrorChart extends BaseChart {
  private errorPath: d3.Selection<SVGPathElement, any, HTMLElement, any>
  private left: d3.Selection<SVGGElement, any, HTMLElement, any>
  private right: d3.Selection<SVGGElement, any, HTMLElement, any>

  constructor() {
    super()

    // Add error line
    this.errorPath = this.layers.append("g")
      .attr("class", "plan-error")
      .append("path")
      .style("fill", "none")
      .style("stroke", "#11d")
      .style("stroke-width", 1.2)
      .style("stroke-linecap", "round")
      .style("stroke-linejoin", "round")

    // Add left/right rects for each data point
    this.left = this.svg.append("g")
      .attr("class", "left")
    this.right = this.svg.append("g")
      .attr("class", "right")
  }

  public update(steps: SimStep[]): void {
    const errors = steps.map((s) => s.score.score)
    const upperBound = Math.max(d3.max(errors) || 1, 100)
    this.xAxis.scale.domain([0, upperBound])
    this.yAxis.scale.domain([0, steps.length])
    this.updateAxes()

    // Update error line
    const line = d3.line<number>()
      .x((d: number) => this.xAxis.scale(d))
      .y((d: number, i: number) => this.yAxis.scale(i))
    this.errorPath
      .attr("d", line(errors) || "")

    // Update left/right toggle controls
    const segmentSize = Math.ceil(this.height / steps.length)

    // Left red
    const left: d3.Selection<any, SimStep, SVGGElement, any> = this.left.selectAll("rect")
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
    const right: d3.Selection<any, SimStep, SVGGElement, any> = this.right.selectAll("rect")
      .data(steps)
    right.enter()
      .append("rect")
      .attr("width", 15)
      .attr("height", segmentSize)
      .attr("x", this.width - 15)
      .style("fill", "#1d1")
      .merge(right)
      .attr("y", (d: SimStep, i: number) => Math.floor(this.yAxis.scale(steps.length - i)))
      .style("opacity", (d: SimStep) => d.controls.right / 255)

    left.exit().remove()
    right.exit().remove()

    super.updateAxes()
  }
}
