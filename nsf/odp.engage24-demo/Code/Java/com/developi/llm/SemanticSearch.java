package com.developi.llm;

import java.net.URI;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import com.developi.engage24.ConfigGateway;
import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.developi.langchain4j.xsp.model.LocalModels;
import com.developi.utils.jnx.DominoClientRunner;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.FTQuery;
import com.hcl.domino.data.FTQueryResult;
import com.hcl.domino.data.NoteIdWithScore;
import com.ibm.commons.util.io.json.JsonJavaArray;
import com.ibm.commons.util.io.json.JsonJavaObject;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * This is the semantic search demo. Here, we have FT search and semantic search for local/cloud.
 * 
 * For this code, we need to have all the embedding ready on the vector store.
 * 
 * There are two search method, one is commented out. That one is using standard Qdrant module of 
 * the langchain4j. Because of the GRPC issues, we removed that. 
 * 
 * @author sbasegmez
 *
 */
@ApplicationScoped
@Named("semantic")
public class SemanticSearch {

	@Inject
	@Named("odaAppDb")
	private org.openntf.domino.Database currentDb;

	public String searchSemanticLocal(String searchText) {
		JsonJavaArray result = DominoClientRunner
													.runOnDominoClient(dc -> searchSemanticInternal(dc, ModelType.LOCAL,
															searchText));
		return result.toString();
	}

	public String searchSemanticCloud(String searchText) {
		JsonJavaArray result = DominoClientRunner
													.runOnDominoClient(dc -> searchSemanticInternal(dc,
															ModelType.CLOUD_OPENAI, searchText));
		return result.toString();
	}

	public String searchFulltext(String searchText) {
		JsonJavaArray result = DominoClientRunner.runOnDominoClient(dc -> searchFulltextInternal(dc, searchText));
		return result.toString();
	}

	// Internal version
	private JsonJavaArray searchFulltextInternal(DominoClient dominoClient, String searchText) {
		Database db = dominoClient.openDatabase(currentDb.getServer() + "!!openntf/pmt_copy.nsf");

		JsonJavaArray resultArray = new JsonJavaArray();
		Set<FTQuery> options = EnumSet.of(FTQuery.SCORES, FTQuery.FUZZY);

		String query = searchText.replaceAll("\\s+", " AND ");
		System.out.println(query);
		FTQueryResult ftResult = db.queryFTIndex(query, 1000, options, null, 0, 1000);

		List<NoteIdWithScore> scores = ftResult.getMatchesWithScore();

		scores.forEach(noteIdWithScore -> db.getDocumentById(noteIdWithScore.getNoteId())
											.ifPresent(doc -> {
												if ("project".equalsIgnoreCase(doc.getAsText("Form", ' '))) {
													JsonJavaObject jsonValue = new JsonJavaObject();
													jsonValue.put("name", doc.getAsText("ProjectName", ' '));
													jsonValue.put("unid", doc.getUNID());
													jsonValue.put("score", noteIdWithScore.getScore());
													resultArray.add(jsonValue);
												}
											}));

		return resultArray;
	}

// QDrantClient implementation uses GRPC and it doesn't work well with the Domino.
//    private static JsonJavaArray searchSemanticInternal(DominoClient dominoClient, ModelType modelType, String searchText) {
//        JsonJavaArray resultArray = new JsonJavaArray();
//
//        Document configDoc = ConfigGateway.getEmbeddingConfigDocument(dominoClient, EmbeddingSource.PROJECTS, modelType)
//                                          .orElseThrow(() -> new RuntimeException("Embedding configuration document not found!"));
//
//        String collectionName = configDoc.get("QDrantCollectionName", String.class, "projects");
//
//        EmbeddingModel model;
//
//        if (modelType == ModelType.LOCAL) {
//            model = LocalModels.getOnnxModel(configDoc.get("ModelName", String.class, null));
//        } else if (modelType == ModelType.CLOUD_OPENAI) {
//            model = OpenAiEmbeddingModel.builder()
//                                        .apiKey(configDoc.get("OpenAIKey", String.class, "demo"))
//                                        .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
//                                        .dimensions(modelType.getDimension())
//                                        .build();
//        } else {
//            throw new RuntimeException("Model type not supported: " + modelType);
//        }
//
//        Embedding queryEmbedding = model.embed(searchText)
//                                        .content();
//
//        QdrantClient client =
//                new QdrantClient(
//                        QdrantGrpcClient.newBuilder(
//                                                configDoc.get("QDrantServer", String.class, "localhost"),
//                                                configDoc.get("QDrantPort", Integer.class, 6334),
//                                                false)
//                                        .build());
//
//        EmbeddingStore<TextSegment> embeddingStore = QdrantEmbeddingStore.builder()
//                                                                         .client(client)
//                                                                         .collectionName(collectionName)
//                                                                         .build();
//
//        List<EmbeddingMatch<TextSegment>> relevantValues = embeddingStore.findRelevant(queryEmbedding, 10);
//
//        relevantValues.forEach(embeddingMatch -> {
//            TextSegment segment = embeddingMatch.embedded();
//
//            JsonJavaObject jsonValue = new JsonJavaObject();
//            jsonValue.put("name", segment.metadata("name"));
//            jsonValue.put("unid", segment.metadata("unid"));
//            jsonValue.put("score", embeddingMatch.score());
//
//            resultArray.add(jsonValue);
//
//        });
//
//        return resultArray;
//    }

	private JsonJavaArray searchSemanticInternal(DominoClient dominoClient, ModelType modelType, String searchText) {
		JsonJavaArray resultArray = new JsonJavaArray();

		Document configDoc = ConfigGateway	.getEmbeddingConfigDocument(dominoClient, currentDb.getServer(),
													EmbeddingSource.PROJECTS, modelType)
											.orElseThrow(() -> new RuntimeException(
													"Embedding configuration document not found!"));

		String collectionName = configDoc.get("QDrantCollectionName", String.class, "projects");

		EmbeddingModel model;

		if (modelType == ModelType.LOCAL) {
			model = LocalModels.getOnnxModel(configDoc.get("ModelName", String.class, null));
		} else if (modelType == ModelType.CLOUD_OPENAI) {
			model = OpenAiEmbeddingModel.builder()
										.apiKey(configDoc.get("OpenAIKey", String.class, "demo"))
										.modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
										.dimensions(modelType.getDimension())
										.build();
		} else {
			throw new RuntimeException("Model type not supported: " + modelType);
		}

		Embedding queryEmbedding = model.embed(searchText)
										.content();

		QdrantSearch.SearchRequest query = new QdrantSearch.SearchRequest();
		query.setLimit(10);
		query.setVector(queryEmbedding.vector());
		query.setWithPayload(true);

		try {
			URI uri = URI.create("http://" + configDoc.getAsText("QDrantServer", ' ') + ":"
					+ configDoc.getAsInt("QDrantPort", 6333));

			QdrantSearch qdrantSearch = RestClientBuilder	.newBuilder()
															.baseUri(uri)
															.build(QdrantSearch.class);

			QdrantSearch.SearchResult searchResult = qdrantSearch.pointsSearch(collectionName, query);

			searchResult.getResult()
						.forEach((point) -> {
							JsonJavaObject jsonValue = new JsonJavaObject();
							jsonValue.put("name", point	.getPayload()
														.get("name"));
							jsonValue.put("score", point.getScore());

							resultArray.add(jsonValue);
						});

		} catch (Exception e) {
			throw new RuntimeException("Error in the point search!", e);
		}

		return resultArray;
	}

	public static String vectorise(String text) {
		EmbeddingModel embeddingModel = LocalModels.getOnnxModel("all-minilm-l6-v2");

		Embedding embedding = embeddingModel.embed(text)
											.content();

		return embedding.toString();
	}

}
