package de.tudarmstadt.informatik.lt.sogaardparser.evaluation;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.NN;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.V;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.regex.Pattern;

/**
 * Annotator that annotates all lower-case words as verbs and all others as nouns.
 */
public class CasingVerbAnnotator extends JCasAnnotator_ImplBase {

    Pattern lowercase = Pattern.compile("\\p{javaLowerCase}+");

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        for (Token token : JCasUtil.select(aJCas, Token.class)) {
            POS pos = null;
            if (lowercase.matcher(token.getCoveredText()).matches()) {
                pos = new V(aJCas, token.getBegin(), token.getEnd());
                pos.setPosValue("V");
            } else {
                pos = new NN(aJCas, token.getBegin(), token.getEnd());
                pos.setPosValue("NN");
            }
            pos.addToIndexes();
            token.setPos(pos);
        }
    }
}
