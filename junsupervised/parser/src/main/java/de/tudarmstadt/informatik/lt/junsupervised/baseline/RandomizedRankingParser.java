package de.tudarmstadt.informatik.lt.junsupervised.baseline;

import de.tudarmstadt.informatik.lt.junsupervised.JUnsupervisedParser;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import java.util.*;

/**
 * Variant of {@link JUnsupervisedParser} that uses a randomized ranking.
 */
public class RandomizedRankingParser extends JUnsupervisedParser {
    private Random random = new Random();

    @Override
    protected Deque<RankedToken> rankTokens(ArrayList<Token> tokens, int[][] matrix) {
        LinkedList<RankedToken> ranking = new LinkedList<>();
        for (int i = 0; i < tokens.size(); i++) {
            ranking.add(new RankedToken(random.nextDouble(), i, tokens.get(i)));
        }

        Collections.sort(ranking, new Comparator<RankedToken>() {
            @Override
            public int compare(RankedToken rankedToken1, RankedToken rankedToken2) {
                return -1 * Double.compare(rankedToken1.getScore(), rankedToken2.getScore());
            }
        });

        return ranking;
    }
}
