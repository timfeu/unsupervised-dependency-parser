package org.jobimtext.lemmatizer;

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


import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;

public class PatriciaLemmatizerAnnotator extends JCasAnnotator_ImplBase {

    private PosLemmatizer lemmatizer;

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        switch (aJCas.getDocumentLanguage()) {
            case "en":
                try {
                    lemmatizer = PosLemmatizerImpl.createEnglishNounVerbAdjectiveLemmatizer();
                } catch (IOException e) {
                    throw new AnalysisEngineProcessException(e);
                }
                break;
            case "de":
                lemmatizer = PosLemmatizerImpl.createGermanNounVerbAdjectiveLemmatizer();
                break;
            default:
                throw new AnalysisEngineProcessException(new IllegalArgumentException(String.format("Unsupported " +
                        "document language %s", aJCas.getDocumentLanguage())));
        }

        for (Token token : JCasUtil.select(aJCas, Token.class)) {
            String lemma = lemmatizer.lemmatizeWord(token.getCoveredText(), token.getPos().getPosValue());
            Lemma lemmaAnnotation = new Lemma(aJCas, token.getBegin(), token.getEnd());
            lemmaAnnotation.setValue(lemma);
            lemmaAnnotation.addToIndexes();
            token.setLemma(lemmaAnnotation);
        }
    }
}
