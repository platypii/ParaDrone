import * as THREE from "https://threejsfundamentals.org/threejs/resources/threejs/r132/build/three.module.js"
import { OrbitControls } from "https://threejsfundamentals.org/threejs/resources/threejs/r132/examples/jsm/controls/OrbitControls.js"
import { OBJLoader } from "https://threejsfundamentals.org/threejs/resources/threejs/r132/examples/jsm/loaders/OBJLoader.js"

let model = undefined

export function init() {
  const width = 300
  const height = 180
  const renderer = new THREE.WebGLRenderer({antialias: true})
  const scene = new THREE.Scene()
  scene.background = new THREE.Color(0xffffff)
  const camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000)
  camera.position.z = 200
  camera.position.y = 50
  renderer.setSize(width, height)
  renderer.physicallyCorrectLights = true
  renderer.gammaOutput = true
  renderer.gammaFactor = 2.2
  document.getElementById("three").appendChild(renderer.domElement)
  new OrbitControls(camera, renderer.domElement)

  // Add lights
  // scene.add(new THREE.AmbientLight(0xeeeeee, 0.2))
  scene.add(new THREE.HemisphereLight(0xffffbb, 0x222244, 0.9))
  const light = new THREE.DirectionalLight(0xeeeecc, 0.9)
  light.position.y = 100
  scene.add(light)

  // Load model
  new OBJLoader().load("paradrone.obj", (obj) => {
    model = obj
    model.traverse((node) => {
      if (node.isMesh) {
        node.material.flatShading = THREE.SmoothShading
      }
    })
    model.rotation.x = -Math.PI / 2
    scene.add(model)
    renderer.render(scene, camera)
  })

  // Start rendering
  function animate() {
    if (model) {
      model.rotation.z += 0.004
    }

    requestAnimationFrame(animate)
    renderer.render(scene, camera)
  }
  animate()
}

init()
