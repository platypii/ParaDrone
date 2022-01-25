import * as d3 from "d3"

interface Axis {
  scale: d3.ScaleLinear<number, number>
  axis?: d3.Axis<any>
  layer?: any
}

export class BaseChart {
  protected readonly svg: d3.Selection<d3.BaseType, unknown, HTMLElement, any>
  protected readonly layers: d3.Selection<SVGGElement, unknown, HTMLElement, any>
  protected readonly xAxis: Axis = {
    scale: d3.scaleLinear()
  }
  protected readonly yAxis: Axis = {
    scale: d3.scaleLinear()
  }
  protected width: number = 185
  protected height: number = 160

  constructor() {
    this.svg = d3.select("#sim-error-chart")
    this.layers = this.svg.append("g")
      .attr("class", "layers")
      .attr("transform", `translate(15,0)`)

    // X Axis
    this.xAxis.scale.range([0, this.width - 30])
    this.xAxis.axis = d3.axisTop(this.xAxis.scale)
      .ticks(4)
      .tickFormat((d) => d ? `${d}` : "")
    this.xAxis.layer = this.layers.append("g")
      .attr("transform", `translate(0,${this.height})`)
      .style("color", "#888")

    // Y Axis
    this.yAxis.scale.range([this.height, 0])
    this.yAxis.axis = d3.axisRight(this.yAxis.scale)
      .ticks(6)
      .tickFormat((d) => d ? `${d}s` : "")
    this.yAxis.layer = this.layers.append("g")
      .style("color", "#888")
  }

  public updateAxes(): void {
    this.xAxis.layer.call(this.xAxis.axis)
    this.yAxis.layer.call(this.yAxis.axis)
  }
}
