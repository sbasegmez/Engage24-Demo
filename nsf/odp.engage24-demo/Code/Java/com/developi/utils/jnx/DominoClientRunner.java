package com.developi.utils.jnx;

import java.util.function.Function;

import com.hcl.domino.DominoClient;
import com.hcl.domino.DominoClientBuilder;
import com.hcl.domino.DominoProcess;

/**
 * Simple JNX wrapper for running JNX commands. It provides DominoClient for the given lambda. It will create a 
 * dominoClient on behalf of the server. 
 * 
 * Beware that: This is not an ideal scenario. Normally we need one dominoClient per request/response cycle.
 * 
 * @author sbasegmez
 *
 */
public class DominoClientRunner {

    public static <T> T runOnDominoClient(Function<DominoClient, T> function) {

        try (DominoProcess.DominoThreadContext ctx = DominoProcess.get()
                                                                  .initializeThread();
             DominoClient dc = DominoClientBuilder.newDominoClient()
                                                  .asIDUser()
                                                  .build()) {
            return function.apply(dc);
        }


    }

}
