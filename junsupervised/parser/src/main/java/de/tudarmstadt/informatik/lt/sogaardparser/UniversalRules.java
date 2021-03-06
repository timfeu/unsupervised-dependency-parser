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

import java.util.HashSet;

/**
 * Container for universal dependency rules from Fig. 5 (Søgaard 2012).
 */
public class UniversalRules {
    // mapping from head to dependent
    private HashSet<Pair<String, String>> rules = new HashSet<>();

    /**
     * Adds a new universal dependency rule spanning from headPos to dependentPos. The POS should be
     * from the universal tagset  by Petrov, Das and McDonald (2011). Cases will be normalized.
     */
    public void addRule(String headPos, String dependentPos) {
        if (headPos == null || dependentPos == null) throw new IllegalArgumentException("POS may not be null");
        rules.add(new Pair<String, String>(headPos.toUpperCase(), dependentPos.toUpperCase()));
    }

    /**
     * Returns whether their is a rule where the first POS acts as the head for the second POS. Silently
     * returns false if one of the POS is null.
     * </p>
     * This operation is thread-safe if no new rules are added during execution.
     */
    public boolean matchesRule(String headPos, String dependentPos) {
        if (headPos == null || dependentPos == null) return false;
        return rules.contains(new Pair<String, String>(headPos.toUpperCase(), dependentPos.toUpperCase()));
    }

    /**
     * Loads the default set of rules from the implementation (not consistent with paper).
     */
    public void loadDefaultRules() {
        addRule("VERB", "VERB");
        addRule("VERB", "NOUN");
        addRule("VERB", "ADV");
        addRule("VERB", "ADP");
        addRule("VERB", "CONJ");
        addRule("VERB", ".");
        addRule("VERB", "X");
        //addRule("VERB", "DET");
        //addRule("VERB", "NUM");
        addRule("VERB", "ADJ");
        addRule("NOUN", "ADJ");
        addRule("NOUN", "DET");
        addRule("NOUN", "NOUN");
        addRule("NOUN", "NUM");
        addRule("ADP", "NOUN");
        //addRule("ADP", "ADV");
        addRule("ADJ", "ADV");
    }

    private class Pair<K, V> {
        K left;
        V right;

        public Pair(K left, V right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?, ?> pair = (Pair<?, ?>) o;

            if (left != null ? !left.equals(pair.left) : pair.left != null) return false;
            return !(right != null ? !right.equals(pair.right) : pair.right != null);

        }

        @Override
        public int hashCode() {
            int result = left != null ? left.hashCode() : 0;
            result = 31 * result + (right != null ? right.hashCode() : 0);
            return result;
        }
    }
}
