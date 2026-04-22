import { defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

//https://vite.dev/config/
// per eseguirlo in locale anzichè in container
//target: "http://localhost:8080",
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    watch: {
      usePolling: true
    },
    proxy: {
      "/api": {
        target: "http://backend:8080",
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, "")
      }
    }
  }
})



// per utilizzare fil .env distinti per fase di sviluppo e pubblicazione
// import { defineConfig, loadEnv} from 'vite'
// import react from '@vitejs/plugin-react'
// export default defineConfig(({ mode }) => {
//   const env = loadEnv(mode, process.cwd(), '')

//   return {
//     plugins: [react()],
//     server: {
//       proxy: {
//         "/api": {
//           target: env.VITE_API_URL,
//           changeOrigin: true,
//           rewrite: (path) => path.replace(/^\/api/, "")
//         }
//       }
//     }
//   }
// })