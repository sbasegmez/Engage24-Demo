package com.developi.llm.pmtdemos;

import com.developi.engage24.QDrantUploader;
import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.developi.jnx.templates.AbstractStandaloneApp;
import com.hcl.domino.DominoClient;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;

public class Upload2QDrantDemo extends AbstractStandaloneApp {

    //    private static final Logger logger = Logger.getLogger(Upload2QDrantDemo.class.getName());

    public static void main(String[] args) {
        Upload2QDrantDemo uploader = new Upload2QDrantDemo();
        uploader.run(args);
    }

    @Override
    protected void _init() {
        // No initialization needed
    }

    @Override
    protected void _run(DominoClient dominoClient) {

        QDrantUploader uploader = new QDrantUploader(
                EmbeddingSource.PROJECTS,
                ModelType.LOCAL_MINILM,
                new AllMiniLmL6V2EmbeddingModel());

        String[] args = getArgs();

        // We will ignore this...
        uploader.setRecreate(args.length > 0 && args[0].equals("recreate"));
        uploader.setCounterConsumer(count -> {
            if (count % 500 == 0) System.out.println("Uploaded " + count + " embeddings to QDrant!");
        });
        uploader.setMessageConsumer(System.out::println);

        uploader.run(dominoClient);

    }


}
