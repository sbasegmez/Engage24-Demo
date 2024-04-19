package com.developi.llm.pmtdemos;

import com.developi.jnx.templates.AbstractStandaloneApp;
import com.hcl.domino.DominoClient;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.util.Arrays;
import java.util.List;

public class PromptsDemo extends AbstractStandaloneApp {

    public static void main(String[] args) {
        new PromptsDemo().run(args);
    }

    interface Assistant {

        @SystemMessage("""
                The user will give a sentence from a user comment. Provide a sentiment for the sentence.
                """)
        String ask(String sentence);

    }


    @Override
    protected void _init() {
    }

    @Override
    protected void _run(DominoClient dominoClient) {
        Assistant assistant = AiServices.builder(Assistant.class)
                                        .chatLanguageModel(OpenAiChatModel.builder()
                                                                          .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                          .temperature(0.1)
                                                                          .build())
                                        .build();

        List<String> sentences = Arrays.asList(
                "I love this product!",
                "This product is terrible!",
                "I am not sure about this product.",
                "Best product in the hall of shame!"
        );

        sentences.forEach(sentence -> {
            System.out.println("\nSentence: " + sentence + "\nSentiment: " + assistant.ask(sentence));
        });

    }
}
