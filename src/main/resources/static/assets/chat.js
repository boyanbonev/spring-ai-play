import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
} from "https://esm.sh/react@18.3.1";
import { createRoot } from "https://esm.sh/react-dom@18.3.1/client";

const h = React.createElement;

function formatElapsed(ms) {
  if (ms < 0) {
    ms = 0;
  }
  const sec = Math.floor(ms / 1000);
  const msec = Math.floor(ms % 1000);
  return `${sec} s ${msec} ms`;
}

/**
 * Static build served from the JAR (no Vite). Backend returns {@code { id, label }} per model; Ollama rows use a
 * {@code (local) } prefix in {@code label} only — we still POST the raw {@code id}.
 */
function App() {
  const [models, setModels] = useState([]);
  const [modelsError, setModelsError] = useState(null);
  const [modelId, setModelId] = useState("");
  const [prompt, setPrompt] = useState("");
  const [output, setOutput] = useState("");
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);
  const [elapsedMs, setElapsedMs] = useState(null);

  const startTimeRef = useRef(null);
  const rafRef = useRef(null);

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
        return res.json();
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
      .catch((e) => {
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

    const body = { prompt: text };
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
      for (;;) {
        const { done, value } = await reader.read();
        if (done) {
          break;
        }
        acc += decoder.decode(value, { stream: true });
        setOutput(acc);
      }
      acc += decoder.decode();
      setOutput(acc);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    } finally {
      stopTimer();
      setBusy(false);
    }
  }

  return h(
    "div",
    { className: "app" },
    h("h1", null, "Chat"),
    h(
      "label",
      { className: "field" },
      h("span", null, "Model"),
      modelsError != null &&
        h("p", { className: "models-err", role: "alert" }, modelsError),
      h(
        "select",
        {
          value: modelId,
          onChange: (e) => setModelId(e.target.value),
          disabled: busy || models.length === 0,
        },
        models.length === 0
          ? h("option", { value: "" }, "No models")
          : models.map((m) =>
              h("option", { key: m.id, value: m.id }, m.label),
            ),
      ),
    ),
    h(
      "label",
      { className: "field" },
      h("span", null, "Prompt"),
      h("textarea", {
        value: prompt,
        onChange: (e) => setPrompt(e.target.value),
        onKeyDown: (e) => {
          if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) {
            e.preventDefault();
            void send();
          }
        },
        placeholder: "Ask something…",
        disabled: busy,
        rows: 6,
      }),
    ),
    h(
      "div",
      { className: "actions" },
      h(
        "button",
        { type: "button", onClick: () => void send(), disabled: busy },
        "Send",
      ),
      elapsedMs != null &&
        h(
          "span",
          { className: "elapsed", "aria-live": "polite" },
          "Elapsed: ",
          formatElapsed(elapsedMs),
        ),
    ),
    error != null &&
      h("p", { className: "err", role: "alert" }, error),
    h("div", { className: "out", "aria-live": "polite" }, output),
  );
}

createRoot(document.getElementById("root")).render(h(App));
