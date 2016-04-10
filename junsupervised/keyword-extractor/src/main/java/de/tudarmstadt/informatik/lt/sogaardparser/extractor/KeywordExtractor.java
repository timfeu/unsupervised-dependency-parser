package de.tudarmstadt.informatik.lt.sogaardparser.extractor;

import de.tudarmstadt.informatik.lt.pagerank.PageRank;
import org.ujmp.core.SparseMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Keyword extractor using the TextRank algorithm (Mihalcea and Tarau 2004) to extract function
 * words of a language.
 */
public class KeywordExtractor {

    /**
     * Extracts function words of a language using TextRank and setence-level co-occurrence
     * to add edges. Skips tokens containing no letter.
     *
     * @param inputFile     plain text, one sentence per line and words separated by blank space (UTF-8)
     * @param outputFile    a newline-separated list of the top ranked words (UTF-8)
     * @param numberOfWords number of top function words to extract
     */
    public static void extract(Path inputFile, Path outputFile, int numberOfWords) throws IOException {
        final Indexer indexer = new Indexer();
        LinkedList<Edge> edges = new LinkedList<>();

        try (BufferedReader reader = Files.newBufferedReader(inputFile, StandardCharsets.UTF_8)) {

            Pattern hasLetterPattern = Pattern.compile(".*\\w.*");

            String line = null;
            System.out.print("[1/3] extracting co-occurrences");
            while ((line = reader.readLine()) != null) {
                String[] words = line.split("\\s+");

                for (int i = 0; i < words.length; i++) {
                    if (!hasLetterPattern.matcher(words[i]).matches()) continue;
                    indexer.addToIndex(words[i]);
                    for (int j = 0; j < i; j++) {
                        if (!hasLetterPattern.matcher(words[j]).matches()) continue;
                        edges.add(new Edge(indexer.getIndex(words[i]), indexer.getIndex(words[j])));
                    }
                }
            }

            System.out.println(" [done]");
            System.out.print("[2/3] building matrix");

            SparseMatrix matrix = SparseMatrix.Factory.zeros(indexer.size(), indexer.size());

            for (Edge edge : edges) {
                matrix.setAsInt(matrix.getAsInt(edge.word1, edge.word2) + 1, edge.word1, edge.word2);
                matrix.setAsInt(matrix.getAsInt(edge.word2, edge.word1) + 1, edge.word2, edge.word1);
            }

            edges = null;

            System.out.println(" [done]");
            System.out.print("[3/3] running PageRank");

            final double[] scores = PageRank.forAdjacencyMatrix(matrix, 0.85, 0.0001, 20, true);
            System.out.println(" [done]");

            List<String> entries = indexer.entries();
            Collections.sort(entries, new Comparator<String>() {
                @Override
                public int compare(String word1, String word2) {
                    return -1 * Double.compare(scores[indexer.getIndex(word1)], scores[indexer.getIndex(word2)]);
                }
            });

            try (BufferedWriter writer = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
                for (int i = 0; i < Math.min(entries.size(), numberOfWords); i++) {
                    writer.write(entries.get(i));
                    writer.write("\n");
                }
            }

            System.out.println("Results written to " + outputFile.toString());
        }
    }

    private static class Edge {
        int word1, word2;

        public Edge(int word1, int word2) {
            this.word1 = word1;
            this.word2 = word2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Edge edge = (Edge) o;

            if (word1 != edge.word1) return false;
            return word2 == edge.word2;

        }

        @Override
        public int hashCode() {
            int result = word1;
            result = 31 * result + word2;
            return result;
        }
    }

    private static class Indexer {
        private HashMap<String, Integer> indices = new HashMap<>();
        private ArrayList<String> entries = new ArrayList<>();

        public int getIndex(String entry) {
            if (indices.containsKey(entry)) {
                return indices.get(entry);
            } else {
                int idx = entries.size();
                indices.put(entry, idx);
                entries.add(entry);
                return idx;
            }
        }

        public void addToIndex(String entry) {
            getIndex(entry);
        }

        public String getEntry(int index) {
            return entries.get(index);
        }

        public int size() {
            return entries.size();
        }

        public List<String> entries() {
            return entries;
        }
    }
}
