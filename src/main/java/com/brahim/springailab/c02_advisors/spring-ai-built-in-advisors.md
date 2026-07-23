## Spring AI Built-in Advisors
Spring AI framework provides several built-in advisors to enhance your AI interactions. Here’s an overview of the available advisors:

### Chat Memory Advisors
These advisors manage conversation history in a chat memory store:

- `MessageChatMemoryAdvisor`

```textmate
Retrieves memory and adds it as a collection of messages to the prompt.
This approach maintains the structure of the conversation history.
Note, not all AI Models support this approach.
```

- `VectorStoreChatMemoryAdvisor`
```textmate
Retrieves memory from a VectorStore and adds it into the prompt’s system text.
This advisor is useful for efficiently searching and retrieving relevant information
 from large datasets.

```

### Question Answering Advisor

- `QuestionAnswerAdvisor`
```textmate
This advisor uses a vector store to provide question-answering capabilities,
implementing the Naive RAG (Retrieval-Augmented Generation) pattern.
```

- `RetrievalAugmentationAdvisor`
```textmate
Advisor that implements common Retrieval Augmented Generation (RAG)
flows using the building blocks defined in the org.springframework.ai.rag
package and following the Modular RAG Architecture.
```

### Reasoning Advisor

- `ReReadingAdvisor`
```textmate
Implements a re-reading strategy for LLM reasoning, dubbed RE2, to enhance understanding
in the input phase. Based on the article: Re-Reading Improves Reasoning in LLMs.

```

### Tool Calling Advisor
- `ToolCallingAdvisor`
```textmate
Handles the tool calling loop as part of the advisor chain.
This advisor is always auto-registered by ChatClient (unless explicitly disabled),
so tools injected at runtime by another advisor are supported even when no static
tools are configured. It executes tool calls requested by the model and sends the
results back, iterating until no more tool calls are needed. Implements ToolAdvisor,
which is a marker interface that prevents a second ToolCallingAdvisor from being auto-registered.

```

For full documentation, see Recursive Advisors - ToolCallingAdvisor and ChatClient - Tool Calling.

### Content Safety Advisor
- `SafeGuardAdvisor`

> A simple advisor designed to prevent the model from generating harmful or inappropriate content.