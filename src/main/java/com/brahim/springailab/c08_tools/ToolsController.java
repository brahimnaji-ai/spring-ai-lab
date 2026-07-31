package com.brahim.springailab.c08_tools;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tools")
public class ToolsController {

    private final ChatClient statelessChatClient; // without tools
    private final ChatClient chatClient; // with default tools
    @Autowired
    ToolCallback toolCallback;
    /**
     * <h4>Warning</h4></br>
     Default tools are shared across all the chat requests performed by all the ChatClient instances
     built from the same ChatClient.Builder. They are useful for tools that are commonly used across
     different chat requests, but they can also be dangerous if not used carefully, risking to make
     them available when they shouldn’t.
     * */

    public ToolsController(ChatClient.Builder builder, DateTimeTools dateTimeTools) {
        this.statelessChatClient = builder
                .clone() // be careful you should clone here, else you will get same instance as the chat client.
                .build();
        this.chatClient = builder
                .defaultSystem("""
                        If answering requires a tool call, make it and return only
                        the tool result, with no extra text.
                        """)
                .defaultTools(dateTimeTools)
                .build();
    }

    /**
     * Tool calling (also known as function calling) is a common pattern in AI applications allowing
     * a model to interact with a set of APIs, or tools, augmenting its capabilities.</br>
     * Tools are mainly used for:</br>
     * </br>
     * - <B>Information Retrieval:</B> Tools in this category can be used to retrieve information from external
     * sources, such as a database, a web service, a file system, or a web search engine.
     * The goal is to augment the knowledge of the model, allowing it to answer questions that it would
     * not be able to answer otherwise. As such, they can be used in Retrieval Augmented Generation (RAG)
     * scenarios. For example, a tool can be used to retrieve the current weather for a given location,
     * to retrieve the latest news articles, or to query a database for a specific record.</br>
     * </br>
     * - <b>Taking Action:</b> Tools in this category can be used to take action in a software system, such as sending
     * an email, creating a new record in a database, submitting a form, or triggering a workflow. The goal
     * is to automate tasks that would otherwise require human intervention or explicit programming.
     * For example, a tool can be used to book a flight for a customer interacting with a chatbot, to fill
     * out a form on a web page, or to implement a Java class based on an automated test (TDD) in a code
     * generation scenario.</br>
     *
     * */

    // To see difference, I'll make up 2 endpoints to fetch time.
    // first- uses stateless Chat client
    // second- uses  Chat client with default tools

    @GetMapping("/ask-time")
    public String askTime() {
        return statelessChatClient.prompt("What time is it now?")
                .call()
                .content();
    }


    @GetMapping("/info-retrieval")
    public String informationRetrieval() {
        return chatClient.prompt("What time is it now? ")
                .call()
                .content();
    }

    /**
     * Next, let’s make both tools available to the model.
     * We’ll provide the tools to the model by passing an instance of DateTimeTools via the tools() method.
     * When we ask to set up an alarm 10 minutes from now, the model will first need to know the current date
     * and time. Then, it will use the current date and time to calculate the alarm time. Finally, it will
     * use the alarm tool to set up the alarm. Internally, the ChatClient will handle any tool call request
     * from the model and send back to it any tool call execution result, so that the model can generate
     * the final response.
     * Thus, it will combine the two existing tools to create the result.
     * */


    @GetMapping("/take-action")
    public String takeAction() {
        return chatClient.prompt("Can you set an alarm 10 minutes from now?")
                .call()
                .content();
    }

    /**
     * Adding Tools to ChatModel:
     * When using the declarative specification approach, you can pass the tool class instance
     * to the toolCallbacks() method of the ToolCallingChatOptions you use to call a ChatModel.
     * Such tools will only be available for the specific chat request they are added to.
     * */

    // ========================================================================================
    /**
     * Functions as Tools:</br>
     * Spring AI provides built-in support for specifying tools from functions,
     * either programmatically using the low-level FunctionToolCallback implementation
     * or dynamically as @Bean(s) resolved at runtime.
     * */

    @GetMapping("/funs-as-tools")
    public String functionAsTools(){
        ToolCallback toolCallback = FunctionToolCallback.builder("currentWeather", new WeatherService())
                .description("Returns the weather for a location")
                .inputType(WeatherService.WeatherRequest.class)
                .build();
        return chatClient.prompt("What the weather in New york?")
                .tools(toolCallback)
                .call()
                .content();
    }

    @GetMapping("/toolcallback-beans")
    public String toolCallBackBeans(){
        return chatClient.prompt("What's the weather like in Copenhagen?")
                .tools(toolCallback)
                .call()
                .content();
    }

}
