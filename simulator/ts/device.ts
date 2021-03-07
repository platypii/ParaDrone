import { GeoPointV } from "./dtypes"

export function sendToDevice(host: string, loc: GeoPointV) {
  const xhr = new XMLHttpRequest()
  const url = getUrl(host, loc as unknown as {[key: string]: number})
  xhr.open("GET", url)
  xhr.send()
}

function getUrl(host: string, obj: {[key: string]: number}): string {
  const params = Object.keys(obj).map((key) => key + "=" + obj[key]).join("&")
  return `http://${host}/msg?${params}`
}
