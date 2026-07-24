## Prompt Engineering
>In generative AI, the creation of prompts is a crucial task for developers. The quality and structure of these prompts significantly influence the effectiveness of the AI’s output.
Investing time and effort in designing thoughtful prompts can greatly improve the results from the AI.
Sharing and discussing prompts is a common practice in the AI community.
This collaborative approach not only creates a shared learning environment but also
leads to the identification and use of highly effective prompts.

> Research in this area often involves analyzing and comparing different prompts
to assess their effectiveness in various situations. For example, a significant
study demonstrated that starting a prompt with "Take a deep breath and work on
this problem step by step" significantly enhanced problem-solving efficiency.
This highlights the impact that well-chosen language can have on generative AI
systems' performance.

> Grasping the most effective use of prompts, particularly with the rapid advancement
of AI technologies, is a continuous challenge. You should recognize the importance of
prompt engineering and consider using insights from the community and research to improve
prompt creation strategies.

### Creating effective prompts
When developing prompts, it’s important to integrate several key components to ensure clarity and effectiveness:

- **Instructions**: Offer clear and direct instructions to the AI, similar to how you would communicate with a person. This clarity is essential for helping the AI "understand" what is expected.

- **External Context**: Include relevant background information or specific guidance for the AI’s response when necessary. This "external context" frames the prompt and aids the AI in grasping the overall scenario.

- **User Input**: This is the straightforward part - the user’s direct request or question forming the core of the prompt.

- **Output Indicator**: This aspect can be tricky. It involves specifying the desired format for the AI’s response, such as JSON. However, be aware that the AI might not always adhere strictly to this format. For instance, it might prepend a phrase like "here is your JSON" before the actual JSON data, or sometimes generate a JSON-like structure that is not accurate.

Providing the AI with examples of the anticipated question and answer format can be highly beneficial when crafting prompts. This practice helps the AI "understand" the structure and intent of your query, leading to more precise and relevant responses. While this documentation does not delve deeply into these techniques, they provide a starting point for further exploration in AI prompt engineering.

Following is a list of resources for further investigation.

#### 1. Simple Techniques
- `Text Summarization`:
Reduces extensive text into concise summaries, capturing key points and main ideas while omitting less critical details.

- `Question Answering`:
Focuses on deriving specific answers from provided text, based on user-posed questions. It’s about pinpointing and extracting relevant information in response to queries.

- `Text Classification`:
Systematically categorizes text into predefined categories or groups, analyzing the text and assigning it to the most fitting category based on its content.

- `Conversation`:
Creates interactive dialogues where the AI can engage in back-and-forth communication with users, simulating a natural conversation flow.

- `Code Generation`:
Generates functional code snippets based on specific user requirements or descriptions, translating natural language instructions into executable code.

#### 2. Advanced Techniques

- `Zero-shot, Few-shot Learning`:
Enables the model to make accurate predictions or responses with minimal to no prior examples of the specific problem type, understanding and acting on new tasks using learned generalizations.

- `Chain-of-Thought`:
Links multiple AI responses to create a coherent and contextually aware conversation. It helps the AI maintain the thread of the discussion, ensuring relevance and continuity.

- `ReAct (Reason + Act)`:
In this method, the AI first analyzes (reasons about) the input, then determines the most appropriate course of action or response. It combines understanding with decision-making.

#### 3. Microsoft Guidance
- `Framework for Prompt Creation and Optimization`:
Microsoft offers a structured approach to developing and refining prompts. This framework guides users in creating effective prompts that elicit the desired responses from AI models, optimizing the interaction for clarity and efficiency.