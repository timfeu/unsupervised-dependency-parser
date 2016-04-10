package de.tudarmstadt.informatik.lt.sogaardparser;

/*
 * #%L
 * JUnsupervisedParser
 * %%
 * Copyright (C) 2016 Tim Feuerbach
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */


import de.tudarmstadt.informatik.lt.sogaardparser.type.UniversalPOS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
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
