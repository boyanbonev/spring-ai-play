# Frontend (Vite + React + TypeScript)

The app under `src/main/resources/static/` is a working UI that loads React from `esm.sh` (no local `npm install` required for the Spring Boot JAR to serve a page). This folder is the **Vite** source for local development and optional production builds.

## Prerequisites

- Node.js 20+ and npm on your `PATH`.

## Commands

```bash
cd frontend
npm install
npm run dev
```

`npm run dev` starts Vite on port 5173 and proxies `/chat` and `/api` to `http://localhost:8080`. Run Spring Boot separately.

To replace the contents of `src/main/resources/static/` with a bundled build (hashed assets, no runtime CDN):

```bash
npm run build
```

Then rebuild the JAR (`mvn package`). To hook the build into Maven, set `-Dskip.npm=false` (see `pom.xml`).
