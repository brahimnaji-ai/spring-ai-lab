### Does .entity() works on LLM level or App level????
> Short answer: both, depending on how you use it.

1. `App level (always)`:
 > .entity() is a Spring AI API.
 It converts the model's response into your Java
 object using Jackson (or another converter).
 This part always happens in your application.
2. `LLM level (optional)`:
 > If you configure .entity(MyClass.class, spec -> spec.useProviderStructuredOutput())
 then Spring AI also tells the LLM provider (e.g., OpenAI, Gemini)
 to produce output that matches your Java class's schema.
 The provider enforces the structure before the
 response reaches your application.
  
#### Conclusion
> So .entity() itself is an `application-level feature`, but it can `leverage LLM-level structured output` when you enable useProviderStructuredOutput().
 
### An optional Consumer<EntityParamSpec> that enables two independent `behaviours`:

1. LLM/Provider-level behavior
>These options affect how the request is sent to the AI provider.

Example:

`spec.useProviderStructuredOutput()`

This tells Spring AI:

> "Ask the provider (Gemini, OpenAI, etc.) to enforce this JSON schema."

The behavior happens before the response reaches your application.

Flow:
```textmate
Your App
│
▼
Spring AI
│
▼
Provider (Gemini/OpenAI) enforces schema
│
▼
Response
```
2. Application-level behavior

> These options affect what Spring AI does after receiving the response.

Example:

`spec.validateSchema()`

This tells Spring AI:

> "After I receive the response, validate it against the expected schema. If it's invalid, try to recover (e.g., retry or self-correct)."

The behavior happens inside your application, after the LLM has responded.

Flow:

```textmate
LLM Response
│
▼
Spring AI validates schema
│
├── Valid → convert to Java object
└── Invalid → recover/retry
```
