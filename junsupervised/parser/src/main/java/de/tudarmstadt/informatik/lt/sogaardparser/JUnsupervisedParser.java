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


import de.tudarmstadt.informatik.lt.pagerank.PageRank;
import de.tudarmstadt.informatik.lt.sogaardparser.type.UniversalPOS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.descriptor.TypeCapability;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Level;
import org.jobimtext.api.struct.DatabaseThesaurusDatastructure;
import org.jobimtext.api.struct.Order2;
import org.ujmp.core.SparseMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Unsupervised unlabeled dependency parser for natural languages.
 * <p/>
 * Takes a sentence as input and outputs a dependency structure over its tokens, that is:
 * <ul>
 * <li>Each token is assigned exactly one head</li>
 * <li>There is a path from a single artificial ROOT element to each token</li>
 * <li>The tree is cycle-free</li>
 * </ul>
 * The implementation follows the paper: <i>ANDERS SØGAARD (2012). Unsupervised dependency parsing without training.
 * Natural Language Engineering, 18, pp 187­203 doi:10.1017/S1351324912000022</i>
 * <p/>
 * The algorithm operates on a single sentence only and is comprised of two steps. In the first step, the tokens are
 * ordered according to their salience by creating a directed graph and running PageRank on it. The edges are
 * constructed using the following rules:
 * <ol>
 * <li>Add an edge between two neighboring tokens (one for each direction), including two-step-neighbors</li>
 * <li>Add incoming edges to function words, originating from their immediate neighbors</li>
 * <li>Add bidirectional edges between words with different prefixes and/or suffixes (2-3 letters)</li>
 * <li>Add edges from all words to all verbs (only if configuration parameter {@link #PARAM_USE_POS_VERB} is true)</li>
 * </ol>
 * In the second step, a tree is constructed from the ranked tokens. In descending order of their PageRank score, they
 * are assigned any head that already has an assignment (which is ROOT for the first token). The nearest head is taken.
 * Alternatively, if {@link #PARAM_USE_UNIVERSAL_RULES} is true, universal dependency rules as discussed in the paper
 * are given precedence over locality. <b>The universal rules require the presence of POS tags following the tagset
 * convention layed out in Petrov, Das and McDonald (2011).</b>
 */
@TypeCapability(inputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence",
        "de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token",
}, outputs = {
        "de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency"
})
public class JUnsupervisedParser extends JCasAnnotator_ImplBase {

    /**
     * How many links to add between direct neighbors (number of directional links, results in 2* bidirectional links
     * between neighbors).
     */
    public static final String PARAM_NEIGHBOR_LINK_COUNT = "neighborLinkCount";
    @ConfigurationParameter(name = PARAM_NEIGHBOR_LINK_COUNT, description = "How many links to add between direct " +
            "neighbors (number of directional links, results in 2* bidirectional links between neighbors).",
            defaultValue = "1")
    private int neighborLinkCount = 1;

    /**
     * Whether to use the POS tag for adding extra edges to verbs. Assumes all tags starting with V to be verbs.
     */
    public static final String PARAM_USE_POS_VERB = "usePosVerb";

    @ConfigurationParameter(name = PARAM_USE_POS_VERB, description = "Whether to use the POS tag for adding extra " +
            "edges to verbs. Assumes all tags starting with V to be verbs.", defaultValue = "true")
    private boolean usePosVerb = true;

    /**
     * Whether to use universal dependency rules during the tree construction step. Requires POS tags to be present
     * following the universal tag rules layed out in Petrov, Das and McDonald (2011).
     */
    public static final String PARAM_USE_UNIVERSAL_RULES = "useUniversalRules";
    @ConfigurationParameter(name = PARAM_USE_UNIVERSAL_RULES, description = "Whether to use universal dependency " +
            "rules during the tree construction step. Requires POS tags to be present following the universal tag " +
            "rules layed out in Petrov, Das and McDonald (2011).", defaultValue = "false")
    private boolean useUniversalRules = false;

    public static final String PARAM_TWO_PASS = "twoPass";
    @ConfigurationParameter(name = PARAM_TWO_PASS, description = "If true, will use the dependencies from the first " +
            "run as additional links for another round of page rank.", defaultValue = "false")
    private boolean twoPass = false;

    /**
     * If two pass is enabled, how many links should be added from dependent to governor.
     */
    public static final String PARAM_TWO_PASS_LINK_WEIGHT = "twoPassLinkWeight";
    @ConfigurationParameter(name = PARAM_TWO_PASS_LINK_WEIGHT, description = "If two pass is enabled, how many links " +
            "should be added from dependent to governor.", defaultValue = "2")
    private int twoPassLinkWeight = 2;

    /**
     * Path to a newline-separated list of function words.
     */
    public static final String PARAM_FUNCTION_WORDS_FILE = "functionWordsList";
    @ConfigurationParameter(name = PARAM_FUNCTION_WORDS_FILE, description = "Path to a newline-separated list of " +
            "function words.")
    private String functionWordsFile;

    public static final String PARAM_APPLY_FUNCTION_WORD_LINKING_TO_FUNCTION_WORDS =
            "applyFunctionWordLinkingToFunctionWords";
    @ConfigurationParameter(name = PARAM_APPLY_FUNCTION_WORD_LINKING_TO_FUNCTION_WORDS, description = "Whether to add" +
            " the function word link even if the target is also a function word", defaultValue = "true")
    private boolean applyFunctionWordLinkingToFunctionWords = true;

    /**
     * Path to a newline-separated list of multi-word expressions in the following format:
     * # of tokens |tab| space separated tokens |tab| druid score |tab| other values
     */
    public static final String PARAM_MWE_FILE = "mweFile";
    @ConfigurationParameter(name = PARAM_MWE_FILE, description = "Path to a newline-separated list of multi-word " +
            "expressions in the following format: # of tokens |tab| space separated tokens |tab| druid score |tab| " +
            "other values", mandatory = false)
    private String mweFile;

    /**
     * Druid score threshold above which multi-word expressions are considered significant. If PARAM_MWE_FILE is not
     * set, this parameter does nothing.
     */
    public static final String PARAM_MWE_SCORE = "mweScore";
    @ConfigurationParameter(name = PARAM_MWE_SCORE, description = "Druid score threshold above which multi-word " +
            "expressions are considered significant. If PARAM_MWE_FILE is not set, this parameter does nothing.",
            defaultValue = "0.5")
    private float mweMinScore = 0.5f;

    /**
     * Maximum size of the multi-word expression detection window. Must be at least 2, otherwise no MWEs will be
     * detected.
     */
    public static final String PARAM_MWE_MAX_TOKENS = "mweMaxTokens";
    @ConfigurationParameter(name = PARAM_MWE_MAX_TOKENS, description = "Maximum size of the multi-word expression " +
            "detection window. Must be at least 2, otherwise no MWEs will be detected.", defaultValue = "4")
    private int mweMaxTokens = 4;

    /**
     * If true, removes links between multi-word expressions alltogether
     */
    public static final String PARAM_MWE_REMOVE_LINKS = "mweRemoveLinks";
    @ConfigurationParameter(name = PARAM_MWE_REMOVE_LINKS, description = "If true, removes links between multi-word " +
            "expressions alltogether", defaultValue = "false")
    private boolean mweRemoveLinks = false;

    /**
     * If true, removes links between similar terms according to a JoBimText distributional thesaurus. Requires
     * dtConfigFile option set.
     */
    public static final String PARAM_DT_REMOVE_SIMILAR = "dtRemoveSimilar";
    @ConfigurationParameter(name = PARAM_DT_REMOVE_SIMILAR, description = "If true, removes links between similar " +
            "terms according to a JoBimText distributional thesaurus. Requires" +
            " dtConfigFile option set.", defaultValue = "false")
    private boolean dtRemoveSimilar = false;

    /**
     * Path to a JoBimText distributional thesaurus database configuration file in XML format. Only used if
     * dtRemoveSimilar is set to true.
     */
    public static final String PARAM_DT_CONFIG_FILE = "dtConfigFile";
    @ConfigurationParameter(name = PARAM_DT_CONFIG_FILE, description = "Path to a JoBimText distributional thesaurus " +
            "database configuration file in XML format. Only used if" +
            " dtRemoveSimilar is set to true.", defaultValue = "conf/dt.xml")
    private String dtConfigFile = "conf/dt.xml";

    /**
     * Whether to use the Lemma of a word when looking up DT entries
     */
    public static final String PARAM_DT_USE_LEMMA = "dtUseLemma";
    @ConfigurationParameter(name = PARAM_DT_USE_LEMMA, description = "Whether to use the Lemma of a word when " +
            "looking up DT entries", defaultValue = "true")
    private boolean dtUseLemma = true;

    public static final String PARAM_LINK_SAME_POS_TAG = "linkSamePosTag";
    @ConfigurationParameter(name = PARAM_LINK_SAME_POS_TAG, description = "Whether to link neighboring words with the" +
            " same 2 letter POS tag prefix", defaultValue = "true")
    private boolean linkSamePosTag = true;

    public static final String PARAM_APPLY_LINK_REMOVAL_TO_TWO_STEP_NEIGHBORS = "applyLinkRemovalToTwoStepNeighbors";
    @ConfigurationParameter(name = PARAM_APPLY_LINK_REMOVAL_TO_TWO_STEP_NEIGHBORS, description = "Whether to apply " +
            "link removal (MWE, DT, same POS) also to two step neighbors. May lead to disconnected graphs.",
            defaultValue = "true")
    private boolean applyLinkRemovalToTwoStepNeighbors = true;

    /**
     * A set of lower-case function words.
     */
    private Set<String> functionWords;

    private MWEData mweData;
    private DatabaseThesaurusDatastructure dt = null;
    private Map<String, Set<String>> dtCache = null;

    private UniversalRules ruleset = new UniversalRules();


    private static final String MESSAGE_DIGEST = "de.tudarmstadt.informatik.lt.unsupervised" +
            ".JUnsupervisedParser_Messages";

    /**
     * Reads the list of function words from {@link #functionWordsFile}.
     */
    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        if (useUniversalRules) ruleset.loadDefaultRules();

        functionWords = new HashSet<String>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(functionWordsFile), StandardCharsets
                .UTF_8)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                functionWords.add(line.trim().toLowerCase());
            }
        } catch (FileNotFoundException e) {
            throw new ResourceInitializationException(MESSAGE_DIGEST, "fileNotFound", new
                    Object[]{functionWordsFile}, e);
        } catch (IOException e) {
            throw new ResourceInitializationException(MESSAGE_DIGEST, "IOError", new Object[]{functionWordsFile}, e);
        }

        if (mweFile != null) {
            try {
                mweData = new MWEData(mweFile, mweMinScore);
                context.getLogger().log(Level.INFO, "MWE list loaded from " + mweFile);
            } catch (IOException e) {
                throw new ResourceInitializationException(MESSAGE_DIGEST, "mweReadError", new Object[]{mweFile}, e);
            }
        } else {
            mweData = MWEData.empty();
        }

        if (dtRemoveSimilar) {
            dt = new DatabaseThesaurusDatastructure(dtConfigFile);
            getLogger().log(Level.INFO, "Connecting to thesaurus database");
            if (!dt.connect()) {
                throw new ResourceInitializationException(MESSAGE_DIGEST, "dtConnectError", new Object[]{}, dt
                        .getConnectionError());
            }
            dtCache = new HashMap<>();
        }
    }

    @Override
    public void destroy() {
        if (dt != null) {
            dt.destroy();
        }
        super.destroy();
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
            parseSentence(sentence, jCas);
        }
    }

    protected void parseSentence(Sentence sentence, JCas jCas) {
        ArrayList<Token> tokens = new ArrayList<>(JCasUtil.selectCovered(Token.class, sentence));
        parseSentence(tokens, jCas, twoPass, new int[tokens.size()][tokens.size()]);
    }

    /**
     * Parses the given sentence and adds dependency annotations to the CAS.
     */
    protected void parseSentence(ArrayList<Token> tokens, JCas jCas, boolean twoPassFirstRun, int[][] matrix) {
        Deque<RankedToken> ranking = rankTokens(tokens, matrix.clone());

        /* DEBUG
        for (RankedToken rt : ranking) {
            System.out.println(rt.token.getCoveredText());
        }*/

        LinkedList<RankedToken> heads = new LinkedList<>();

        if (ranking.isEmpty()) {
            return;
        }

        // first is always considered ROOT
        RankedToken firstElement = ranking.pop();
        heads.add(firstElement);

        if (!twoPassFirstRun) {
            // by convention, the Conll2006Writer in DKPro expects elements attached to the root to be looping
            // see https://github.com/dkpro/dkpro-core/issues/628
            Dependency annotation = new Dependency(jCas, firstElement.token.getBegin(), firstElement.token.getEnd());
            annotation.setDependencyType("ROOT");
            annotation.setDependent(firstElement.token);
            annotation.setGovernor(firstElement.token);
            annotation.addToIndexes();
        }

        List<Set<Integer>> mweSets = mweData.createMWESets(tokens, mweMaxTokens, mweMinScore);

        while (!ranking.isEmpty()) {
            RankedToken dependent = ranking.pop();

            boolean bestMatchedRuleOrIsMWE = false;
            int bestDistance = Integer.MAX_VALUE;
            double bestPR = 0.0;
            RankedToken bestHead = null;

            for (RankedToken headCandidate : heads) {
                boolean candidateMatchesRuleOrIsMWE = ruleset.matchesRule(getUniversalPosValueForToken(headCandidate
                                .token),
                        getUniversalPosValueForToken(dependent.token)) || !Collections.disjoint(mweSets.get
                        (headCandidate.index), mweSets.get(dependent.index));

                if (bestHead == null || (!bestMatchedRuleOrIsMWE && candidateMatchesRuleOrIsMWE)) {
                    // start with fresh head OR prefer rule matchers over non-matchers
                    bestMatchedRuleOrIsMWE = candidateMatchesRuleOrIsMWE;
                    bestDistance = Math.abs(headCandidate.index - dependent.index);
                    bestPR = headCandidate.score;
                    bestHead = headCandidate;
                } else if (bestMatchedRuleOrIsMWE && !candidateMatchesRuleOrIsMWE) {
                    continue; // prefer heads matching a rule
                } else {
                    // bestMatchedRule == pairMatchesRule
                    int distance = Math.abs(headCandidate.index - dependent.index);
                    if (distance < bestDistance) {
                        // always prefer best distance
                        bestMatchedRuleOrIsMWE = candidateMatchesRuleOrIsMWE;
                        bestDistance = distance;
                        bestPR = headCandidate.score;
                        bestHead = headCandidate;
                    } else if (distance == bestDistance && headCandidate.score > bestPR) {
                        // in case of a tie, prefer with a better score
                        bestMatchedRuleOrIsMWE = candidateMatchesRuleOrIsMWE;
                        bestDistance = distance;
                        bestPR = headCandidate.score;
                        bestHead = headCandidate;
                    }
                }
            }

            assert bestHead != null : "No head has been assigned in the head search loop";

            if (twoPassFirstRun) {
                matrix[dependent.getIndex()][bestHead.getIndex()] += twoPassLinkWeight;
            } else {
                // create annotation
                Dependency annotation = new Dependency(jCas, dependent.token.getBegin(), dependent.token.getEnd());
                annotation.setDependent(dependent.token);
                annotation.setGovernor(bestHead.token);
                annotation.setDependencyType("DEP");
                annotation.addToIndexes();
            }

            // add to possible heads
            heads.add(dependent);
        }

        if (twoPassFirstRun) {
            parseSentence(tokens, jCas, false, matrix);
        }
    }

    protected String getUniversalPosValueForToken(Token token) {
        List<UniversalPOS> universalPOS = JCasUtil.selectCovered(UniversalPOS.class, token);
        if (universalPOS.isEmpty()) {
            if (token.getPos() != null) {
                return token.getPos().getPosValue();
            }
            return null;
        }
        return universalPOS.get(0).getPosValue();
    }

    /**
     * First step of the parsing algorithm. Ranks tokens according to their PageRank.
     *
     * @param matrix preexisting links between tokens from other sources (e.g. two pass run). <b>The array is
     *               modified during the link discovery step and contains all links afterwards.</b>
     * @return an ordering of tokens, where the values represent the indices of the initial token list.
     */
    protected Deque<RankedToken> rankTokens(final ArrayList<Token> tokens, int[][] matrix) {

        List<Set<Integer>> mweTokenMemberships = mweData.createMWESets(tokens, mweMaxTokens, mweMinScore);

        for (int i = 0; i < tokens.size(); i++) {
            // 1. add bidirectional links to first and second left neighbors (automatically takes care of right)
            if (i > 0 && !tokens.get(i).getCoveredText().equals(tokens.get(i - 1).getCoveredText()) &&
                    !shouldRemoveLinks(tokens, mweTokenMemberships, i, i - 1)) {
                matrix[i][i - 1] += neighborLinkCount;
                matrix[i - 1][i] += neighborLinkCount;
            }
            if (i > 1 && !tokens.get(i).getCoveredText().equals(tokens.get(i - 2).getCoveredText()) &&
                    (!applyLinkRemovalToTwoStepNeighbors || !shouldRemoveLinks(tokens, mweTokenMemberships, i, i
                            - 2))) {
                matrix[i][i - 2] += 1;
                matrix[i - 2][i] += 1;
            }

            // 2. add incoming neighbors to function words
            if (isFunctionWord(tokens.get(i))) {
                if (i > 0 && (applyFunctionWordLinkingToFunctionWords || !isFunctionWord(tokens.get(i - 1))))
                    matrix[i - 1][i] += 1;
                if (i + 1 < tokens.size() && (applyFunctionWordLinkingToFunctionWords || !isFunctionWord(tokens.get(i
                        + 1))))
                    matrix[i + 1][i] += 1;
            }

            // 3. morphological rules (only looking to the left to avoid duplicity)
            for (int j = 0; j < i; j++) {
                if (suffixesInequal(tokens.get(i), tokens.get(j))) {
                    matrix[i][j] += 1;
                    matrix[j][i] += 1;
                }
                if (prefixesInequal(tokens.get(i), tokens.get(j))) {
                    matrix[i][j] += 1;
                    matrix[j][i] += 1;
                }
            }

            // 4. incoming links to verbs
            if (usePosVerb && isVerb(tokens.get(i))) {
                for (int j = 0; j < tokens.size(); j++) {
                    if (j != i) matrix[j][i] += 1;
                }
            }
        }

        //DEBUG:        System.out.println(PageRank.matrixToString(matrix));

        final double[] scores = PageRank.forAdjacencyMatrix(SparseMatrix.Factory.importFromArray(matrix), 1.0,
                PageRank.DEFAULT_CONVERGENCE, PageRank.DEFAULT_MAX_ITERATIONS);

        LinkedList<RankedToken> ranking = new LinkedList<>();
        for (int i = 0; i < tokens.size(); i++) {
            ranking.add(new RankedToken(scores[i], i, tokens.get(i)));
        }

        Collections.sort(ranking, new Comparator<RankedToken>() {
            @Override
            public int compare(RankedToken rankedToken1, RankedToken rankedToken2) {
                return -1 * Double.compare(rankedToken1.score, rankedToken2.score);
            }
        });

        return ranking;
    }

    private boolean shouldRemoveLinks(ArrayList<Token> tokens, List<Set<Integer>> mweTokenMemberships, int i, int j) {
        return shouldRemoveEqualPosTags(tokens.get(i), tokens.get(j)) || shouldRemoveSimilarDTTokens(tokens.get(i),
                tokens.get(j)) || (mweRemoveLinks &&
                !Collections.disjoint(mweTokenMemberships.get(i), mweTokenMemberships.get
                        (j)));
    }

    private boolean shouldRemoveEqualPosTags(Token token1, Token token2) {
        if (linkSamePosTag) return false;
        String pos1 = token1.getPos().getPosValue();
        String pos2 = token2.getPos().getPosValue();
        return (pos1.substring(0, Math.min(pos1.length(), 2)).equals(pos2.substring(0, Math.min(pos2.length(), 2))));
    }

    private boolean shouldRemoveSimilarDTTokens(Token token1, Token token2) {
        if (!dtRemoveSimilar) return false;
        String term1 = createTerm(token1);
        String term2 = createTerm(token2);
        if (getCachedSimilarTerms(term1).contains(term2)) {
            return true;
        }
        return false;
    }

    private Set<String> getCachedSimilarTerms(String term) {
        if (dtCache.containsKey(term)) {
            return dtCache.get(term);
        } else {
            HashSet<String> similarTerms = new HashSet<>();
            for (Order2 similarTerm : dt.getSimilarTerms(term)) {
                similarTerms.add(similarTerm.key);
            }
            return similarTerms;
        }
    }

    private String createTerm(Token token) {
        return ((dtUseLemma) ? token.getLemma().getValue().toLowerCase() : token.getCoveredText()) + "#" +
                convertPosToTermPos(token.getPos()
                        .getPosValue());
    }

    private String convertPosToTermPos(String posValue) {
        if (posValue.startsWith("NNP")) {
            return "NP";
        } else if (posValue.startsWith("NNS")) {
            return "NN";
        } else if (posValue.startsWith("VB")) {
            return "VB";
        } else if (posValue.startsWith("JJ")) {
            return "JJ";
        } else {
            return posValue;
        }

    }

    private boolean isVerb(Token token) {
        return !(token.getPos() == null || token.getPos().getPosValue() == null) && token.getPos().getPosValue()
                .toLowerCase().startsWith("v");
    }

    private boolean suffixesInequal(Token token1, Token token2) {
        String a = token1.getCoveredText();
        String b = token2.getCoveredText();

        return !(a.substring(Math.max(0, a.length() - 3)).equals(b.substring(Math.max(0, b.length() - 3))));
    }

    private boolean prefixesInequal(Token token1, Token token2) {
        String a = token1.getCoveredText();
        String b = token2.getCoveredText();

        return !(a.substring(0, Math.min(a.length(), 3)).equals(b.substring(0, Math.min(b.length(), 3))));
    }

    private boolean isFunctionWord(Token token) {
        return functionWords.contains(token.getCoveredText().toLowerCase());
    }

    protected class RankedToken {
        double score;
        int index;
        Token token;

        public RankedToken(double score, int index, Token token) {
            this.score = score;
            this.index = index;
            this.token = token;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public Token getToken() {
            return token;
        }

        public void setToken(Token token) {
            this.token = token;
        }

        @Override
        public String toString() {
            return token.getCoveredText() + "@" + String.valueOf(index);
        }
    }
}
