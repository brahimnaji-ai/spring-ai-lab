# 🧠 Multimodality (Spring AI)

> **One-line definition** — a model's ability to process multiple input types (text, image, audio, video) in the same request; in Spring AI this rides entirely on `UserMessage`'s optional `media` field, no separate API.

---

## 📖 Definition

`UserMessage` has two fields that matter here: `content` (text — what you've used everywhere so far) and `media` (optional, one or more `Media` objects). `MimeType` tells the model what kind of data each `Media` entry is; the underlying data is either a raw `Resource` or a `URI`, depending on what the provider accepts.

```java
var imageResource = new ClassPathResource("/multimodal.test.png");

var userMessage = UserMessage.builder()
    .text("Explain what do you see in this picture?")
    .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
    .build();

ChatResponse response = chatModel.call(new Prompt(userMessage));
```

> ⚠️ **`media` is input-only.** It's valid on `UserMessage` and has no meaning on a system message. `AssistantMessage` (the model's reply) is **text-only** — a multimodal-input model does not give you multimodal output through this API. Generating an image or audio response means calling a dedicated single-modality model (Image API, Audio API) separately.

---

## ⚙️ How It Works

No new abstraction beyond what [[03-prompts]] already covers — `Media` just rides inside the same `Message` → `Prompt` → `ChatModel`/`ChatClient` pipeline used for plain text.

```
UserMessage
 ├── content: String                    (text portion)
 └── media: List<Media>                 (optional — image/audio/video)
       └── Media(MimeType, Resource|URI)
              │
              ▼
      Prompt → ChatModel.call() → provider translates Media into
      its own native multimodal request format
```

Whether `Media`'s data is a raw `Resource` (bytes sent inline) or a `URI` (provider fetches it) depends on the specific model integration — check the per-provider multimodal doc before assuming either works everywhere.

---

## 📍 Where We Find It

- "Describe this image" / "What's in this screenshot" use cases.
- Document understanding where the source is a scanned image or PDF page rather than extracted text.
- Any workflow where visual input replaces or supplements a text description you'd otherwise have to write by hand.

---

## 💻 Code Demo

```java
package com.brahim.springailab.c0X_multimodality;

@RestController
@RequestMapping("/multimodal")
class MultimodalController {

    private final ChatModel chatModel;

    MultimodalController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    // low-level ChatModel
    @PostMapping("/describe")
    String describe() {
        var imageResource = new ClassPathResource("/multimodal.test.png");
        var userMessage = UserMessage.builder()
            .text("Explain what do you see in this picture?")
            .media(new Media(MimeTypeUtils.IMAGE_PNG, imageResource))
            .build();

        return chatModel.call(new Prompt(userMessage)).getResult().getOutput().getText();
    }

    // fluent ChatClient
    @PostMapping("/describe-fluent")
    String describeFluent(ChatClient.Builder builder) {
        return builder.build().prompt()
            .user(u -> u.text("Explain what do you see on this picture?")
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/multimodal.test.png")))
            .call()
            .content();
    }
}
```

---

## 🌐 Provider Support

Not every model accepts every modality — check before building around it:

| Provider | Multimodal support |
|---|---|
| Anthropic Claude | ✅ images |
| AWS Bedrock Converse | ✅ (varies by underlying model) |
| Mistral AI | ✅ Pixtral models |
| Ollama | ✅ LLaVA, BakLLaVA, Llama vision models |
| OpenAI | ✅ GPT-4/GPT-4o and later |
| Google Gemini | ✅ images, and per-model: audio, video, PDF |

For local/offline dev on your setup (16GB RAM, CPU-only), a vision-capable Ollama model is the practical choice over always hitting a paid API for image description testing — same reasoning as the `ollama` profile from [[01-chat-client]].

---

## 🎯 Why It Matters — By Path

| Path | Relevance | Why |
|------|-----------|-----|
| 🔐 Ethical Hacking / AI Security | ⭐⭐⭐ | Image/document input is an underexplored injection surface — text hidden inside an image (steganography-adjacent, or just literal rendered text) can carry instructions a text-only content filter never sees. Don't assume your prompt-injection defenses that scan text also cover what's embedded in an uploaded image. |
| 🤖 AI / ML Engineering | ⭐⭐⭐⭐ | Removes an entire OCR/pre-processing step for a large class of "understand this document/screenshot" tasks. |
| 💻 Backend Dev | ⭐⭐⭐ | Same `Prompt`/`Message` plumbing you already know — this is additive, not a new pipeline to learn. |
| ⚙️ DevOps / Cloud | ⭐⭐ | Image/audio payloads are heavier than text — factor that into request size limits and provider cost, since multimodal calls are typically priced higher than text-only. |

---

## 🔁 Comparison Table

| | Multimodal input (`UserMessage.media`) | Dedicated single-modality model (Image/Audio API) |
|---|---|---|
| Direction | Model reads image/audio, responds with text | Model generates image/audio as output |
| API | Same `ChatModel`/`ChatClient` as text | Separate `ImageModel`/`AudioModel` APIs |
| Use case | "Describe this", "What's wrong in this diagram" | "Generate an image of...", text-to-speech |

---

## 🧩 Related Concepts

- [[01-chat-client]] — `ChatClient.user(...).media(...)` is the fluent entry point
- [[03-prompts]] — `Media` is just another field on the same `UserMessage`/`Prompt` structure
- [[06-rag]] — Vertex AI multimodal embeddings extend this idea to retrieval: embedding images/video alongside text for similarity search

---

## 📌 TL;DR

- Multimodal input = `UserMessage.media` (list of `Media`, each with a `MimeType` + `Resource`/`URI`). No new pipeline — same `Prompt`/`ChatModel`/`ChatClient` as text.
- Input-only: `AssistantMessage` responses are always text, even when the input was an image. Generating non-text output needs a dedicated Image/Audio model.
- Provider support varies (OpenAI GPT-4o+, Claude, Gemini, Ollama vision models, Mistral Pixtral, Bedrock) — verify per-provider before relying on a specific modality.
- Treat embedded content (text-in-image) as a distinct injection surface — your text-based prompt-injection defenses don't automatically cover it.