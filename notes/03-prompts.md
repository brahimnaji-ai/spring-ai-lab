# 🧠 Prompts (Spring AI)

> **One-line definition** — a `Prompt` is an ordered `List<Message>` + `ChatOptions`; `PromptTemplate` is the tool that renders a template string (and its placeholders) into that `Prompt`/`Message`/`String`.

---

## 📖 Definition

Spring AI's own analogy: prompt handling is the JDBC-equivalent layer — `ChatModel` mirrors the core JDBC library, `ChatClient` mirrors `JdbcClient` built on top with `Advisor`-powered extras. `PromptTemplate` is closer to a JSP/Thymeleaf view than to a SQL statement, but the "placeholders filled at render time" idea is the same.

```java
public class Prompt implements ModelRequest<List<Message>> {
    private final List<Message> messages;
    private ChatOptions chatOptions;
}
```

```java
public interface Message extends Content {
    MessageType getMessageType();
}

public enum MessageType {
    USER("user"), ASSISTANT("assistant"), SYSTEM("system"), TOOL("tool");
}
```

> ⚠️ The official Prompts reference page's `Content` interface snippet shows `getContent()`. The ChatModel/ChatClient pages (and the current API) use `.getText()` — that snippet is stale relative to the rest of the docs. Use `getText()`, verify via IDE autocomplete if in doubt.

---

## ⚙️ How It Works

`PromptTemplate` renders through a `TemplateRenderer` — a `BiFunction<String, Map<String,Object>, String>`. The default, `StTemplateRenderer`, is built on the StringTemplate engine and uses `{}` as the placeholder delimiter.

**This means literal `{` `}` in your prompt text — JSON examples, code snippets, regex — get parsed as template syntax whether you intended it or not.** Two fixes:
1. Swap delimiters: `StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build()`
2. Skip templating entirely: `new NoOpTemplateRenderer()` when the string is already final.

**Convenience accessors on `Prompt`:**

| Method | Returns |
|---|---|
| `getUserMessage()` | the **last** user message |
| `getSystemMessage()` | the **first** system message |
| `getUserMessages()` / `getSystemMessages()` | all of them, in order |
| `getLastUserOrToolResponseMessage()` | for conversation-continuity logic |

---

## 📍 Where We Find It

- Every `ChatClient.prompt().user(...)`/`.system(...)` call — building a `PromptTemplate` under the hood.
- Few-shot prompting, system-role instructions, RAG context injection (the retrieved documents get spliced into a template).
- Externalized prompt files (`.st` under `src/main/resources`), loaded via `Resource`, for prompts that need to be editable without a redeploy.

---

## 🛠️ What We Use It For

```java
// inline template
PromptTemplate template = new PromptTemplate("Tell me a {adjective} joke about {topic}");
Prompt prompt = template.create(Map.of("adjective", "dark", "topic", "backend engineering"));

// system role + user role combined
SystemPromptTemplate systemTemplate = new SystemPromptTemplate("You are {name}, reply as a {voice}.");
Message systemMessage = systemTemplate.createMessage(Map.of("name", "Ada", "voice", "pirate"));
Prompt combined = new Prompt(List.of(systemMessage, new UserMessage("Explain recursion")));

// external file
@Value("classpath:/prompts/system-message.st")
Resource systemResource;
SystemPromptTemplate fromFile = new SystemPromptTemplate(systemResource);
```

---

## 🎯 Why It Matters — By Path

| Path | Relevance | Why |
|------|-----------|-----|
| 🔐 Ethical Hacking / AI Security | ⭐⭐⭐⭐ | Any user-supplied value dropped into a `PromptTemplate` param is untrusted input reaching the model — this is the injection surface. Treat prompt construction with the same discipline as SQL parameter binding: never string-concatenate raw user text into a system-role instruction meant to constrain behavior. |
| 🤖 AI / ML Engineering | ⭐⭐⭐⭐⭐ | Prompt structure (system vs. user role separation, few-shot examples, output-format instructions) is the single highest-leverage lever on output quality — cheaper to iterate on than swapping models. |
| 💻 Backend Dev | ⭐⭐⭐⭐ | Same templating mental model as JSP/Thymeleaf, with the same footgun: unescaped user input inside a template placeholder. |
| ⚙️ DevOps / Cloud | ⭐⭐⭐ | Externalized `.st` files under `resources/prompts/` are versionable and swappable per environment — same idea as externalized config. |

---

## 💡 Example / Scenario

Building the ONDA internal assistant: a support-ticket description gets dropped into a `{ticketText}` placeholder inside a summarization prompt. If a ticket happens to contain literal braces — say, a pasted JSON payload from a monitoring alert — `StTemplateRenderer` tries to parse that JSON as more template syntax and the render fails or corrupts the prompt. This is exactly the class of bug Demo 5 preempts: know in advance whether the *content* you're templating can contain `{}`, and pick `NoOpTemplateRenderer` or custom delimiters up front rather than debugging a cryptic StringTemplate error in production.

---

## 🔁 Comparison Table

| Approach | When to use |
|---|---|
| `new PromptTemplate("...")` + `.create(Map)` | one-off inline prompt, simple placeholders |
| `SystemPromptTemplate` | specifically building the system-role message |
| `Resource`-backed template | prompt text that should be editable without recompiling |
| `NoOpTemplateRenderer` | prompt text already fully formed, or contains literal `{}` you don't want parsed |
| `ChatClient.user(u -> u.text(...).param(...))` | same mechanism, fluent-API entry point — use this by default |

---

## 🧩 Related Concepts

- [[01-chat-client]] — `.user()`/`.system()` builders wrap `PromptTemplate` internally
- [[02-advisors]] — `BulletPointAdvisor`'s `before()` mutates the `Prompt`'s user message the same way a template would
- [[04-structured-output]] — output-format instructions are a `PromptTemplate` concern too (schema-as-instructions)
- [[10-rag]] — retrieved documents get spliced into the prompt via templating before reaching the model

---

## 📌 TL;DR

- `Prompt` = `List<Message>` + `ChatOptions`. `PromptTemplate` renders placeholders into one.
- Default renderer (`StTemplateRenderer`) uses `{}` — literal braces in your content (JSON, code) will break rendering unless you change delimiters or use `NoOpTemplateRenderer`.
- `getUserMessage()` = last user message, `getSystemMessage()` = first system message — not "the only one."
- Externalize prompts to `.st` files via `Resource` for anything you'll want to tune without redeploying.
- Prompt-templated user input is untrusted input reaching the model — same discipline as parameterized SQL.