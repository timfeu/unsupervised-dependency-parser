package de.tudarmstadt.informatik.lt.sogaardparser.baseline;

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


import de.tudarmstadt.informatik.lt.sogaardparser.JUnsupervisedParser;
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
