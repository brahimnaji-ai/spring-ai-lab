# 🧠 Advisors (Spring AI)

> **One-line definition** — an `Advisor` is a middleware layer around a `ChatClient` call: it can inspect and mutate the request before it reaches the model, and inspect and mutate the response before it reaches the caller. Memory, RAG, tool calling, and logging are all just advisors on the same chain.

---

## 📖 Definition

Package: `org.springframework.ai.chat.client.advisor.api`.

- `Advisor` — base interface: `getName()` + `getOrder()` (extends Spring's `Ordered`).
- `CallAdvisor` — sync: `adviseCall(ChatClientRequest, CallAdvisorChain)` → `ChatClientResponse`.
- `StreamAdvisor` — reactive: `adviseStream(ChatClientRequest, StreamAdvisorChain)` → `Flux<ChatClientResponse>`.
- `BaseAdvisor` — convenience interface with `before()` / `after()`, for advisors that don't need to loop the chain (non-recursive case).
- `ChatClientRequest` / `ChatClientResponse` — immutable records carrying the `Prompt`/`ChatResponse` plus a shared `adviseContext` map for passing state between advisors.

---

## ⚙️ How It Works — the chain is a stack, not a pipeline

Advisors are ordered by `getOrder()`, lowest value first — but "first" means **outermost wrapper**, not "runs, then done."

```
A.adviseCall() starts   (order 0 — outermost)
  B.adviseCall() starts (order 10)
    ChatModel.call()
  B.adviseCall() finishes  (post-processing)
A.adviseCall() finishes    (post-processing)
```

**Request path**: `Spring AI` creates a `ChatClientRequest` from the user's `Prompt` + an empty `adviseContext` → each advisor runs in ascending order value, each optionally rewriting the request or short-circuiting entirely → the final built-in step sends the request to the `ChatModel`.

**Response path**: the model's response is wrapped into a `ChatClientResponse` → passes back through the same advisors in **reverse** order → the caller gets the final `ChatClientResponse`, unwrapped via `.content()`/`.entity()`/etc.

Practical consequence: set an advisor's order near `Ordered.HIGHEST_PRECEDENCE` (very negative) to guarantee it sees the raw request first and gets the final word on the response — the correct position for anything safety- or policy-related.

---

## 📍 Where We Find It

- Underneath every `ChatClient` feature that "does something extra": chat memory, RAG, tool calling, structured-output validation, content moderation.
- `ToolCallingAdvisor` is **always auto-registered** on a `ChatClient`, whether or not `.tools()` is ever called — it's a permanent occupant of the chain.
- Custom advisors: cross-cutting concerns you'd otherwise duplicate in every controller — request tagging, cost logging, prompt augmentation, output filtering.

---

## 🛠️ Two ways to implement one

**Recursive (`CallAdvisor`/`StreamAdvisor` directly)** — full control, can call `chain.nextCall()` multiple times. Needed for anything loop-like — `ToolCallingAdvisor` calls the chain repeatedly until no more tool calls are pending.

**Non-recursive (`BaseAdvisor`)** — simpler `before()`/`after()` pair, called exactly once each. Correct default for prompt augmentation (`ReReadingAdvisor`'s Re2 pattern) or response observation that doesn't need to alter control flow.

```java
public class BulletPointAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
        String augmented = request.prompt().getUserMessage().getText()
            + "\n\nAnswer using bullet points only.";
        return request.mutate()
            .prompt(request.prompt().augmentUserMessage(augmented))
            .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
        var usage = response.chatResponse().getMetadata().getUsage();
        System.out.printf("BulletPointAdvisor: %d tokens used%n", usage.getTotalTokens());
        return response;
    }

    @Override
    public int getOrder() { return 0; }
}
```

`ChatClientRequest`/`ChatClientResponse` are immutable — every change goes through `.mutate().build()`.

---

## 🎯 Why It Matters — By Path

| Path | Relevance | Why |
|------|-----------|-----|
| 🔐 Ethical Hacking / AI Security | ⭐⭐⭐⭐⭐ | `SafeGuardAdvisor` and any custom moderation advisor only work correctly if placed at the lowest order value — outermost. An advisor with a lower order sitting in front of it can leak unfiltered content before the guard ever runs. Chain *position*, not just presence, is the security boundary. |
| 🤖 AI / ML Engineering | ⭐⭐⭐⭐⭐ | This is the extension point for every "agentic" behavior — memory, retrieval, tool orchestration are all advisors, not special cases. |
| 💻 Backend Dev | ⭐⭐⭐⭐ | Same interceptor/middleware pattern as Servlet `Filter` chains or OkHttp `Interceptor`s — if you've debugged filter ordering bugs before, this is the same class of bug in a new API. |
| ⚙️ DevOps / Cloud | ⭐⭐⭐ | Advisors participate in the observability stack — metrics/traces per advisor execution, useful for spotting which stage of the chain is adding latency. |

---

## 💡 Example / Scenario

A moderation advisor at order `0` and a chat-memory advisor at order `10`: the moderation advisor sees the raw user input first (before memory injects prior context into the prompt) and gets the final response back last — after memory has already recorded whatever the model said. If you flip the order, memory advisor runs first and persists a response the moderation advisor hasn't cleared yet, meaning a blocked response could still get written to conversation history.

---

## 🔁 Comparison Table

| Aspect | `CallAdvisor`/`StreamAdvisor` (recursive) | `BaseAdvisor` (before/after) |
|---|---|---|
| Can call the chain more than once | ✅ yes | ❌ no |
| Use case | Tool-calling loops, retry logic | Prompt augmentation, response logging/validation |
| Complexity | Higher — manage the chain call yourself | Lower — framework calls before/after for you |
| Built-in example | `ToolCallingAdvisor` | `ReReadingAdvisor` |

---

## 🧩 Related Concepts

- [[01-chat-client]] — where advisors are registered (`defaultAdvisors()` / `.advisors()`)
- [[03-chat-memory]] — `MessageChatMemoryAdvisor`, `VectorStoreChatMemoryAdvisor`
- [[04-tool-calling]] — `ToolCallingAdvisor`, the always-registered recursive advisor
- [[06-rag]] — `QuestionAnswerAdvisor`, `RetrievalAugmentationAdvisor`

---

## 📌 TL;DR

- Advisors wrap `ChatClient` calls like middleware — request in, response out, chain in between.
- Lowest `getOrder()` value = first on the request, last on the response = outermost wrapper.
- `BaseAdvisor` (before/after) for simple, non-looping advisors; implement `CallAdvisor`/`StreamAdvisor` directly for recursive ones like tool calling.
- `ChatClientRequest`/`ChatClientResponse` are immutable — mutate via `.mutate().build()`.
- Placement in the chain is a correctness and security property, not just an implementation detail — a safety advisor in the wrong position doesn't protect anything.