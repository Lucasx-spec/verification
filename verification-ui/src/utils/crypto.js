import { sm2, sm3 } from 'sm-crypto'
import http from '../api/http'

const DEFAULT_CHUNK_SIZE = 1024 * 1024

export function generateKeyPairHex() {
  const keypair = sm2.generateKeyPairHex()
  return {
    publicKey: keypair.publicKey,
    privateKey: keypair.privateKey,
  }
}

async function loadChunkSize() {
  try {
    const response = await http.get('/system/settings')
    const sizeKb = Number(response?.data?.sm3ChunkSizeKb)
    if (Number.isFinite(sizeKb) && sizeKb > 0) {
      return sizeKb * 1024
    }
  } catch {
    // ignore and use default chunk size
  }
  return DEFAULT_CHUNK_SIZE
}

function readChunkAsArrayBuffer(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(reader.result)
    reader.onerror = () => reject(reader.error || new Error('分块读取文件失败'))
    reader.readAsArrayBuffer(blob)
  })
}

function finalizeSm3Hasher(hasher) {
  if (typeof hasher.sum === 'function') {
    return hasher.sum()
  }
  if (typeof hasher.digest === 'function') {
    return hasher.digest()
  }
  if (typeof hasher.hex === 'function') {
    return hasher.hex()
  }
  if (typeof hasher.finish === 'function') {
    return hasher.finish()
  }
  throw new Error('当前 SM3 实现不支持增量摘要结果输出')
}

export async function digestFileHexByChunk(file, chunkSize) {
  const resolvedChunkSize = chunkSize || await loadChunkSize()
  const totalSize = file.size || 0
  const supportsIncremental = typeof sm3.create === 'function'
  const hasher = supportsIncremental ? sm3.create() : null
  const fallbackChunks = supportsIncremental ? null : []

  let offset = 0
  while (offset < totalSize) {
    const chunk = file.slice(offset, offset + resolvedChunkSize)
    const buffer = await readChunkAsArrayBuffer(chunk)
    const chunkBytes = Array.from(new Uint8Array(buffer))

    if (hasher && typeof hasher.update === 'function') {
      hasher.update(chunkBytes)
    } else {
      fallbackChunks.push(chunkBytes)
    }

    offset += resolvedChunkSize
  }

  if (hasher && typeof hasher.update === 'function') {
    return finalizeSm3Hasher(hasher)
  }

  const mergedBytes = []
  for (const chunkBytes of fallbackChunks) {
    for (const byte of chunkBytes) {
      mergedBytes.push(byte)
    }
  }
  return sm3(mergedBytes)
}

function hexToByteArray(hex) {
  const normalized = hex.length % 2 === 0 ? hex : `0${hex}`
  const bytes = []
  for (let index = 0; index < normalized.length; index += 2) {
    bytes.push(parseInt(normalized.slice(index, index + 2), 16))
  }
  return bytes
}

export function signDigestHex(privateKey, publicKey, digestHex) {
  const digestBytes = hexToByteArray(digestHex)
  return sm2.doSignature(digestBytes, privateKey, {
    publicKey,
    hash: true,
    der: false,
  })
}
