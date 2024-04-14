package com.developi.engage24;

import com.developi.engage24.data.EmbeddingSource;
import com.developi.engage24.data.ModelType;
import com.hcl.domino.DominoClient;
import com.hcl.domino.data.Database;
import com.hcl.domino.data.Document;
import com.hcl.domino.data.DominoDateTime;
import com.hcl.domino.data.Item;
import com.hcl.domino.dql.DQL;
import com.hcl.domino.mime.MimeData;
import com.hcl.domino.richtext.RichTextRecordList;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectsGateway {

    public static final String META_UNID = "unid";
    public static final String META_NAME = "name";
    public static final String META_LAST_MODIFIED = "lastModified";


    public static List<TextSegment> getProjects(DominoClient dominoClient, Document configDoc) {
        Database dbPmt = dominoClient.openDatabase(getProjectDbPath(dominoClient));
        System.out.println("Connected to database: " + dbPmt.getServer() + "!!!" + dbPmt.getRelativeFilePath());

        List<TextSegment> projects = new ArrayList<>();

        Optional<DominoDateTime> lastUploaded = configDoc.getOptional("LastUploaded", DominoDateTime.class);
        System.out.println("Last uploaded: " + lastUploaded.map(DominoDateTime::toString)
                                                           .orElse("never"));

        DQL.DQLTerm dql = DQL.item("Form")
                             .isEqualTo("Project");

        if (lastUploaded.isPresent()) {
            dql = DQL.and(dql, DQL.modifiedInThisFile()
                                  .isGreaterThan(lastUploaded.get()));
        }

        dbPmt.queryDQL(dql)
             .forEachDocument(0, Integer.MAX_VALUE, (doc, loop) -> extractTextSegment(doc).ifPresent(projects::add));

        return projects;
    }

    private static String getProjectDbPath(DominoClient dominoClient) {
        String projectDbPath = System.getenv("PROJECTDB_FILEPATH");

        if (projectDbPath == null || projectDbPath.isEmpty()) {
            projectDbPath = dominoClient.getDominoRuntime().getPropertyString("PROJECTDB_FILEPATH");
        }

        if (projectDbPath == null || projectDbPath.isEmpty()) {
            projectDbPath = "openntf/pmt_copy.nsf";
        }

        return projectDbPath;
    }

    private static Optional<TextSegment> extractTextSegment(@NotNull Document projectDoc) {
        return projectDoc.getOptional("ProjectName", String.class)
                         .map(name -> {
                                     String text = extractText(projectDoc);
                                     Metadata metadata = Metadata.from(META_UNID, projectDoc.getUNID())
                                                                 .add(META_NAME, name)
                                                                 .add(META_LAST_MODIFIED, projectDoc.getLastModified()
                                                                                                    .toString());

                                     return TextSegment.from(text, metadata);
                                 }
                         );
    }

    private static String extractText(@NotNull Document doc) {
        final StringBuilder textData = new StringBuilder();

        textData.append(doc.get("ProjectName", String.class, ""))
                .append(" ")
                .append(doc.get("ProjectOverview", String.class, ""))
                .append(" ")
                .append(extractDetails(doc));

        return textData.toString();
    }

    private static String extractDetails(@NotNull Document doc) {

        Optional<Item> item = doc.getFirstItem("Details");

        if(item.isPresent()) {
            switch (item.get().getType()) {
                case TYPE_COMPOSITE: // RichText
                    RichTextRecordList rtl = item.get().getValueRichText();
                    return rtl.extractText();
                case TYPE_MIME_PART: // MIME
                    MimeData mimeData = doc.get("Details", MimeData.class, null);
                    if (null != mimeData) {
                        return mimeData.getPlainText();
                    }
                    break;
                default:
                    return item.get().getAsText(' ');
            }
        }

        return doc.get("DetailsAbstract", String.class, "");
    }

}
