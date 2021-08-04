import * as d3 from "d3"

interface Axis {
  scale: d3.ScaleLinear<number, number>
  axis?: d3.Axis<any>
  layer?: any
}

export class ErrorChart {
  private readonly svg: d3.Selection<d3.BaseType, unknown, HTMLElement, any>
  private readonly xAxis: Axis = {
    scale: d3.scaleLinear()
  }
  private readonly yAxis: Axis = {
    scale: d3.scaleLinear()
  }
  private path: any

  constructor() {
    this.svg = d3.select("#sim-error-chart")
    const width = 200
    const height = 120

    // X Axis
    this.xAxis.scale.range([0, width])
    this.xAxis.axis = d3.axisBottom(this.xAxis.scale)
      .ticks(8)
    this.xAxis.layer = this.svg.append("g")

    // Y Axis
    this.yAxis.scale.range([height, 0])
    this.yAxis.axis = d3.axisRight(this.yAxis.scale)
      .ticks(6)
    this.yAxis.layer = this.svg.append("g")

    // Construct d3 line
    this.path = this.svg.append("path")
      .style("fill", "none")
      .style("stroke", "#d11")
      .style("stroke-width", 1.2)
      .style("stroke-linecap", "round")
      .style("stroke-linejoin", "round")
  }

  public update(errors: number[]): void {
    const upperBound = Math.max(d3.max(errors) || 1, 100)
    this.xAxis.scale.domain([0, errors.length])
    this.yAxis.scale.domain([0, upperBound])
    this.xAxis.layer.call(this.xAxis.axis)
    this.yAxis.layer.call(this.yAxis.axis)

    // Construct d3 line
    const line = d3.line<number>()
      .x((d: number, i: number) => this.xAxis.scale(i))
      .y((d: number) => this.yAxis.scale(d))
    this.path
      .attr("d", line(errors) || "")
  }
}
