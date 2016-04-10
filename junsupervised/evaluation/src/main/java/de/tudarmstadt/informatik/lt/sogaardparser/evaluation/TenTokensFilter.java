package de.tudarmstadt.informatik.lt.sogaardparser.evaluation;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Filters sentences with more than 10 non-punctuation tokens.
 */
public class TenTokensFilter extends JCasAnnotator_ImplBase {

    private Pattern allPuncWord = Pattern.compile("\\p{Punct}+");

    public static final String PARAM_REMOVE_PUNCTUATION = "removePunctuation";
    @ConfigurationParameter(name = PARAM_REMOVE_PUNCTUATION, description = "If true, removes all punctuation tokens",
            defaultValue = "false")
    private boolean removePunctuation = false;

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        List<Sentence> sentenceListCopy = new LinkedList<>(JCasUtil.select(jCas, Sentence.class));
        for (Sentence sentence : sentenceListCopy) {
            List<Token> tokens = JCasUtil.selectCovered(Token.class, sentence);
            if (tokens.size() > 10) {
                int allPuncWordCount = 0;
                for (Token token : tokens) {
                    if (isPunctuation(token)) {
                        allPuncWordCount++;
                    }
                }

                int nonPuncWords = tokens.size() - allPuncWordCount;
                if (nonPuncWords > 10 || nonPuncWords == 0) {
                    sentence.removeFromIndexes();
                    continue;
                }
            }

            if (removePunctuation) {
                //System.out.println(sentence.getCoveredText());
                HashMap<Token, Token> redirectedDependencies = new HashMap<>();
                HashSet<Token> removedTokens = new HashSet<>();

                for (Token token : tokens) {
                    if (isPunctuation(token)) {
                        token.removeFromIndexes();
                        removedTokens.add(token);
                    }
                }

                // failsafe for all-punctuation sentences (occurs in german tiger train)
                if (removedTokens.size() == tokens.size()) {
                    sentence.removeFromIndexes();
                    continue;
                }

                if (!removedTokens.isEmpty()) {

                    List<Dependency> dependencies = JCasUtil.selectCovered(Dependency.class, sentence);
                    for (Dependency dependency : dependencies) {
                        if (removedTokens.contains(dependency.getDependent())) {
                            redirectedDependencies.put(dependency.getDependent(), dependency.getGovernor());
                            dependency.removeFromIndexes();
                        }
                    }

                    Token tokenReplacingPunctuationRoot = null;
                    for (Dependency dependency : dependencies) {
                        while (!removedTokens.contains(dependency.getDependent()) && redirectedDependencies
                                .containsKey(dependency.getGovernor())) {
                            Token target = redirectedDependencies.get(dependency.getGovernor());
                            if (dependency.getGovernor().equals(target)) {
                                if (tokenReplacingPunctuationRoot == null) {
                                    tokenReplacingPunctuationRoot = dependency.getDependent();
                                }
                                dependency.setGovernor(tokenReplacingPunctuationRoot);
                            } else {
                                //System.out.println(String.format("redirecting %s to %s", dependency.getGovernor()
                                // .getCoveredText(), redirectedDependencies.get(dependency.getGovernor())
                                // .getCoveredText()));
                                dependency.setGovernor(target);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isPunctuation(Token token) {
        return  allPuncWord.matcher(token.getCoveredText()).matches();
    }
}

