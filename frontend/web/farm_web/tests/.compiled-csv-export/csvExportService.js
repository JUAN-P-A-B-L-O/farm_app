import api from './api.js'


function sanitizeParams(params) {
  if (!params) {
    return undefined
  }

  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  )
}

function parseFileName(contentDisposition, fallbackFileName) {
  if (!contentDisposition) {
    return fallbackFileName
  }

  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    return decodeURIComponent(utf8Match[1])
  }

  const fallbackMatch = contentDisposition.match(/filename="?([^"]+)"?/i)
  return fallbackMatch?.[1] ?? fallbackFileName
}

export async function downloadCsv(
  endpoint,
  params,
  fallbackFileName = 'export.csv',
) {
  const response = await api.get(endpoint, {
    params: sanitizeParams(params),
    responseType: 'blob',
  })

  const fileName = parseFileName(response.headers['content-disposition'], fallbackFileName)
  const blob = response.data instanceof Blob
    ? response.data
    : new Blob([response.data], { type: 'text/csv;charset=utf-8' })
  const downloadUrl = window.URL.createObjectURL(blob)
  const link = document.createElement('a')

  link.href = downloadUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(downloadUrl)
}
