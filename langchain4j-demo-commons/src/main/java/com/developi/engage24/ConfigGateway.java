package com.developi.engage24;

import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.CollectionEntry;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoCollection;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfigGateway {

    public static final String DEMOSDB_FILEPATH = "demos/engage24.nsf";

    public static Optional<Document> getEmbeddingConfigDocument(DominoClient dominoClient, EmbeddingSource source, ModelType modelType) {

        // Make sure we have a replica on wherever we are running.
        Database dbConfig = dominoClient.openDatabase(DEMOSDB_FILEPATH);

        String overrideConfigDocumentUNID = System.getProperty("OVERRIDE_CONFIG_DOCUMENT_UNID");
        if(StringUtils.isNotEmpty(overrideConfigDocumentUNID)) {
            System.out.println("Using override config document with UNID: " + overrideConfigDocumentUNID);
            return dbConfig.getDocumentByUNID(overrideConfigDocumentUNID);
        }

        String serverName = dbConfig.getServer();
        if(serverName == null || serverName.isEmpty()) {
            serverName = dominoClient.getIDUserName();
        }

        final List<Object> keys = Arrays.asList(
                StringUtils.defaultIfEmpty(dbConfig.getServer(), "Local"),
                source.getLabel(),
                modelType.getLabel());

        Optional<DominoCollection> embeddings = dbConfig.openCollection("(EmbeddingConfigs)");

        return embeddings.flatMap(dominoCollection -> dominoCollection
                .query()
                .selectByKey(keys, true)
                .firstEntry()
                .flatMap(CollectionEntry::openDocument));

    }

}
