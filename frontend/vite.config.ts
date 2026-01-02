import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '^/actuator/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '^/api/.*': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '^/swagger-ui.*': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      '^/api-docs.*': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});

