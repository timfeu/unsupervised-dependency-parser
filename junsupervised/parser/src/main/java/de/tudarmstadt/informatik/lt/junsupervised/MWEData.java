package de.tudarmstadt.informatik.lt.junsupervised;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

/**
 * A mapping from multi-word expressions to their druid scores obtained from some corpus.
 */
public class MWEData {
    private Map<String, Double> entries;

    private MWEData(Map<String, Double> entries) {
        this.entries = entries;
    }

    public static MWEData empty() {
        return new MWEData(Collections.<String, Double>emptyMap());
    }

    /**
     * Reads in a file in DRUID data format, discards any unigrams, and stores multi word expressions together with
     * their druid score.
     */
    public MWEData(String filename, double minScore) throws IOException {
        this(Paths.get(filename), minScore);
    }

    /**
     * Reads in a file in DRUID data format, discards any unigrams, and stores multi word expressions together with
     * their druid score.
     */
    public MWEData(Path file, double minScore) throws IOException {
        entries = new HashMap<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (Integer.valueOf(parts[0]) > 1 && Double.valueOf(parts[2]) >= minScore) {
                    entries.put(parts[1], Double.valueOf(parts[2]));
                }
            }
        }
        if (entries.isEmpty()) {
            Logger.getLogger(this.getClass().getName()).warning("Found no multiword expressions in file " + file
                    .toString());
        }
    }

    /**
     * Returns the druid score for the possible multi-word expression, constructed using the given tokens. If the
     * MWE is not in the database, 0.0 will be returned.
     */
    public double getScore(String... words) {
        return (double) ObjectUtils.defaultIfNull(entries.get(StringUtils.join(words, " ")), 0.0);
    }

    /**
     * Creates a mapping from token id's to their muli-word-expression memberships. If multi-word-expression detection
     * is disabled, each token will receive an empty set. Otherwise, will scan with an increasing window size (from 2
     * till mweMaxTokens) and add any multi-word expression found as a new id to each member token's set.
     * <p/>
     * Example: "Barack Obama visited France" yields a list ({0}, {0}, {}, {}) indicating that "Barack" and "Obama"
     * are both members of the same MWE.
     *
     * @param tokens       a sentence
     * @param mweMaxTokens
     * @param mweMinScore
     */
    List<Set<Integer>> createMWESets(List<Token> tokens, int mweMaxTokens, double mweMinScore) {
        ArrayList<Set<Integer>> mweMemberships = new ArrayList<>(tokens.size());
        for (int i = 0; i < tokens.size(); i++) {
            mweMemberships.add(i, new HashSet<Integer>());
        }

        if (!entries.isEmpty()) {
            int mweId = 0;
            for (int windowSize = 2; windowSize <= mweMaxTokens; windowSize++) {
                for (int i = 0; i + windowSize <= tokens.size(); i++) {
                    String[] possibleMWE = new String[windowSize];
                    for (int j = 0; j < windowSize; j++) {
                        possibleMWE[j] = tokens.get(i + j).getCoveredText();
                    }
                    if (getScore(possibleMWE) >= mweMinScore) {
                        for (int j = 0; j < windowSize; j++) {
                            mweMemberships.get(i + j).add(mweId);
                        }
                        mweId++;
                    }
                }
            }
        }

        return mweMemberships;
    }
}
