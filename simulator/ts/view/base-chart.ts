import { axisRight, axisTop, Axis } from "d3-axis"
import { NumberValue, scaleLinear, ScaleLinear } from "d3-scale"
import { BaseType, select, Selection } from "d3-selection"

interface BaseAxis {
  scale: ScaleLinear<number, number>
  axis?: Axis<NumberValue>
  layer?: Selection<SVGGElement, unknown, HTMLElement, unknown>
}

export class BaseChart {
  protected readonly svg: Selection<BaseType, unknown, HTMLElement, unknown>
  protected readonly layers: Selection<SVGGElement, unknown, HTMLElement, unknown>
  protected readonly xAxis: BaseAxis = {
    scale: scaleLinear()
  }
  protected readonly yAxis: BaseAxis = {
    scale: scaleLinear()
  }
  protected width: number = 185
  protected height: number = 160

  constructor() {
    this.svg = select("#sim-error-chart")
    this.layers = this.svg.append("g")
      .attr("class", "layers")
      .attr("transform", "translate(15,0)")

    // X Axis
    this.xAxis.scale.range([0, this.width - 30])
    this.xAxis.axis = axisTop(this.xAxis.scale)
      .ticks(4)
      .tickFormat((d) => d ? `${d}` : "")
    this.xAxis.layer = this.layers.append("g")
      .attr("transform", `translate(0,${this.height})`)
      .style("color", "#888")

    // Y Axis
    this.yAxis.scale.range([this.height, 0])
    this.yAxis.axis = axisRight(this.yAxis.scale)
      .ticks(6)
      .tickFormat((d) => d ? `${d}s` : "")
    this.yAxis.layer = this.layers.append("g")
      .style("color", "#888")
  }

  public updateAxes(): void {
    this.xAxis.layer!.call(this.xAxis.axis!)
    this.yAxis.layer!.call(this.yAxis.axis!)
  }
}
