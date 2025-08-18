import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        port: 5173, // 使用默认的5173端口
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,

            }
        }
    },
    build: {
        outDir: '../src/main/resources/static',
        emptyOutDir: true,
        rollupOptions: {
            input: {
                main: fileURLToPath(new URL('./src/main.js', import.meta.url))
            }
        }
    },
    optimizeDeps: {
        include: [
            'vue',
            'vue-router',
            'axios'
        ]
    }
})