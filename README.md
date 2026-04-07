# Spring AI Play

This repository is a small **Spring Boot** application that exposes a **chat API** backed by **Google Vertex AI (Gemini)** via **Spring AI**, plus a **React** web UI for prompts, **streaming** replies, **elapsed time**, and **model selection** from a configured allowlist.

---

## What it does

- **Backend** serves REST endpoints and static files:
  - `POST /chat` — accepts JSON `{ "prompt": "...", "model": "..." }` (`model` optional). Streams the assistant reply as `text/plain` chunks.
  - `GET /api/models` — returns the comma-separated allowlist from `app.chat.allowed-models` in `application.properties` for the UI dropdown.
- **Default model** and Vertex project/region are set in `src/main/resources/application.properties` (`spring.ai.vertex.ai.gemini.*`, `app.chat.allowed-models`).
- **UI** (under `src/main/resources/static/`):
  - **Shipped default:** `index.html` + `assets/chat.js` load **React from esm.sh** so the app runs **without** running `npm install` for the JAR.
  - **Optional:** the `frontend/` folder is a **Vite + React + TypeScript** app. After `npm run build`, output overwrites `static/` with a bundled build (no runtime CDN).

---

## Development — tools to install

| Tool | Role |
|------|------|
| **JDK 17** | Required. The build targets Java 17 (`java.version` in `pom.xml`). Use `java -version` to confirm. |
| **Maven 3.8+** | Builds the backend, runs tests, packages the JAR. Use `mvn -v`. |
| **Node.js + npm** | Needed only if you develop or bundle the Vite UI (`frontend/`). Node **20+** recommended. Not required to compile or run the backend if you rely on the prebuilt static assets and `skip.npm=true` (default). |
| **Google Cloud / Vertex** | To call Gemini, configure **Vertex AI** in `application.properties` and authenticate with your usual GCP method (e.g. `gcloud auth application-default login`, or a service account as used by your environment). |

Optional: **Git**, an IDE, and **curl** or a browser for manual API checks.

---

## Build

From the **repository root** (not only `frontend/`):

```bash
mvn clean package
```

- Compiles Java, runs tests, and builds a runnable JAR under `target/`.
- By default **`skip.npm` is `true`**, so Maven **does not** run `npm install` / `npm run build` in `frontend/`. The UI served from the JAR uses the files already in `src/main/resources/static/`.

To **embed a fresh Vite production build** into `static/` during the Maven build (requires `npm` on your `PATH` and network for dependencies):

```bash
mvn clean package -Dskip.npm=false
```

---

## Run

```bash
mvn spring-boot:run
```

Or run the packaged JAR:

```bash
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

Then open **http://localhost:8080/** in a browser.

Vertex/GCP must be configured and authenticated or chat requests will fail at runtime.

---

## Frontend development (Vite)

Use this when you want hot reload and TypeScript editing of the React app.

1. Start the backend (see **Run** above), e.g. on port **8080**.
2. In another terminal:

```bash
cd frontend
npm install
npm run dev
```

Vite serves the app (default **http://localhost:5173**) and **proxies** `/chat` and `/api` to the Spring Boot server.

3. After changes, you can produce static assets for the JAR:

```bash
npm run build
```

That writes into `../src/main/resources/static/` (see `frontend/vite.config.ts`). Then run **`mvn package`** (with or without `-Dskip.npm=false`, depending on whether you want Maven to rebuild the frontend again).

---

## Configuration notes

- **Models:** default Gemini model and allowlist are in `src/main/resources/application.properties`. Adjust `spring.ai.vertex.ai.gemini.project-id`, `location`, `chat.options.model`, and `app.chat.allowed-models` for your project.
- **Maven npm step:** `pom.xml` uses `exec-maven-plugin` with `NODE_TLS_REJECT_UNAUTHORIZED=0` only for that plugin’s environment; adjust if your security policy forbids it.

---

## Project layout (short)

| Path | Purpose |
|------|---------|
| `src/main/java/` | Spring Boot app, REST controllers, chat wiring |
| `src/main/resources/application.properties` | Vertex AI + app chat settings |
| `src/main/resources/static/` | Web UI served by Spring (default + optional Vite build output) |
| `frontend/` | Vite + React + TypeScript source for local dev and `npm run build` |
