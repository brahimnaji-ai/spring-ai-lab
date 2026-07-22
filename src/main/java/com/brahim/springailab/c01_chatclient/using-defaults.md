### Other defaults

At the `ChatClient.Builder` level, you can specify the default prompt configuration.

- `defaultOptions(ChatOptions chatOptions)`: Pass in either portable options defined in the ChatOptions class or model-specific options such as those in OpenAiChatOptions. For more information on model-specific ChatOptions implementations, refer to the JavaDocs.

- `defaultTools(Object…​ tools)`: Register one or more default tools that will be available to every request. Accepts a heterogeneous mix of ToolCallback, ToolCallbackProvider, or POJOs with @Tool annotated methods.

- `defaultToolContext(Map<String, Object> toolContext)`: Set a default context for tool executions.

- `defaultSystem(String text)`, `defaultSystem(Resource text)`, `defaultSystem(Consumer<PromptSystemSpec> systemSpecConsumer)`: These methods let you define the default system text.

- `defaultUser(String text)`, `defaultUser(Resource text)`, `defaultUser(Consumer<UserSpec> userSpecConsumer)`: These methods let you define the user text. The Consumer<UserSpec> allows you to use a lambda to specify the user text and any default parameters.

- `defaultTemplateRenderer(TemplateRenderer templateRenderer)`: Set a default TemplateRenderer for prompt templates.

- `defaultAdvisors(Advisor…​ advisor)`, `defaultAdvisors(List<Advisor> advisors)`: Advisors allow modification of the data used to create the Prompt. The QuestionAnswerAdvisor implementation enables the pattern of Retrieval Augmented Generation by appending the prompt with context information related to the user text.

- `defaultAdvisors(Consumer<AdvisorSpec> advisorSpecConsumer)`: This method allows you to define a Consumer to configure multiple advisors using the AdvisorSpec. Advisors can modify the data used to create the final Prompt. The Consumer<AdvisorSpec> lets you specify a lambda to add advisors, such as QuestionAnswerAdvisor, which supports Retrieval Augmented Generation by appending the prompt with relevant context information based on the user text.

You can override these defaults at runtime using the corresponding methods without the default prefix.

- `options`(ChatOptions.Builder optionsCustomizer)

- `tools`(Object…​ tools)

- `toolContext`(Map<String, Object> toolContext)

- `messages`(Message…​ messages), `messages`(List<Message> messages)

- `system`(String text), system(Resource text), `system`(Consumer<PromptSystemSpec> systemSpecConsumer)

- `user`(String text), user(Resource text), `user`(Consumer<UserSpec> userSpecConsumer)

- `templateRenderer`(TemplateRenderer templateRenderer)

- `advisors`(Advisor…​ advisor), `advisors`(List<Advisor> advisors)

- `advisors`(Consumer<AdvisorSpec> advisorSpecConsumer)