const paradroneModel = require("../hardware/jscad/index.js").main
const jscadReglRenderer = require("@jscad/regl-renderer")
const { toPolygons } = require("@jscad/modeling").geometries.geom3
const { flatten } = require("@jscad/modeling").utils

// ********************
// Renderer configuration and initiation.
// ********************
const { prepareRender, drawCommands, cameras, entitiesFromSolids } = jscadReglRenderer

const perspectiveCamera = cameras.perspective
const orbitControls = jscadReglRenderer.controls.orbit

const container = document.getElementById("jscad")

const width = container.clientWidth
const height = container.clientHeight

const rendering = {
  background: [0, 0, 0, 0] // trans
}

// prepare the camera
let camera = {
  ...perspectiveCamera.defaults,
  position: [0, -700, 180]
}
perspectiveCamera.setProjection(camera, camera, { width, height })
perspectiveCamera.update(camera, camera)

// prepare the controls
let controls = {
  ...orbitControls.defaults,
  scale: 0.3
}

// prepare the renderer
const renderer = prepareRender({
  glOptions: { container },
})

const model = paradroneModel({})
flatten(model).forEach(toPolygons) // TODO: this is a workaround for jscad #985
const entities = entitiesFromSolids({}, model)

// the heart of rendering, as themes, controls, etc change
let updateView = true

const doRotatePanZoom = () => {

  if (rotateDelta[0] || rotateDelta[1]) {
    const updated = orbitControls.rotate({ controls, camera, speed: rotateSpeed }, rotateDelta)
    controls = { ...controls, ...updated.controls }
    updateView = true
    rotateDelta = [0, 0]
  }

  if (panDelta[0] || panDelta[1]) {
    const updated = orbitControls.pan({ controls, camera, speed: panSpeed }, panDelta)
    controls = { ...controls, ...updated.controls }
    panDelta = [0, 0]
    camera.position = updated.camera.position
    camera.target = updated.camera.target
    updateView = true
  }

  if (zoomDelta) {
    const updated = orbitControls.zoom({ controls, camera, speed: zoomSpeed }, zoomDelta)
    controls = { ...controls, ...updated.controls }
    zoomDelta = 0
    updateView = true
  }
}

const updateAndRender = (timestamp) => {
  doRotatePanZoom()

  // Rotate slowly
  rotateDelta[0] -= 0.5

  if (updateView) {
    const updates = orbitControls.update({ controls, camera })
    controls = { ...controls, ...updates.controls }
    updateView = controls.changed // for elasticity in rotate / zoom

    camera.position = updates.camera.position
    perspectiveCamera.update(camera)

    renderer({ camera, drawCommands, entities, rendering })
  }
  window.requestAnimationFrame(updateAndRender)
}
window.requestAnimationFrame(updateAndRender)

// convert HTML events (mouse movement) to viewer changes
let lastX = 0
let lastY = 0

const rotateSpeed = 0.002
const panSpeed = 1
const zoomSpeed = 0.08
let rotateDelta = [0, 0]
let panDelta = [0, 0]
let zoomDelta = 0
let pointerDown = false

const moveHandler = (ev) => {
  if(!pointerDown) return
  const dx = lastX - ev.pageX
  const dy = ev.pageY - lastY

  const shiftKey = (ev.shiftKey === true) || (ev.touches && ev.touches.length > 2)
  if (shiftKey) {
    panDelta[0] += dx
    panDelta[1] += dy
  } else {
    rotateDelta[0] -= dx
    rotateDelta[1] -= dy
  }

  lastX = ev.pageX
  lastY = ev.pageY

  ev.preventDefault()
}
const downHandler = (ev) => {
  pointerDown = true
  lastX = ev.pageX
  lastY = ev.pageY
  container.setPointerCapture(ev.pointerId)
  ev.preventDefault()
}

const upHandler = (ev) => {
  pointerDown = false
  container.releasePointerCapture(ev.pointerId)
  ev.preventDefault()
}

const wheelHandler = (ev) => {
  zoomDelta += ev.deltaY
  ev.preventDefault()
}

container.onpointermove = moveHandler
container.onpointerdown = downHandler
container.onpointerup = upHandler
container.onwheel = wheelHandler
