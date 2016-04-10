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
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.fit.util.JCasUtil;
import org.ujmp.core.treematrix.Tree;

import java.util.*;

/**
 * Parser that uses gold dependency annotations to rank the tokens. Used to establish the parser's skyline.
 */
public class OracleParser extends JUnsupervisedParser {
    @Override
    protected Deque<RankedToken> rankTokens(ArrayList<Token> tokens, int[][] matrix) {
        HashMap<Token, TreeNode> tokenToTreeNode = new HashMap<>();
        LinkedList<TreeNode> roots = new LinkedList<>(); // some languages (like Dutch) make use of multiple roots
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
                roots.add(currentNode);
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

        assert !roots.isEmpty();
        assert tokenToTreeNode.size() == tokens.size();

        // perform ranking
        LinkedList<RankedToken> ranking = new LinkedList<>();
        LinkedList<Pair<TreeNode, Double>> queue = new LinkedList<>();
        for (TreeNode root : roots) {
            queue.add(new Pair<TreeNode, Double>(root, 1000.0));
        }

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

        assert ranking.size() == tokens.size() : String.format("Ranking has %d tokens, but should be %d, tree 1 is %s", ranking.size(), tokens.size(), roots.get(0).printTree(""));

        return ranking;
    }

    private class TreeNode {
        int index = -1;
        Token token = null;
        TreeNode parent = null;
        ArrayList<TreeNode> children = new ArrayList<>();

        StringBuilder printTree(String indent) {
            StringBuilder sb = new StringBuilder();
            sb.append(token.getCoveredText()).append(" ").append(index).append("\n");
            for (TreeNode child : children) {
                sb.append(child.printTree("  "));
            }
            return sb;
        }
    }

    private class Pair<A,B> {
        private A left;
        private B right;

        Pair(A left, B right) {
            this.left = left;
            this.right = right;
        }
    }
}
