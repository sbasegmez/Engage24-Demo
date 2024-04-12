package com.developi.llm.pmtdemos;

import com.developi.jnx.templates.AbstractStandaloneApp;
import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoProcess;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.Navigate;
import com.hcl.domino.mime.MimeData;
import com.hcl.domino.richtext.RichTextRecordList;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

public class Upload2QDrantDemo extends AbstractStandaloneApp {

//    private static final Logger logger = Logger.getLogger(Upload2QDrantDemo.class.getName());

    public static void main(String[] args) {
        Upload2QDrantDemo uploader = new Upload2QDrantDemo();
        uploader.run();
    }

    @Override
    protected void _init() {
        // No initialization needed
    }

    @Override
    protected void _run(DominoProcess.DominoThreadContext ctx, DominoClient dominoClient) {
        AtomicLong count = new AtomicLong(0);

        System.out.println("Uploading embeddings to QDrant!");

        EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();
        System.out.println("Using model: " + model.getClass().getCanonicalName());

        Database dbPmt = dominoClient.openDatabase(System.getProperty("PROJECTDB_SERVER"), System.getProperty("PROJECTDB_FILEPATH"));
        System.out.println("Connected to database: " + dbPmt.getServer() + "!!!" + dbPmt.getRelativeFilePath());

        EmbeddingStore<TextSegment> embeddingStore = buildEmbeddingStore();
        System.out.println("Embedding Store is ready!");

        dbPmt.openCollection("APIProjects")
             .ifPresent(collection -> collection.query()
                                                .direction(Navigate.NEXT_DOCUMENT)
                                                .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> {
                                                    String unid = doc.getUNID();
                                                    String name = doc.get("ProjectName", String.class, "");
                                                    String text = extractText(doc);

                                                    if (StringUtils.isNotEmpty(name)) {
                                                        Metadata metadata = Metadata.from("unid", unid)
                                                                                    .add("name", name);

                                                        TextSegment segment = TextSegment.from(text, metadata);
                                                        Embedding embedding = model.embed(segment)
                                                                                   .content();
                                                        embeddingStore.add(embedding, segment);
                                                        count.getAndIncrement();
                                                    }
                                                }));

        System.out.println("Uploaded " + count.get() + " embeddings to QDrant!");
    }

    private EmbeddingStore<TextSegment> buildEmbeddingStore() {
        String collectionName = System.getProperty("QDRANT_COLLECTION_NAME");
        String qHost = System.getProperty("QDRANT_HOST", "localhost");
        int qPort = NumberUtils.toInt(System.getProperty("QDRANT_PORT"), 6334);

        QdrantClient client =
                new QdrantClient(
                        QdrantGrpcClient.newBuilder(qHost, qPort, false)
                                        .build());

        boolean collectionExists = false;
        try {
            Collections.CollectionInfo info = client.getCollectionInfoAsync(collectionName)
                                                    .get();

            collectionExists = (null != info);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to get collection info!", e);
        } catch (Exception e) {
            // Otherwise, collection seems doesn't exist, we can ignore this exception
        }

        if (collectionExists) {
            try {
                System.out.println("Deleting existing collection...");
                client.deleteCollectionAsync(collectionName)
                      .get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete collection", e);
            }
        } else {
            System.out.println("Collection does not exist, creating a new one...");
        }

        // We seem to be safe to recreate the collection
        try {
            int DIMENSION = 384;
            client.createCollectionAsync(
                          collectionName,
                          Collections.VectorParams.newBuilder()
                                                  .setDistance(Collections.Distance.Cosine)
                                                  .setSize(DIMENSION)
                                                  .build())
                  .get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to recreate collection", e);
        }

        // We'll use the client object to create the embedding store
        return QdrantEmbeddingStore.builder()
                                   .client(client)
                                   .collectionName(collectionName)
                                   .build();

    }

    private static String extractText(@NotNull Document doc) {
        final StringBuilder textData = new StringBuilder();

        textData.append(doc.get("ProjectName", String.class, ""));
        textData.append(" ");
        textData.append(doc.get("ProjectOverview", String.class, ""));
        textData.append(" ");

        final StringBuffer details = new StringBuffer();

        doc.getFirstItem("Details")
           .ifPresent(item -> {
               switch (item.getType()) {
                   case TYPE_COMPOSITE: // RichText
                       RichTextRecordList rtl = item.getValueRichText();
                       details.append(rtl.extractText());
                       break;
                   case TYPE_MIME_PART: // MIME
                       MimeData mimeData = doc.get("Details", MimeData.class, null);
                       if (null != mimeData) {
                           details.append(mimeData.getPlainText());
                       }
                       break;
                   default:
                       details.append(item.getAsText(' '));
               }
           });

        // Fallback to DetailsAbstract if Details is empty
        if (details.length() > 0) {
            textData.append(details);
        } else {
            textData.append(doc.get("DetailsAbstract", String.class, ""));
        }

        return textData.toString();
    }
}
