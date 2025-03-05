package org.wildfly.ai.booking;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import java.io.StringReader;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.logging.Logger;

@ApplicationScoped
@Path("/car-booking")
public class CarBookingResource {

    private static final Logger LOGGER = Logger.getLogger(CarBookingResource.class);

    @Inject
    private ChatAiService aiService;

    @Inject
    private FraudAiService fraudService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/chat")
    @Operation(summary = "Chat with an asssitant.", description = "Ask any car booking related question.", operationId = "chatWithAssistant")
    @APIResponse(responseCode = "200", description = "Anwser provided by assistant", content = @Content(mediaType = "text/plain"))
    public String chatWithAssistant(
            @Parameter(description = "The question to ask the assistant", required = true, example = "I want to book a car how can you help me?") @QueryParam("question") String question) {
        String answer;
        try {
            answer = aiService.chat(question);
        } catch (Exception e) {
            e.printStackTrace();
            answer = "My failure reason is:\n\n" + e.getMessage();
        }

        return answer;
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @Path("/streaming-chat")
    public void streamingChatWithAssistant(@Context Sse sse, @Context SseEventSink sseEventSink,
            @HeaderParam(HttpHeaders.LAST_EVENT_ID_HEADER) @DefaultValue("-1") int lastReceivedId,
            @QueryParam("question") String question) throws InterruptedException {
        final int lastEventId;
        if (lastReceivedId != -1) {
            lastEventId = lastReceivedId + 1;
        } else {
            lastEventId = 1;
        }
        OutboundSseEvent.Builder eventBuilder = sse.newEventBuilder();
        aiService.streamingChat(question)
                .onPartialResponse(partialResponse -> {
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data(partialResponse.replace("\n", "<br/>"))
                            .reconnectDelay(3000)
                            .comment("This is a token from the llm")
                            .build();
                    sseEventSink.send(sseEvent);
                })
                .onToolExecuted(tooExecution -> {
                    LOGGER.info("Tool " + tooExecution.request().name() + " was called with result " + tooExecution.result());
                })
                .onCompleteResponse(chatResponse -> {
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data("end-data-token")
                            .reconnectDelay(3000)
                            .comment("This is a token from the llm")
                            .build();
                    sseEventSink.send(sseEvent)
                            .whenComplete((event, throwable) -> {
                                sseEventSink.close();
                            });
                })
                .onError(error -> {
                    LOGGER.error("Error processing question \"" + question + "\"", error);
                    JsonObject message = Json.createReader(new StringReader(error.getMessage())).readObject().getJsonObject("error");
                    LOGGER.error("Error sending error " + message.toString());
                    OutboundSseEvent sseEvent = eventBuilder
                            .name("token")
                            .id(String.valueOf(lastEventId + 1))
                            .mediaType(MediaType.TEXT_PLAIN_TYPE)
                            .data(message.toString())
                            .reconnectDelay(3000)
                            .comment("This is an error from the llm")
                            .build();
                    sseEventSink.send(sseEvent);
                })
                .start();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/fraud")
    @Operation(summary = "Detect for a customer.", description = "Detect fraud for a customer given his name and surname.", operationId = "detectFraudForCustomer")
    @APIResponse(responseCode = "200", description = "Anwser provided by assistant", content = @Content(mediaType = "application/json"))
    public FraudResponse detectFraudForCustomer(
            @Parameter(description = "Name of the customer to detect fraud for.", required = true, example = "Bond") @QueryParam("name") String name,
            @QueryParam("surname") @Parameter(description = "Surname of the customer to detect fraud for.", required = true, example = "James") String surname) {
        return fraudService.detectFraudForCustomer(name, surname);
    }

}
