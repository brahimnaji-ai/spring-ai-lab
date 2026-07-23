package com.brahim.springailab.c01_chatclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
/** You can do "/api/v1/..."
 * but for simplicity-demo, I won't.*/
@RequestMapping("/chat-client")
@Slf4j
public class ChatClientController {

    private final ChatClient chatClient;
    private final String CHAT_CLIENT_NAME = "ChatClientWithDefaultSystemWithParameters";

    public ChatClientController(@Qualifier(CHAT_CLIENT_NAME) ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** Return type - content: String */
    @GetMapping("/content")
    public String ask1(){
        return chatClient.prompt()
                .user("Hi")
                .call()
                .content();
    }


    /** Return type - ChatResponse: ChatResponse
     *  text + metadata (token usage, finish reason)
     */
    @GetMapping("/chat-response")
    public ChatResponse chatResponse(String input){
        return chatClient.prompt().user(input).call().chatResponse();
        //        var usage = response.getMetadata().getUsage();
        //        return Map.of(
        //                "text",response.getResult().getOutput().getText(),
        //                "promptTokens", usage.getPromptTokens(),
        //                "completionTokens", usage.getCompletionTokens(),
        //                "totalTokensUsage", usage.getTotalTokens()
        //        );
    }


    /** Return type - entity: Deserializes the response into a T instance
     * In Spring AI, ParameterizedTypeReference is used for the same
     * reason as in Spring Web: to tell Spring AI the exact type you
     * want the AI response converted into,
     * especially when the target type is generic.
     */
    record ActorsFilms(String actor, List<String> movies){}


    @GetMapping("/entity")
    public List<ActorsFilms> ask3(String input){
        return chatClient.prompt()
                .user(input)
                .call()
                .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
    }


    @GetMapping("/response-entity")
    public Map<String, Object> responseEntity() {
        var result = chatClient.prompt()
                .user("Generate a random actor and 3 movies they starred in")
                .call()
                .responseEntity(ActorsFilms.class);

        return Map.of(
                "entity", result.entity(),
                "totalTokens", result.response().getMetadata().getUsage().getTotalTokens()
        );
    }


    /**
     * EntityParamSpec
     * All three entity() overloads accept an optional Consumer<EntityParamSpec>
     *     that enables two independent behaviours: Provider-Native Structured Output - Schema Validation with Retry.
     *     DOC:: See entity-param-spec.md.
     */
    @GetMapping("/entity-param-spec")
    public String generate(){
        ActorsFilms films = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorsFilms.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
        /** What I did here is equivalent to
            *      .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
            *      .entity(ActorsFilms.class);*/
        return films.toString();
    }


    @GetMapping("/entity-param-spec2")
    public String generate2(){
        ActorsFilms films = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorsFilms.class, spec->{
                    spec
                            .useProviderStructuredOutput()
                            .validateSchema();
                });
        return films.toString();
    }

    /**
     * In Spring AI, streaming responses let the LLM send tokens
     *  incrementally instead of waiting for the complete response.
     *  This is useful for chat applications where you want the user
     *  to see the answer appear in real time.
     */

    @GetMapping("stream")
    public Flux<String> stream(){
        return chatClient
                .prompt()
                .user("Tell me a joke.")
                .stream()
                .content();
    }

    /**
     * fetched from: {@link <a href="https://docs.spring.io/spring-ai/reference/api/chatclient.html#_streaming_responses">...</a>}
     * In the future, we will offer a convenience method that will let you return a Java entity
     * with the reactive stream() method. In the meantime, you should use the Structured Output
     * Converter to convert the aggregated response explicitly as shown below. This also demonstrates
     * the use of parameters in the fluent API that will be discussed in more detail in a later
     * section of the documentation.
     * This example demonstrates how to combine streaming with structured output.
     */

    @GetMapping("stream-converter")
    public String stream2(){
        // Creating an object responsible for converting the LLM's text output into Java objects.
        var converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<ActorsFilms>>() {});
        Flux<String> flux = this.chatClient.prompt()
                .user(u-> u.text("""
                        Generate the filmography for a random actor.
                        {format}
                        """)
                        .param("format", converter.getFormat())
                ).stream()
                .content();

        /**
         * Turns: Flux<String> --into--> Mono<List<String>>
         *     - .collectList(): "Wait until all chunks have been emitted, then put them into a List<String>."
         *     - .block(): collectList() returns a Mono<List<String>>, which is still asynchronous.
         *      block() waits until the stream is completely finished and gives you the actual List<String>.
         *     - stream(): To use Java Stream operations like collect(), you first convert the list into a stream.
         *     - .collect(Collectors.joining()): This concatenates all the strings into one big string.
         */


        String content =  flux.collectList().block().stream().collect(Collectors.joining()); // return: big json string
        List<ActorsFilms> actorsFilms = converter.convert(content);
        return actorsFilms.toString();
    }



    @GetMapping("/prompt-template-1")
    public String prompt(@RequestParam String language){
        return  chatClient.prompt()
                .user(u -> u
                        .text("Tell me the core of {language}")
                        .param("language", language)
                )
                .call()
                .content();
    }

    @GetMapping("/prompt-template-2")
    public String prompt2(@RequestParam String concept){
        PromptTemplate template = new PromptTemplate("""
                Explain {concept}
                """);
        String prompt = template.render(Map.of("concept", concept));
        return  chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    @GetMapping("/prompt-template-3")
    public String prompt3(@RequestParam String concept){
        PromptTemplate template = new PromptTemplate("""
                Explain {concept}
                """);
        Prompt prompt = template.create(Map.of("concept", concept));
        return  chatClient.prompt()
                .user(prompt.toString())
                .call()
                .content();
    }

    /**
     * Return types of call:  content- ChatResponse - ChatClientResponse -
     * entity :
             * entity(ParameterizedTypeReference<T> type): used to return a Collection of entity types.
             * entity(Class<T> type): used to return a specific entity type.
             * entity(StructuredOutputConverter<T> structuredOutputConverter): used to specify an instance of a StructuredOutputConverter to convert a String to an entity type.
             * entity(ParameterizedTypeReference<T> type, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
             * entity(Class<T> type, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
             * entity(StructuredOutputConverter<T> converter, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
     * - responseEntity:
             * responseEntity(Class<T> type): used to return a ResponseEntity containing both the complete ChatResponse object and a specific entity type.
             * responseEntity(Class<T> type, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
             * responseEntity(ParameterizedTypeReference<T> type): used to return a ResponseEntity containing both the complete ChatResponse object and a Collection of entity types.
             * responseEntity(ParameterizedTypeReference<T> type, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
             * responseEntity(StructuredOutputConverter<T> structuredOutputConverter): used to return a ResponseEntity containing both the complete ChatResponse object and an entity converted using a specified StructuredOutputConverter.
             * responseEntity(StructuredOutputConverter<T> converter, Consumer<EntityParamSpec> spec): like above, with optional EntityParamSpec configuration.
     * */

    /**
     * stream() return values
     * After specifying the stream() method on ChatClient, there are a few options for the response type:
         * Flux<String> content(): Returns a Flux of the string being generated by the AI model.
         * Flux<ChatResponse> chatResponse(): Returns a Flux of the ChatResponse object, which contains additional metadata about the response.
         * Flux<ChatClientResponse> chatClientResponse(): returns a Flux of the ChatClientResponse object that contains the ChatResponse object and the ChatClient execution context, giving you access to additional data used during the execution of advisors (e.g. the relevant documents retrieved in a RAG flow).
     */

    /** See Controller 2 ...*/


    // ===================================================================

    /**
     * What is Message Metadata?
     * Normally, when you send a prompt to an LLM, you only send the message itself.
     * User: "Summarize this document."
     * Sometimes your application also wants to attach extra information about that message.
     * That information is not the actual prompt.
     * It's simply metadata (data about the message).
     * Usecase: imagine you're in your company and  want some observability,
     * so you can use Metadata, to keep trace of your users.
     * We can add metadata on 3 levels: user - system - chatclientBuilder.
     */
    @GetMapping("/metadata-user")
    public String metadata(){
        return chatClient.prompt()
                // I get access to the message builder.
                .user(u->u
                        .text("Hi")
                        .metadata(Map.of(
                                "userId", Math.round(Math.random()),
                                "requestId", UUID.randomUUID().toString()
                        ))
                ).call()
                .content();
    }

    @GetMapping("/metadata-system")
    public String metadata2(){
        Map<String, Object> metadata = Map.of(
                "version", "1.0",
                "model", ""
        );
        return chatClient.prompt()
                .system(s->s
                        .text("You are helpful assistant")
                        .metadata(metadata)
                )
                .user("What is the best city in Morocco?")
                .call()
                .content();
    }


    /**
     * Metadata Validation
     * The ChatClient validates metadata to ensure data integrity:
         * Metadata keys cannot be null or empty
         * Metadata values cannot be null
         * When passing a Map, neither keys nor values can contain null elements
     * */
    @GetMapping("/metadata-validation")
    public String metadataValidation(){
        // This will throw an IllegalArgumentException
        String response1 = chatClient.prompt()
                .user(u->u
                        .text("Hello")
                        .metadata(null, "value")
                )
                .call().content();

        // This will throw an IllegalArgumentException
        String response2 = chatClient.prompt()
                .user(u->u
                        .text("Hello")
                        .metadata("key", null)
                )
                .call().content();
        return response1; // invoke return response2; to see the exception
    }


    /**
     * Accessing Metadata
     * The metadata is included in the generated UserMessage and SystemMessage
     * objects and can be accessed through the message’s getMetadata() method.
     * This is particularly useful when processing messages in advisors or
     * when examining the conversation history.
     * */
    // =========================================================================================
    /**
     * Using Defaults:
         * Creating a ChatClient with a default system text in an @Configuration
         * class simplifies runtime code. By setting defaults, you only need to
         * specify the user text when calling ChatClient, eliminating the need
         * to set a system text for each request in your runtime code path.
     */
    @GetMapping("/default-system")
    public Map<String, String> completion(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message) {
        return Map.of(
                "completion", Objects.requireNonNull(chatClient.prompt().user(message).call().content())
        );
    }

    @GetMapping("/default-system-with-parameters")
    public Map<String, String> completion2(@RequestParam(value = "message", defaultValue = "Tell me a joke") String message, String voice) {
        var content = chatClient.prompt()
                .system(promptSystemSpec -> promptSystemSpec.param("voice", voice))
                .user(message)
                .call()
                .content();
        return Map.of(
                "completion", Objects.requireNonNull(content)
        );
    }
    /**
     * usig-defaults:
     *     DOC:: See using-defaults.md to use other defaults.
     */

    // =========================================================

    /**
     * You can create a new ChatClient (or ChatClientRequestSpec) with settings replicated from an existing one using the mutate() method:
         * mutate() does NOT modify the existing ChatClient.
         * It creates a new ChatClient.Builder preconfigured with the current
         * client's settings (model, advisors, default options, tools, etc.).
         * You can then override only the settings you want and build a new
         * ChatClient, leaving the original instance unchanged.
     */


}
