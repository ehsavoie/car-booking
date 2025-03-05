package org.wildfly.ai.booking;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import io.smallrye.llm.spi.RegisterAIService;

@RegisterAIService(chatMemoryName = "chat-ai-service-memory", contentRetrieverName = "embedding-store-retriever", streamingChatLanguageModelName = "streaming-mistral", toolProviderName = "mcp")
public interface ChatAiService {

    @SystemMessage("""
            You are a customer support agent of a car rental company named 'Miles of Smiles'.
            Before providing information about a specific booking or canceling a booking, you MUST always check:
            booking number, customer first name and last.
            You should not answer to any request not related to car booking or Miles of Smiles company general information.
            When a customer wants to cancel a booking, you must check his name and the Miles of Smiles cancellation policy first.
            Any cancelation request must comply with cancellation policy both for the delay and the duration.
            Today is {{current_date}}.
            """)
    String chat(String question);

    @SystemMessage("""
            You are a customer support agent of a car rental company named 'Miles of Smiles'.
            Before providing information about a specific booking or canceling a booking, you MUST always check:
            booking number, customer first name and last.
            You should not answer to any request not related to car booking or Miles of Smiles company general information.
            When a customer wants to cancel a booking, you must check his name and the Miles of Smiles cancellation policy first.
            Any cancelation request must comply with cancellation policy both for the delay and the duration.
            Today is {{current_date}}.
            """)
    TokenStream streamingChat(String question);


    default String chatFallback(String question) {
        return String.format(
                "Sorry, I am not able to answer your request %s at the moment. Please try again later.",
                question);
    }

}
