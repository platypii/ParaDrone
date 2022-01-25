import { GeoPointV } from "./dtypes"
import { LandingZone } from "./geo/landingzone"
import { Paraglider } from "./paraglider"

const deviceEnabled = document.getElementById("device-enabled") as HTMLInputElement
const deviceIp = document.getElementById("device-ip") as HTMLInputElement

export function init(para: Paraglider, lz: LandingZone) {
  // Send landing zone to device on enable
  deviceEnabled.addEventListener("change", () => {
    deviceIp.classList.remove("has-error")
    if (deviceEnabled.checked && deviceIp.value) {
      sendLandingZone(lz)
    }
  })
  // Send location updates to device
  para.onLocationUpdate(() => {
    if (deviceEnabled.checked && deviceIp.value && para.loc) {
      sendLocation(para.loc)
    }
  })
}

/**
 * Send a simulated location to a real device for testing
 */
function sendLocation(loc: GeoPointV) {
  sendToDevice(deviceIp.value, "msg", {
    lat: loc.lat.toFixed(7),
    lng: loc.lng.toFixed(7),
    alt: loc.alt,
    vE: loc.vE.toFixed(3),
    vN: loc.vN.toFixed(3),
    climb: loc.climb
  })
}

/**
 * Set landing zone on a real device for testing
 */
function sendLandingZone(lz: LandingZone) {
  sendToDevice(deviceIp.value, "lz", {
    lat: lz.destination.lat,
    lng: lz.destination.lng,
    alt: lz.destination.alt,
    dir: lz.landingDirection.toFixed(4)
  })
}

/**
 * Send a message to the device as an http GET
 */
function sendToDevice(host: string, path: string, obj: {[key: string]: number | string}) {
  // Construct url from query string parameters
  const params = Object.keys(obj).map((key) => key + "=" + obj[key]).join("&")
  const url = `http://${host}/${path}?${params}`
  const xhr = new XMLHttpRequest()
  xhr.addEventListener("error", () => {
    deviceIp.classList.add("has-error")
  })
  xhr.open("GET", url)
  xhr.send()
}
