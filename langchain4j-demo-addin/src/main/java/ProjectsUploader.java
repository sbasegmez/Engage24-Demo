import com.developi.engage24.QDrantUploader;
import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.hcl.domino.DominoClient;
import com.hcl.domino.exception.ObjectDisposedException;
import com.hcl.domino.exception.QuitPendingException;
import com.hcl.domino.mq.MessageQueue;
import com.hcl.domino.server.RunJavaAddin;
import com.hcl.domino.server.ServerStatusLine;

public class ProjectsUploader extends RunJavaAddin {


    public ProjectsUploader() {
        super("Engage24AddIn");
    }

    @Override
    protected void runAddin(final DominoClient client, final ServerStatusLine statusLine, final MessageQueue queue) {
        System.out.println("ProjectsUploader is running");
        statusLine.setLine("Engage 2024 Projects Uploader");

        try {
            QDrantUploader uploader = new QDrantUploader(EmbeddingSource.PROJECTS, ModelType.LOCAL);

            uploader.setMessageConsumer(System.out::println);

            uploader.setCounterConsumer(count -> {
                if (count % 50 == 0) {
                    statusLine.setLine("Processed " + count + " documents");
                }
            });

            uploader.run(client);
        } catch (QuitPendingException | ObjectDisposedException e) {
            System.out.println("ProjectsUploader was interrupted! Exiting...");
        }
    }

    public synchronized void stopAddin() {
        System.out.println("Stopping...");
    }
}
