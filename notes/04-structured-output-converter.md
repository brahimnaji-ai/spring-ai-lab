# 🧠 Structured Output Converter (Spring AI)

> **One-line definition** — a `StructuredOutputConverter<T>` appends format instructions to the prompt *before* the LLM call, then parses the raw text response into a Java type *after* — the mechanism behind `ChatClient.entity()`.

---

## 📖 Definition

```java
public interface StructuredOutputConverter<T> extends Converter<String, T>, FormatProvider {
}

public interface FormatProvider {
    String getFormat();
}
```

Two responsibilities in one interface: `FormatProvider.getFormat()` supplies the instruction text injected into the prompt (e.g. *"Your response should be in JSON format matching this schema..."*), and `Converter<String, T>` parses the model's raw text output back into type `T`.

> ⚠️ Not used for [[04-tool-calling|Tool Calling]] — tool calling already returns structured arguments natively, so this converter layer is redundant there.

---

## ⚙️ How It Works

```
Before call:  converter.getFormat() → appended to prompt via {format} placeholder
              ↓
LLM call:     model sees format instructions, generates text trying to match them
              ↓
After call:   converter.convert(rawText) → instance of T
```

The format instructions are spliced in via a `{format}` placeholder in a `PromptTemplate` — same templating mechanism covered in [[03-prompts]]:

```java
StructuredOutputConverter<T> outputConverter = ...;
String template = """
    ... user text input ....
    {format}
    """;
Prompt prompt = new Prompt(
    PromptTemplate.builder()
        .template(template)
        .variables(Map.of(..., "format", outputConverter.getFormat()))
        .build()
        .createMessage()
);
```

**Critical caveat**: this is best-effort. The model is not guaranteed to honor the format — it may misunderstand the prompt or produce malformed output. `BeanOutputConverter.convert()` will throw on bad JSON. Always plan for that failure mode; don't assume `.entity()` is a hard contract.

---

## 🛠️ Available Converters

| Converter | Produces | Format guidance |
|---|---|---|
| `BeanOutputConverter<T>` | a Java class/record, via `JsonMapper` deserialization | JSON Schema (`DRAFT_2020_12`) generated from the class |
| `MapOutputConverter` | `java.util.Map<String, Object>` | RFC8259-compliant JSON |
| `ListOutputConverter` | `java.util.List` | comma-delimited list |
| `AbstractConversionServiceOutputConverter<T>` | custom, via `GenericConversionService` | no default format — you supply it |
| `AbstractMessageOutputConverter<T>` | custom, via `MessageConverter` | no default format — you supply it |

`BeanOutputConverter` is the one you'll reach for almost every time — it's what `.entity(MyRecord.class)` uses internally.

---

## 📍 Where We Find It

- Any time a model's answer needs to become a Java object your code branches on, not just text a human reads.
- Extracting structured facts from free text (dates, names, categorized fields).
- Generating lists or maps from a prompt without writing custom JSON parsing.

---

## 💻 Code Demos

### Bean Output Converter — fluent `ChatClient`

```java
record ActorsFilms(String actor, List<String> movies) {}

ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("Generate the filmography of 5 movies for {actor}.")
                .param("actor", "Tom Hanks"))
    .call()
    .entity(ActorsFilms.class);
```

### Bean Output Converter — low-level `ChatModel`

```java
BeanOutputConverter<ActorsFilms> beanOutputConverter = new BeanOutputConverter<>(ActorsFilms.class);
String format = beanOutputConverter.getFormat();

String template = """
    Generate the filmography of 5 movies for {actor}.
    {format}
    """;

Generation generation = chatModel.call(
    PromptTemplate.builder()
        .template(template)
        .variables(Map.of("actor", "Tom Hanks", "format", format))
        .build()
        .create()
).getResult();

ActorsFilms actorsFilms = beanOutputConverter.convert(generation.getOutput().getText());
```

### Controlling property order

```java
@JsonPropertyOrder({"actor", "movies"})
record ActorsFilms(String actor, List<String> movies) {}
```

### Generic / parameterized types

```java
List<ActorsFilms> actorsFilms = ChatClient.create(chatModel).prompt()
    .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
    .call()
    .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
```

### Map and List converters

```java
Map<String, Object> result = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("Provide me a List of {subject}")
                .param("subject", "an array of numbers from 1 to 9 under the key 'numbers'"))
    .call()
    .entity(new ParameterizedTypeReference<Map<String, Object>>() {});

List<String> flavors = ChatClient.create(chatModel).prompt()
    .user(u -> u.text("List five {subject}").param("subject", "ice cream flavors"))
    .call()
    .entity(new ListOutputConverter(new DefaultConversionService()));
```

---

## 🚀 Native Structured Output

Instead of prompt-based format instructions, some providers accept the JSON schema directly through their own structured-output API — more reliable, no extra prompt text needed.

```java
ActorsFilms actorsFilms = ChatClient.create(chatModel).prompt()
    .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
    .user("Generate the filmography for a random actor.")
    .call()
    .entity(ActorsFilms.class);
```

Or globally:

```java
@Bean
ChatClient chatClient(ChatClient.Builder builder) {
    return builder
        .defaultAdvisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
        .build();
}
```

**Not enabled by default** — support varies by provider (OpenAI GPT-4o+, Anthropic Claude 3.5 Sonnet+, Gemini 1.5 Pro+, Mistral Small+, some Ollama models). The prompt-based default works everywhere; native mode is an opt-in reliability upgrade you test per model.

---

## ⚠️ Known Limitations (real gotchas, not theoretical)

**Ollama reasoning models can return their thinking trace as plain text**, breaking `BeanOutputConverter` with a `StreamReadException`. Fix: switch models (`llama3.1:latest` is stable) or combine native output with schema validation for auto-retry:

```java
ActorFilms actorFilms = chatClient.prompt()
    .user("Generate the filmography for a random actor.")
    .call()
    .entity(ActorFilms.class, spec -> spec
        .useProviderStructuredOutput()
        .validateSchema());
```

**OpenAI rejects top-level array schemas** — requesting `List<T>` with native structured output enabled fails outright. Wrap it in a container record instead:

```java
// fails with OpenAI native structured output:
List<ActorsFilms> films = chatClient.prompt()
    .user("Generate filmographies for Tom Hanks and Bill Murray.")
    .call()
    .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {},
            spec -> spec.useProviderStructuredOutput()); // fails

// fix: wrap in a container
record FilmographyList(List<ActorsFilms> films) {}

FilmographyList result = chatClient.prompt()
    .user("Generate filmographies for Tom Hanks and Bill Murray.")
    .call()
    .entity(FilmographyList.class, spec -> spec.useProviderStructuredOutput());
```

---

## 🎯 Why It Matters — By Path

| Path | Relevance | Why |
|------|-----------|-----|
| 🔐 Ethical Hacking / AI Security | ⭐⭐⭐ | "Best effort" parsing means a malformed or adversarially-crafted model response can throw at runtime — treat `.convert()`/`.entity()` failures as an expected input-validation case, not a "should never happen" bug, especially on any endpoint where model output feeds directly into a downstream system call. |
| 🤖 AI / ML Engineering | ⭐⭐⭐⭐⭐ | This is what turns free text into something your application can actually branch logic on — the bridge between "the model talked" and "my code did something." |
| 💻 Backend Dev | ⭐⭐⭐⭐ | Familiar shape if you've used Jackson deserialization or Spring's `Converter<S,T>` — same interface, new source (an LLM instead of a JSON body). |
| ⚙️ DevOps / Cloud | ⭐⭐ | Native structured output vs. prompt-based is a per-model compatibility matrix you'll want documented if you support multiple providers in production. |

---

## 🔁 Comparison Table

| Approach | Reliability | Prompt overhead | Provider support |
|---|---|---|---|
| Prompt-based (default) | Best-effort — can fail on malformed output | Adds format instructions to every prompt | Works with every model |
| Native structured output | Provider enforces the schema | None — no extra prompt text | Limited to specific models/versions |
| Native + `validateSchema()` | Native reliability + auto-retry on failure | None | Same limited set, plus retry cost |

---

## 🧩 Related Concepts

- [[01-chat-client]] — `.entity()` is the fluent-API entry point for this converter layer
- [[03-prompts]] — the `{format}` placeholder is the same `PromptTemplate` mechanism
- [[04-tool-calling]] — tool calling doesn't need this converter; arguments are already structured

---

## 📌 TL;DR

- `StructuredOutputConverter<T>` = `FormatProvider` (prompt instructions) + `Converter<String,T>` (response parsing).
- `BeanOutputConverter` is the workhorse — generates a JSON Schema from your record/class, parses the response back into it.
- It's best-effort, not guaranteed — always expect and handle conversion failures.
- Native structured output trades prompt-based reliability for provider-level schema enforcement, but isn't universally supported — check the model first.
- Known traps: Ollama reasoning models leaking their thinking trace as plain text; OpenAI refusing top-level array schemas (wrap in a container record).