### Roles
Each message is assigned a specific role. These roles categorize the messages, clarifying the context and purpose of each segment of the prompt for the AI model. This structured approach enhances the nuance and effectiveness of communication with the AI, as each part of the prompt plays a distinct and defined role in the interaction.

The primary roles are:

- `System Role`: 
>Guides the AI’s behavior and response style, setting parameters
or rules for how the AI interprets and replies to the input.
It’s akin to providing instructions to the AI before initiating a conversation.

- `User Role`: 
>Represents the user’s input – their questions, commands, or statements to the AI. 
This role is fundamental as it forms the basis of the AI’s response.

- `Assistant Role`: 
> The AI’s response to the user’s input. More than just an answer or reaction,
it’s crucial for maintaining the flow of the conversation. By tracking the
AI’s previous responses (its 'Assistant Role' messages), the system ensures
coherent and contextually relevant interactions. The Assistant message may
contain Function Tool Call request information as well. It’s like a special
feature in the AI, used when needed to perform specific functions such as
calculations, fetching data, or other tasks beyond just talking.

- `Tool/Function Role`: 
>The Tool/Function Role focuses on returning additional information in response
to Tool Call Assistant Messages.