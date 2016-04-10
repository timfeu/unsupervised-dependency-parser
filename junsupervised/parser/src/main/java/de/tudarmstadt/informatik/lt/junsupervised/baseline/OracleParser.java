package de.tudarmstadt.informatik.lt.junsupervised.baseline;

import de.tudarmstadt.informatik.lt.junsupervised.JUnsupervisedParser;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.analysis_engine.annotator.AnnotatorProcessException;
import org.apache.uima.fit.util.JCasUtil;

import java.util.*;

/**
 * Parser that uses gold dependency annotations to rank the tokens. Used to establish the parser's skyline.
 */
public class OracleParser extends JUnsupervisedParser {
    @Override
    protected Deque<RankedToken> rankTokens(ArrayList<Token> tokens, int[][] matrix) {
        HashMap<Token, TreeNode> tokenToTreeNode = new HashMap<>();
        TreeNode root = null;
        LinkedList<TreeNode> tokensWithoutDependencies = new LinkedList<>();

        // build tree
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            TreeNode currentNode = tokenToTreeNode.get(token);
            if (currentNode == null) {
                currentNode = new TreeNode();
                currentNode.token = token;
                tokenToTreeNode.put(token, currentNode);
            }

            currentNode.index = i;

            List<Dependency> dependencies = JCasUtil.selectCovered(Dependency.class, token);
            if (dependencies.isEmpty()) {
                getLogger().warn(String.format("No dependency relation annotated for token %s (%d, %d). Relation type is probably dummied out in original file", token.getCoveredText(), token.getBegin(), token.getEnd()));
                tokensWithoutDependencies.add(currentNode);
                continue;
            }
            Dependency dependency = dependencies.get(0);

            if (dependency.getGovernor() == null || dependency.getGovernor().equals(token)) {
                root = currentNode;
            } else {
                TreeNode parentNode = tokenToTreeNode.get(dependency.getGovernor());
                if (parentNode == null) {
                    parentNode = new TreeNode();
                    parentNode.token = dependency.getGovernor();
                    tokenToTreeNode.put(dependency.getGovernor(), parentNode);
                }

                parentNode.children.add(currentNode);
                currentNode.parent = parentNode;
            }

            // delete since we will add our own
            dependency.removeFromIndexes();
        }

        assert root != null;

        // perform ranking
        LinkedList<RankedToken> ranking = new LinkedList<>();
        LinkedList<Pair<TreeNode, Double>> queue = new LinkedList<>();
        queue.add(new Pair<TreeNode, Double>(root, 1000.0));

        while (!queue.isEmpty()) {
            Pair<TreeNode, Double> nodePair = queue.pop();
            double pageRank = nodePair.right;
            TreeNode node = nodePair.left;
            ranking.add(new RankedToken(pageRank, node.index, node.token));
            for (TreeNode child : node.children) {
                queue.add(new Pair<TreeNode, Double>(child, pageRank - 1));
            }
        }

        for (TreeNode node : tokensWithoutDependencies) {
            ranking.add(new RankedToken(0.0, node.index, node.token));
        }

        return ranking;
    }

    private class TreeNode {
        int index = -1;
        Token token = null;
        TreeNode parent = null;
        ArrayList<TreeNode> children = new ArrayList<>();
    }

    private class Pair<A,B> {
        private A left;
        private B right;

        public Pair(A left, B right) {
            this.left = left;
            this.right = right;
        }
    }
}
