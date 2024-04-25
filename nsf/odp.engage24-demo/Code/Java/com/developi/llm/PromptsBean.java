package com.developi.llm;

import java.util.Map;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * This is the FewShots demo running from the XPages. There is not much difference from the standalone
 * version, except we get the chat model from the ChatterService for simplicity.
 * 
 * We don't provide a memory mechanism in this case, as we don't need one. 
 * 
 * @author sbasegmez
 *
 */
@ApplicationScoped
@Named("prompts")
public class PromptsBean {

	@Inject
	@Named("chatterService")
	private ChatterService chatterService;

	private SentimentServiceFewShots sentimentServiceFewShots;

	public void checkComment() {
		Map<String,Object> viewScope = ExtLibUtil.getViewScope();
		
		String comment = (String)viewScope.get("commentText");
		
		viewScope.put("checkResult", getSentimentServiceFewShots().ask(comment));
	}

	public SentimentServiceFewShots getSentimentServiceFewShots() {
		if (sentimentServiceFewShots == null) {
			this.sentimentServiceFewShots = AiServices	.builder(SentimentServiceFewShots.class)
														.chatLanguageModel(chatterService.getChatModel())
														.build();
		}

		return sentimentServiceFewShots;
	}

	interface SentimentServiceFewShots {

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

}
