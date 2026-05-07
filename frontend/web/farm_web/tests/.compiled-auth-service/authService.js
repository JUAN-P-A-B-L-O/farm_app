import api from './api.js'
export async function login(email, password) {
  const response = await api.post('/auth/login', {
    email,
    password,
  })

  return response.data
}

export async function registerAccount(data) {
  const response = await api.post('/auth/register', data)
  return response.data
}
