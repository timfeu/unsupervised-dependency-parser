package de.tudarmstadt.informatik.lt.junsupervised.baseline;

import de.tudarmstadt.informatik.lt.junsupervised.JUnsupervisedParser;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.descriptor.ConfigurationParameter;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Variant of {@link JUnsupervisedParser} that uses a ranking in the order of the tokens.
 */
public class LinearRankingParser extends JUnsupervisedParser {

    public static final String PARAM_INVERT = "invertRanking";
    @ConfigurationParameter(name = PARAM_INVERT, description = "whether to invert the ranking. If true, trees become " +
            "left-branching", defaultValue = "false")
    private boolean invertRanking;

    @Override
    protected Deque<JUnsupervisedParser.RankedToken> rankTokens(ArrayList<Token> tokens, int[][] matrix) {
        LinkedList<JUnsupervisedParser.RankedToken> ranking = new LinkedList<>();

        for (int i = 0; i < tokens.size(); i++) {

            if (invertRanking) {
                ranking.addFirst(new JUnsupervisedParser.RankedToken(tokens.size() - i, tokens.size() - i, tokens.get
                        (i)));
            } else {
                ranking.add(new JUnsupervisedParser.RankedToken(i, i, tokens.get(i)));
            }
        }

        return ranking;
    }
}
