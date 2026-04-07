import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: "../src/main/resources/static",
    emptyOutDir: true,
  },
  base: "/",
  server: {
    port: 5173,
    proxy: {
      "/chat": { target: "http://localhost:8080", changeOrigin: true },
      "/api": { target: "http://localhost:8080", changeOrigin: true },
    },
  },
});
