package com.developi.llm;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * This is the QDrant REST API connector. Normally, QDrant is a supported vector store in langchain4j.
 * However, it works over GRPC and for some reason GRPC didn't work with Domino server.
 * 
 * @author sbasegmez
 *
 */
@RegisterRestClient
@Path("/collections/{collectionName}/points/search")
public interface QdrantSearch {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	SearchResult pointsSearch(@PathParam("collectionName") String collectionName, SearchRequest searchRequest);

	class SearchRequest {
		private float[] vector;
		private int limit;

		@JsonbProperty("with_payload")
		private boolean withPayload;

		public float[] getVector() {
			return vector;
		}

		public void setVector(float[] vector) {
			this.vector = vector;
		}

		public int getLimit() {
			return limit;
		}

		public void setLimit(int limit) {
			this.limit = limit;
		}

		public boolean isWithPayload() {
			return withPayload;
		}

		public void setWithPayload(boolean withPayload) {
			this.withPayload = withPayload;
		}

	}

	class SearchResult {
		private float time;
		private String status;
		private List<ScoredPoint> result;

		public float getTime() {
			return time;
		}

		public void setTime(float time) {
			this.time = time;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public List<ScoredPoint> getResult() {
			return result;
		}

		public void setResult(List<ScoredPoint> result) {
			this.result = result;
		}

	}

	class ScoredPoint {
		private String id;
		private int version;
		private float score;
		private Map<String, String> payload;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public int getVersion() {
			return version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public float getScore() {
			return score;
		}

		public void setScore(float score) {
			this.score = score;
		}

		public Map<String, String> getPayload() {
			return payload;
		}

		public void setPayload(Map<String, String> payload) {
			this.payload = payload;
		}
	}

}
