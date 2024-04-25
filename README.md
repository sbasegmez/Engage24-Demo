# Engage 2024 Demo Database
## Experimenting with Integrating Large Language Models into Domino Apps

This repository supports the session "Experimenting with Integrating Large Language Models into Domino Apps" at Engage 2024.

Please note that some of the code requires the project database from OpenNTF, which is not publicly available. However, you can still use this database as a reference and modify the code to utilize your own data. We also employ the `LocalModels` class from the [Domino-LangChain4j](https://github.com/sbasegmez/domino-langchain4j) project.

### Repository Modules

#### langchain4j-demos-commons

This module contains common functionalities used across the demos:

- **[ConfigGateway.java](langchain4j-demo-commons%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fengage24%2FConfigGateway.java)**: A simple gateway to read configuration files.
- **[ProjectsGateway.java](langchain4j-demo-commons%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fengage24%2FProjectsGateway.java)**: Creates a collection of text segments from the OpenNTF project database.
- **[QDrantUploader.java](langchain4j-demo-commons%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fengage24%2FQDrantUploader.java)**: An example class that receives projects, creates embeddings, and uploads them to the QDrant database.

#### langchain4j-demos-cli

This module contains the runnable applications of the demo:

- **[AbstractStandaloneApp.java](langchain4j-demos-cli%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fjnx%2Ftemplates%2FAbstractStandaloneApp.java)**: A reusable class that facilitates running applications with `dominoClient` connectivity. It also loads the `.env` file from your home directory for keys and other configuration variables.
- **[FewShotsDemo.java](langchain4j-demos-cli%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fllm%2Fpmtdemos%2FFewShotsDemo.java)** and **[PromptsDemo.java](langchain4j-demos-cli%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fllm%2Fpmtdemos%2FPromptsDemo.java)**: Examples of simple prompt-based AI services using `langchain4j`.
- **[Upload2QDrantDemo.java](langchain4j-demos-cli%2Fsrc%2Fmain%2Fjava%2Fcom%2Fdevelopi%2Fllm%2Fpmtdemos%2FUpload2QDrantDemo.java)**: A standalone application that uploads project embeddings to the Qdrant database. Utilizes `QDrantUploader` from `langchain4j-demos-commons`.

#### langchain4j-demos-addin

This module includes an experimental Java addin intended to run on the server. Currently, it does not function as expected due to a class dependency issue on the Domino server.

#### nsf/odp.engage24-demo

The [ODP project](nsf%2Fodp.engage24-demo) for the demo NSF. You can see how langchain4j is implemented in java classes in [com.developi.llm]([llm](nsf%2Fodp.engage24-demo%2FCode%2FJava%2Fcom%2Fdevelopi%2Fllm)) package. 

Feel free to play with it. Currently, it needs designer and server plugins from the [Domino-LangChain4j](https://github.com/sbasegmez/domino-langchain4j) project and also a Vector store prepared in the Qdrant server. Semantic search will look for embedding config documents for local and cloud model examples. Fulltext search will need the OpenNTF project database which is not publicly available.

You may define OpenAI Api key in xsp.properties file (in the database or in the server).

