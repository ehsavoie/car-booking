package org.wildfly.ai.car.booking.ingestor;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CarBookingIngestor {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CarBookingIngestor.class.getName());

    private EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

    private InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    private List<Document> loadDocs(Path docs) {
        return loadDocuments(docs, new TextDocumentParser());
    }

    public void ingest(Path folder) {
        long start = System.currentTimeMillis();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        List<Document> docs = loadDocs(folder);
        ingestor.ingest(docs);
        log.info(String.format("DEMO %d documents ingested in %d msec", docs.size(), System.currentTimeMillis() - start));
        Path file = Paths.get("../car-booking/extra-content/standalone/configuration/embeddings.json");
        embeddingStore.serializeToFile(file);
    }

    public static void main(String[] args) {
        CarBookingIngestor ingestor = new CarBookingIngestor();
        Path folder = Paths.get("docs-for-rag");
        ingestor.ingest(folder);
        
    }
}
