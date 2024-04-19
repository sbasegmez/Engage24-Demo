package com.developi.engage24;

import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.developi.langchain4j.xsp.model.LocalModels;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModelName;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class QDrantUploader {

    private final EmbeddingSource source;
    private final ModelType modelType;

    private boolean recreate = true;
    private Consumer<Long> counterConsumer;
    private Consumer<String> messageConsumer;

    private String collectionName = "projects";
    private EmbeddingModel model;
    private int dimensions = 384;

    public QDrantUploader(EmbeddingSource source, ModelType modelType) {
        this.source = source;
        this.modelType = modelType;
    }

    public void setRecreate(boolean recreate) {
        this.recreate = recreate;
    }

    public boolean isRecreate() {
        return recreate;
    }

    public void setCounterConsumer(Consumer<Long> counterConsumer) {
        this.counterConsumer = counterConsumer;
    }

    public void setMessageConsumer(Consumer<String> messageConsumer) {
        this.messageConsumer = messageConsumer;
    }

    private void count(long count) {
        if (counterConsumer != null) {
            counterConsumer.accept(count);
        }
    }

    private void message(String message) {
        if (messageConsumer != null) {
            messageConsumer.accept(message);
        }
    }

    public void run(final DominoClient dominoClient) {

        AtomicLong count = new AtomicLong(0);

        message("Uploading embeddings to QDrant!");
        message("Acting as '" + dominoClient.getEffectiveUserName() + "'");

        // Incremental upload has not been implemented yet. So we will override existing embeddings all the time.
        this.recreate = true;

        Document configDoc = ConfigGateway.getEmbeddingConfigDocument(dominoClient, source, modelType)
                                      .orElseThrow(() -> new RuntimeException("Embedding configuration document not found!"));

        this.collectionName = configDoc.get("QDrantCollectionName", String.class, "projects");

        if (modelType == ModelType.LOCAL) {
            this.model = LocalModels.getOnnxModel(configDoc.get("ModelName", String.class, null));
            this.dimensions = configDoc.get("ModelDimension", Integer.class, 384);
        } else if (modelType == ModelType.CLOUD_OPENAI) {
            this.dimensions = modelType.getDimension();
            this.model = OpenAiEmbeddingModel.builder()
                                             .apiKey(configDoc.get("OpenAIKey", String.class, "demo"))
                                             .modelName(OpenAiEmbeddingModelName.TEXT_EMBEDDING_3_LARGE)
                                             .dimensions(this.dimensions)
                                             .build();
        } else {
            throw new RuntimeException("Model type not supported: " + modelType);
        }

        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(
                configDoc.get("QDrantServer", String.class, "localhost"),
                configDoc.get("QDrantPort", Integer.class, 6334)
        );

        message("Embedding Store is ready!");

        ProjectsGateway.getProjects(dominoClient, configDoc)
                       .forEach(segment -> {
                           Embedding embedding = model.embed(segment)
                                                      .content();
                           embeddingStore.add(embedding, segment);
                           count(count.incrementAndGet());
                       });

        // When incremental update implemented, we should save the upload time.
        // configDoc.replaceItemValue("LastUpload", LocalDateTime.now());
        // configDoc.save();

        message("Uploaded " + count.get() + " embeddings to QDrant!");
    }


    private boolean isCollectionExists(QdrantClient client) {
        try {
            Collections.CollectionInfo info = client.getCollectionInfoAsync(this.collectionName)
                                                    .get();

            return (null != info);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to get collection info!", e);
        } catch (Exception e) {
            // Otherwise, collection seems doesn't exist, we can ignore this exception
        }
        return false;
    }

    private void removeCollection(QdrantClient client) {
        try {
            message("Deleting existing collection...");
            client.deleteCollectionAsync(this.collectionName)
                  .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete collection", e);
        }
    }

    private void createCollection(QdrantClient client) {
        try {
            client.createCollectionAsync(
                          this.collectionName,
                          Collections.VectorParams.newBuilder()
                                                  .setDistance(Collections.Distance.Cosine)
                                                  .setSize(this.dimensions)
                                                  .build())
                  .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to recreate collection", e);
        }
    }

    private EmbeddingStore<TextSegment> createEmbeddingStore(String qHost, int qPort) {
        QdrantClient client =
                new QdrantClient(
                        QdrantGrpcClient.newBuilder(qHost, qPort, false)
                                        .build());

        if (isCollectionExists(client)) {
            if (isRecreate()) {
                message("Recreating collection...");
                removeCollection(client);
                createCollection(client);
            }
        } else {
            message("Creating collection for the first time...");
            createCollection(client);
        }

        // We'll use the client object to create the embedding store
        return QdrantEmbeddingStore.builder()
                                   .client(client)
                                   .collectionName(this.collectionName)
                                   .build();

    }

}
