const KEYPAIR_KEY = 'verification-keypair'

export function saveKeyPair(keypair) {
  localStorage.setItem(KEYPAIR_KEY, JSON.stringify(keypair))
}

export function loadKeyPair() {
  const raw = localStorage.getItem(KEYPAIR_KEY)
  return raw ? JSON.parse(raw) : null
}

export function clearKeyPair() {
  localStorage.removeItem(KEYPAIR_KEY)
}
