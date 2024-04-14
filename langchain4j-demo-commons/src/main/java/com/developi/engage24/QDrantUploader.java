package com.developi.engage24;

import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class QDrantUploader {

    private static final String COLLECTION_NAME = "projects";

    private final EmbeddingSource source;
    private final ModelType modelType;
    private final EmbeddingModel model;

    private boolean recreate = true;
    private Consumer<Long> counterConsumer;
    private Consumer<String> messageConsumer;

    public QDrantUploader(EmbeddingSource source, ModelType modelType, EmbeddingModel model) {
        this.source = source;
        this.modelType = modelType;
        this.model = model;
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

        message("Using model: " + model.getClass()
                                       .getCanonicalName());

        Document configDoc = ConfigGateway.getEmbeddingConfigDocument(dominoClient, source, modelType)
                                          .orElseThrow(() -> new RuntimeException("Embedding configuration document not found!"));

        EmbeddingStore<TextSegment> embeddingStore = createEmbeddingStore(
                recreate,
                modelType.getDimension(),
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
            Collections.CollectionInfo info = client.getCollectionInfoAsync(QDrantUploader.COLLECTION_NAME)
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
            client.deleteCollectionAsync(QDrantUploader.COLLECTION_NAME)
                  .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete collection", e);
        }
    }

    private void createCollection(QdrantClient client, int dimension) {
        try {
            client.createCollectionAsync(
                          QDrantUploader.COLLECTION_NAME,
                          Collections.VectorParams.newBuilder()
                                                  .setDistance(Collections.Distance.Cosine)
                                                  .setSize(dimension)
                                                  .build())
                  .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to recreate collection", e);
        }
    }

    private EmbeddingStore<TextSegment> createEmbeddingStore(boolean forceRecreate,
                                                             int dimension,
                                                             String qHost,
                                                             int qPort
    ) {
        QdrantClient client =
                new QdrantClient(
                        QdrantGrpcClient.newBuilder(qHost, qPort, false)
                                        .build());

        if (isCollectionExists(client)) {
            if (forceRecreate) {
                message("Recreating collection...");
                removeCollection(client);
                createCollection(client, dimension);
            }
        } else {
            message("Creating collection for the first time...");
            createCollection(client, dimension);
        }

        // We'll use the client object to create the embedding store
        return QdrantEmbeddingStore.builder()
                                   .client(client)
                                   .collectionName(QDrantUploader.COLLECTION_NAME)
                                   .build();

    }

}
