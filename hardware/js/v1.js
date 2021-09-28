const { cuboid, cylinder, cylinderElliptic, roundedCuboid } = require('@jscad/modeling').primitives
const { rotate, translate } = require('@jscad/modeling').transforms

module.exports = {
  /**
   * @deprecated Migrate to jscad v2 cuboid
   */
  cube: (opt) => {
    opt.center = [opt.size[0] / 2, opt.size[1] / 2, opt.size[2] / 2]
    if (opt.radius) {
      opt.roundRadius = opt.radius
      return roundedCuboid(opt)
    } else {
      return cuboid(opt)
    }
  },
  /**
   * @deprecated Migrate to jscad v2 cylinder and cylinderElliptic
   */
  cylinderV1: (opt) => {
    const rot = [0, 0, 0]
    const center = [0, 0, 0]
    if (opt.start && opt.end) {
      center[0] = (opt.start[0] + opt.end[0]) / 2
      center[1] = (opt.start[1] + opt.end[1]) / 2
      center[2] = (opt.start[2] + opt.end[2]) / 2
      opt.height = Math.hypot(opt.start[0] - opt.end[0], opt.start[1] - opt.end[1], opt.start[2] - opt.end[2])
      if (opt.start[0] !== opt.end[0]) {
        rot[1] = -Math.PI / 2 * Math.sign(opt.start[0] - opt.end[0])
      }
      if (opt.start[1] !== opt.end[1]) {
        rot[0] = Math.PI / 2 * Math.sign(opt.start[1] - opt.end[1])
      }
    }
    if (opt.r1 === 0) {
      opt.r1 = 0.01
    }
    if (opt.r2 === 0) {
      opt.r2 = 0.01
    }
    if (opt.r1 && opt.r2) {
      opt.startRadius = [opt.r1, opt.r1]
      opt.endRadius = [opt.r2, opt.r2]
      return translate(center, rotate(rot, cylinderElliptic(opt)))
    } else {
      return translate(center, rotate(rot, cylinder(opt)))
    }
  }
}
