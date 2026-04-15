import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    proxy: {
      '/Utenti': {
        target: 'http://host.docker.internal:8080',
        changeOrigin: true
      },
      // ECCO IL PEZZO MANCANTE:
      '/Eventi': {
        target: 'http://host.docker.internal:8080',
        changeOrigin: true
      },
      '/password-reset': {
        target: 'http://host.docker.internal:8080',
        changeOrigin: true
      }
    }
  }
})