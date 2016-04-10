package de.tudarmstadt.informatik.lt.junsupervised;

import de.tudarmstadt.informatik.lt.junsupervised.type.UniversalPOS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Reads Penn Treebank POS tags and adds new {@link UniversalPOS} tags.
 */
public class UniversalPOSTagConverter extends JCasAnnotator_ImplBase {
    private HashMap<String, String> conversions = new HashMap<>();

    public static final String PARAM_MAPPING = "paramMapping";

    @ConfigurationParameter(name = PARAM_MAPPING, description = "Path to a blank space separated mapping from POS " +
            "tags to universal POS tags, one pair per line", mandatory = true)
    private String mappingPath;

    public static final String PARAM_PRINT_MAPPING = "printMapping";
    @ConfigurationParameter(name = PARAM_PRINT_MAPPING, description = "Path to a blank space separated mapping from " +
            "POS " +
            "tags to universal POS tags, one pair per line", defaultValue = "false")
    private boolean printMapping;

    public static final String PARAM_OVERWRITE_POS = "overwritePos";
    @ConfigurationParameter(name = PARAM_OVERWRITE_POS, description = "Whether to override existing posValues on " +
            "token and POS annotations with the universal POS", defaultValue = "false")
    private boolean overwritePos;

    private static final String MESSAGE_DIGEST = "de.tudarmstadt.informatik.lt.unsupervised" +
            ".UniversalPOSTagConverter_Messages";

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);
        try {
            for (String line : Files.readAllLines(Paths.get(mappingPath), StandardCharsets.UTF_8)) {
                String[] parts = line.split("\\s+");
                if (parts.length < 2) {
                    throw new ResourceInitializationException(MESSAGE_DIGEST, "badFormat", new Object[]{mappingPath});
                }

                conversions.put(parts[0].trim(), parts[1].trim());
            }
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        if (printMapping) {
            StringBuilder sb = new StringBuilder();
            sb.append("Universal POS mapping:\n");
            for (String from : conversions.keySet()) {
                sb.append(from).append(" -> ").append(conversions.get(from)).append("\n");
            }
            context.getLogger().log(Level.INFO, sb.toString());
        }
    }

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        for (POS pos : JCasUtil.select(aJCas, POS.class)) {
            UniversalPOS universalPOS = new UniversalPOS(aJCas);
            universalPOS.setBegin(pos.getBegin());
            universalPOS.setEnd(pos.getEnd());
            String posValue = convertValue(pos.getPosValue());
            universalPOS.setPosValue(posValue);
            universalPOS.addToIndexes();

            if (overwritePos) {
                pos.setPosValue(posValue);
            }
        }
    }

    private String convertValue(String posValue) {
        if (conversions.containsKey(posValue)) {
            return conversions.get(posValue);
        } else {
            return posValue;
        }
    }
}
