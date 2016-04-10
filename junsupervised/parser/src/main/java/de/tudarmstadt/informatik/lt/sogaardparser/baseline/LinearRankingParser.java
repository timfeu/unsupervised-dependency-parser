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
                ranking.addFirst(new JUnsupervisedParser.RankedToken(tokens.size() - i, i, tokens.get
                        (i)));
            } else {
                ranking.add(new JUnsupervisedParser.RankedToken(i, i, tokens.get(i)));
            }
        }

        return ranking;
    }
}
