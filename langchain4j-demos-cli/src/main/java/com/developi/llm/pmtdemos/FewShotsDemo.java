package com.developi.llm.pmtdemos;

import com.developi.jnx.templates.AbstractStandaloneApp;
import com.hcl.domino.DominoClient;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;

import java.util.Arrays;
import java.util.List;

public class FewShotsDemo extends AbstractStandaloneApp {

    public static void main(String[] args) {
        new FewShotsDemo().run(args);
    }

    interface FewshotsAiService {

        @SystemMessage("""
                Classify incoming sentences into sentiments: Positive, Negative, Neutral, Sarcastic.
                Here are some examples with expected sentiments:
                
                Text: I'm very unhappy with this decision.
                Sentiment: Negative
                
                Text: I absolutely like the idea!
                Sentiment: Positive
                
                Text: Not sure about this.
                Sentiment: Neutral
                
                Text: I would bu millions to protect others.
                Sentiment: Sarcastic
                """)
        String ask(String sentence);

    }


    @Override
    protected void _init() {
    }

    @Override
    protected void _run(DominoClient dominoClient) {
        FewshotsAiService service = AiServices.builder(FewshotsAiService.class)
                                        .chatLanguageModel(OpenAiChatModel.builder()
                                                                          .apiKey(System.getProperty("OPENAI_API_KEY"))
                                                                          .temperature(0.0)
                                                                          .build())
                                        .build();

        List<String> sentences = Arrays.asList(
                "I love this product!",
                "This product is terrible!",
                "I would rather wait before commenting.",
                "Best product in the hall of shame!"
        );

        sentences.forEach(sentence -> {
            System.out.println("\nSentence: " + sentence + "\n > " + service.ask(sentence));
        });

    }
}
