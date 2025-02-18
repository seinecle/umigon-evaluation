/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datasets;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Annotation;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public class SubjQA implements DatasetInterface {

    private final String name = "subjqa";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final Task task = Task.FACTUALITY;

    private Map<String, AnnotatedDocument> goldenDocs = new ConcurrentHashMap();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getDataWebLink() {
        return "https://huggingface.co/datasets/subjqa";
    }

    @Override
    public String getPaperWebLink() {
        return "http://dx.doi.org/10.18653/v1/2020.emnlp-main.442";
    }

    @Override
    public String getShortDescription() {
        return "a set of consumer reviews on goods from the “electronics” product category";
    }
    
    @Override
    public int getNumberOfEntries() {
        return 1374;
    }
        
    
    

    @Override
    public Map<String, AnnotatedDocument> getGoldenLabels() {
        return goldenDocs;
    }

    @Override
    public Map<String, AnnotatedDocument> read() {
        JsonbConfig jsonbConfig = new JsonbConfig();
        jsonbConfig.withFormatting(Boolean.TRUE);
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
        try {

            Path goldLabelsPath = Path.of(name, GOLD_LABELS);
            if (Files.exists(goldLabelsPath)) {
                try {
                    goldenDocs = jsonb.fromJson(Files.newBufferedReader(goldLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                    }.getClass().getGenericSuperclass());
                } catch (IOException ex) {
                    Logger.getLogger(SubjQA.class.getName()).log(Level.SEVERE, null, ex);
                }
                return goldenDocs;
            }

            Path documentPath = Path.of(name,"subjective-statements-electronics.txt"); // Replace this with the actual folder path
            List<String> lines = Files.readAllLines(documentPath);

            lines.parallelStream().forEach(line -> {

                Annotation annotation = Annotation.empty().withFactuality(Factuality.SUBJ);
                AnnotatedDocument doc = new AnnotatedDocument(line);
                doc.setText(line);
                doc.addAnnotation(annotation);
                goldenDocs.put(doc.getId(), doc);
            });
        } catch (IOException ex) {
            Logger.getLogger(SubjQA.class.getName()).log(Level.SEVERE, null, ex);
        }
        String json = jsonb.toJson(goldenDocs);
        try {
            Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(SubjQA.class.getName()).log(Level.SEVERE, null, ex);
        }

        return goldenDocs;
    }

}
