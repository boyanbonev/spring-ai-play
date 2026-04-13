import { useCallback, useEffect, useRef, useState } from "react";
import "./App.css";

/** Matches backend {@code ModelInfo} records from {@code GET /api/models} ({@code id} + {@code label}). */
interface ModelOption {
  id: string;
  label: string;
}

function formatElapsed(ms: number): string {
  if (ms < 0) {
    ms = 0;
  }
  const sec = Math.floor(ms / 1000);
  const msec = Math.floor(ms % 1000);
  return `${sec} s ${msec} ms`;
}

export default function App() {
  const [models, setModels] = useState<ModelOption[]>([]);
  const [modelsError, setModelsError] = useState<string | null>(null);
  /** Selected model {@code id} (raw tag), sent in {@code POST /chat}; not the display {@code label}. */
  const [modelId, setModelId] = useState("");
  const [prompt, setPrompt] = useState("");
  const [output, setOutput] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [elapsedMs, setElapsedMs] = useState<number | null>(null);

  const startTimeRef = useRef<number | null>(null);
  const rafRef = useRef<number | null>(null);

  const stopTimer = useCallback(() => {
    if (rafRef.current != null) {
      cancelAnimationFrame(rafRef.current);
      rafRef.current = null;
    }
    if (startTimeRef.current != null) {
      setElapsedMs(performance.now() - startTimeRef.current);
      startTimeRef.current = null;
    }
  }, []);

  const tickElapsed = useCallback(() => {
    if (startTimeRef.current == null) {
      return;
    }
    setElapsedMs(performance.now() - startTimeRef.current);
    rafRef.current = requestAnimationFrame(tickElapsed);
  }, []);

  useEffect(() => {
    let cancelled = false;
    fetch("/api/models")
      .then((res) => {
        if (!res.ok) {
          throw new Error(`Failed to load models (${res.status})`);
        }
        return res.json() as Promise<ModelOption[]>;
      })
      .then((list) => {
        if (cancelled) {
          return;
        }
        setModels(list);
        setModelsError(null);
        if (list.length > 0) {
          setModelId((prev) =>
            prev && list.some((m) => m.id === prev) ? prev : list[0].id,
          );
        }
      })
      .catch((e: unknown) => {
        if (!cancelled) {
          setModelsError(e instanceof Error ? e.message : String(e));
        }
      });
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    return () => {
      if (rafRef.current != null) {
        cancelAnimationFrame(rafRef.current);
      }
    };
  }, []);

  async function send() {
    const text = prompt.trim();
    if (!text) {
      setError("Enter a prompt.");
      return;
    }
    setError(null);
    setBusy(true);
    setOutput("");
    startTimeRef.current = performance.now();
    setElapsedMs(0);
    rafRef.current = requestAnimationFrame(tickElapsed);

    const body: { prompt: string; model?: string } = { prompt: text };
    if (modelId) {
      body.model = modelId;
    }

    try {
      const res = await fetch("/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });
      if (!res.ok) {
        setError(
          res.status === 400
            ? "Bad request (empty prompt or invalid model)."
            : `Request failed: ${res.status}`,
        );
        return;
      }
      if (!res.body) {
        setError("No response body.");
        return;
      }
      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let acc = "";
      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          break;
        }
        acc += decoder.decode(value, { stream: true });
        setOutput(acc);
      }
      acc += decoder.decode();
      setOutput(acc);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      stopTimer();
      setBusy(false);
    }
  }

  return (
    <div className="app">
      <h1>Chat</h1>

      <label className="field">
        <span>Model</span>
        {modelsError != null && (
          <p className="models-err" role="alert">
            {modelsError}
          </p>
        )}
        <select
          value={modelId}
          onChange={(e) => setModelId(e.target.value)}
          disabled={busy || models.length === 0}
        >
          {models.length === 0 ? (
            <option value="">No models</option>
          ) : (
            models.map((m) => (
              <option key={m.id} value={m.id}>
                {m.label}
              </option>
            ))
          )}
        </select>
      </label>

      <label className="field">
        <span>Prompt</span>
        <textarea
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) {
              e.preventDefault();
              void send();
            }
          }}
          placeholder="Ask something…"
          disabled={busy}
          rows={6}
        />
      </label>

      <div className="actions">
        <button type="button" onClick={() => void send()} disabled={busy}>
          Send
        </button>
        {elapsedMs != null && (
          <span className="elapsed" aria-live="polite">
            Elapsed: {formatElapsed(elapsedMs)}
          </span>
        )}
      </div>

      {error != null && (
        <p className="err" role="alert">
          {error}
        </p>
      )}

      <div className="out" aria-live="polite">
        {output}
      </div>
    </div>
  );
}
