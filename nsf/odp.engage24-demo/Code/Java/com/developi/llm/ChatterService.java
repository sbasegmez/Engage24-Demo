package com.developi.llm;

import java.time.Duration;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * 
 * The main service bean is responsible from creating chat service and its chat model.
 * 
 * A single chat service is created using AiService integration and the chat model. 
 * It also adds the BugReportTool as an example.
 * 
 * After some trial error, I decided that this functionality is the best with GPT4 model.
 * GPT 3.5 fails at the tool creation.
 * 
 * This example can be enhanced with RAG functionality.
 * 
 * @author sbasegmez
 *
 */
@ApplicationScoped
@Named("chatterService")
public class ChatterService {

	@Inject
	@Named("openAiApiKey")
	private String openAiApiKey;

	// private static final EmbeddingModel model;
	private ChatLanguageModel chatModel;
	private HelpfulAssistant assistant;

	public ChatterService() {
		// this.model = LocalModels.getOnnxModel("all-minilm-l6-v2");

	}

	ChatLanguageModel getChatModel() {
		if (this.chatModel == null) {
			System.out.println("Creating a new chat model");
			
			this.chatModel = OpenAiChatModel.builder()
											.apiKey(openAiApiKey)
											.timeout(Duration.ofSeconds(30))
//											.modelName(OpenAiChatModelName.GPT_3_5_TURBO_0125)
											.modelName(OpenAiChatModelName.GPT_4_TURBO_PREVIEW)
											.temperature(0.1)
											.logRequests(true)
											.logResponses(true)
											.build();
		}

		return this.chatModel;
	}

	HelpfulAssistant getAssistant() {
		if(assistant == null) {
			System.out.println("Creating a new assistant");
		
			this.assistant = AiServices
					.builder(HelpfulAssistant.class)
					.chatLanguageModel(getChatModel())
					.chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(20))
					.tools(new BugReportTool())
					.build();
		} else {
			// debug memory
		}
		
		return assistant;
	}
}
