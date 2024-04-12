package com.developi.jnx.templates;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractStandaloneApp {

    private static final Logger logger = Logger.getLogger(AbstractStandaloneApp.class.getName());

    public void run() {
        init();

        // Set the jnx.skipNotesThread property to true to avoid creating a NotesThread.
        // Otherwise, we are going to spend precious time to find a non-error exception!
        System.setProperty("jnx.skipNotesThread", "true");

        // Although the documentation suggests a single string argument, we use an array.
        // The second parameter would be the notes.ini file path, but we don't need it, I guess.
        String[] initArgs = new String[]{
                System.getProperty("Notes_ExecDirectory")
        };

        try {
            DominoProcess.get()
                         .initializeProcess(initArgs);

            try (DominoProcess.DominoThreadContext ctx = DominoProcess.get()
                                                                      .initializeThread();

                 // At this point, it's best to keep the Notes client open. Otherwise, it will ask for a password.
                 DominoClient dc = DominoClientBuilder.newDominoClient()
                                                      .asIDUser()
                                                      .build()) {

                _run(ctx, dc);
            }

        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in running Domino subroutine!", t);
        } finally {
            DominoProcess.get()
                         .terminateProcess();
        }
    }

    private void init() {
        // Initialise dotenv
        initDotenv();

        try {
            // Initialize the app
            _init();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "Error in initialising Domino subroutine!", t);
        }
    }

    private void initDotenv() {
        // This is first time. It will load the .env file.
        Dotenv dotenv = Dotenv.configure()
                              .directory(System.getProperty("user.home"))
                              .filename(".engage24demo.env")
                              .ignoreIfMalformed()
                              .ignoreIfMissing()
                              .load();

        // But we don't want to use dotenv any more. So we will just dump everything to environment variables.
        dotenv.entries()
              .forEach(e -> System.setProperty(e.getKey(), e.getValue()));
    }


    protected abstract void _init();

    protected abstract void _run(DominoProcess.DominoThreadContext ctx, DominoClient dominoClient);

}
