package com.developi.llm.pmtdemos;

import com.developi.engage24.QDrantUploader;
import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.developi.jnx.templates.AbstractStandaloneApp;
import com.hcl.domino.DominoClient;

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

        String[] args = getArgs();
        // We will ignore this...
        boolean recreate = args.length > 0 && args[0].equals("recreate");

        // This is with the local model
        QDrantUploader uploader1 = new QDrantUploader(
                EmbeddingSource.PROJECTS,
                ModelType.LOCAL);

        uploader1.setRecreate(recreate);
        uploader1.setCounterConsumer(count -> {
            if (count % 500 == 0) System.out.println("LocalModel: Uploaded " + count + " embeddings to QDrant!");
        });
        uploader1.setMessageConsumer(System.out::println);

        uploader1.run(dominoClient);


        // This is with the local model
        QDrantUploader uploader2 = new QDrantUploader(
                EmbeddingSource.PROJECTS,
                ModelType.CLOUD_OPENAI);

        uploader2.setRecreate(recreate);
        uploader2.setCounterConsumer(count -> {
            if (count % 500 == 0) System.out.println("CloudModel: Uploaded " + count + " embeddings to QDrant!");
        });
        uploader2.setMessageConsumer(System.out::println);

        uploader2.run(dominoClient);

    }


}
