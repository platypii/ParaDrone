import { GeoPointV } from "./dtypes"

/**
 * Send a simulated location to a real device for testing
 */
export function sendToDevice(host: string, loc: GeoPointV) {
  const xhr = new XMLHttpRequest()
  const url = getUrl(host, loc as unknown as {[key: string]: number})
  xhr.addEventListener("error", () => {
    document.getElementById("device-ip")!.style.border = "1px solid #d11"
  })
  xhr.open("GET", url)
  xhr.send()
}

/**
 * Construct a url with query string parameters
 */
function getUrl(host: string, obj: {[key: string]: number}): string {
  const params = Object.keys(obj).map((key) => key + "=" + obj[key]).join("&")
  return `http://${host}/msg?${params}`
}
