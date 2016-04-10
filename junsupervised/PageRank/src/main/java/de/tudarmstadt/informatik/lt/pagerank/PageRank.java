package de.tudarmstadt.informatik.lt.pagerank;

/*
 * #%L
 * MatrixPageRank
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


import org.ujmp.core.DenseMatrix;
import org.ujmp.core.Matrix;
import org.ujmp.core.SparseMatrix;
import org.ujmp.core.util.UJMPSettings;

/**
 * Helper class providing an implementation of the PageRank algorithm.
 */
public class PageRank {
    public final static double DEFAULT_CONVERGENCE = 1e-8;
    public final static int DEFAULT_MAX_ITERATIONS = 200;
    public final static double DEFAULT_DAMPING_FACTOR = 0.85;

    /**
     * Runs PageRank on a graph defined by an adjacency matrix.
     *
     * @param adjacencyMatrix  an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *                         graph. The
     *                         value indicates the number of directed edges from first to second node, e.g.
     *                         matrix[0][1] = 2 means
     *                         there are two edges from node 0 to node 1.
     * @param dampingFactor    The probability that the surfer follows the links. If set to 1.0, the surfer will always
     *                         follow links; if set to 0, the surfer jumps randomly from node to node (degenerate case,
     *                         since in infinity all nodes get the same PageRank score).
     * @param convergenceDelta the PageRank algorithm stops when the sum of the score differences to before is below
     *                         the given
     *                         value.
     * @param maxIterations    the maximum number of iterations the algorithm is run.
     * @param printStep        if true, prints delta at each iteration and prints out additional debug information
     * @return PageRank scores indexed by node no.
     */
    public static double[] forAdjacencyMatrix(final SparseMatrix adjacencyMatrix, final double dampingFactor, double
            convergenceDelta, int maxIterations, boolean printStep) {
        if (adjacencyMatrix == null || adjacencyMatrix.getValueCount() == 0) return new double[0];

        if (adjacencyMatrix.getRowCount() > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Can't work with matrices with more than INTEGER.MAX_VALUE rows at the" +
                    " moment");
        }

        if (!adjacencyMatrix.isSquare()) {
            throw new IllegalArgumentException(String.format("Expected square adjacency matrix, got %d x %d matrix",
                    adjacencyMatrix.getRowCount(), adjacencyMatrix.getColumnCount()));
        }

        if (printStep) {
            System.out.println(String.format("Starting PageRank with damping factor %f, convergence delta %f and %d " +
                    "max iterations", dampingFactor, convergenceDelta, maxIterations));
            System.out.println("Number of threads: " + UJMPSettings.getInstance().getNumberOfThreads());
        }

        final int vertexCount = (int) adjacencyMatrix.getRowCount();

        // initialize uniformly
        Matrix oldScores = DenseMatrix.Factory.fill(1.0 / (double) vertexCount, vertexCount, 1);

        // create transition probability matrix
        Matrix probabilityMatrix = SparseMatrix.Factory.zeros(vertexCount, vertexCount);

        for (int i = 0; i < vertexCount; i++) {
            int outgoingSum = 0;
            for (int j = 0; j < vertexCount; j++) {
                outgoingSum += adjacencyMatrix.getAsInt(i, j);
            }
            if (outgoingSum > 0) {
                for (int j = 0; j < vertexCount; j++) {
                    probabilityMatrix.setAsDouble(adjacencyMatrix.getAsDouble(i, j) / (double) outgoingSum, i, j);
                }
            }
        }

        // we need it in transposed form
        probabilityMatrix = probabilityMatrix.transpose();

        int iterations = 0;
        Matrix newScores = DenseMatrix.Factory.zeros(vertexCount, 1);
        Matrix dampingConstant = DenseMatrix.Factory.ones(vertexCount, 1).times((1.0 - dampingFactor) / (double)
                vertexCount);

        while (iterations < maxIterations) {
            newScores = probabilityMatrix.mtimes(oldScores).times(dampingFactor).plus(dampingConstant);

            double delta = newScores.minus(oldScores).getAbsoluteValueSum();

            if (printStep) {
                System.out.println(String.format("After iteration %d/%d: delta = %f (maxDelta = %f)", iterations+1, maxIterations, delta, convergenceDelta));
            }

            if (delta < convergenceDelta) {
                break;
            }

            oldScores = newScores;
            iterations++;
        }

        double[] outScores = new double[vertexCount];

        for (int i = 0; i < vertexCount; i++) {
            outScores[i] = newScores.getAsDouble(i, 0);
        }

        return outScores;
    }

    /**
     * Runs PageRank on a graph defined by an adjacency matrix.
     *
     * @param adjacencyMatrix  an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *                         graph. The
     *                         value indicates the number of directed edges from first to second node, e.g.
     *                         matrix[0][1] = 2 means
     *                         there are two edges from node 0 to node 1.
     * @param dampingFactor    The probability that the surfer follows the links. If set to 1.0, the surfer will always
     *                         follow links; if set to 0, the surfer jumps randomly from node to node (degenerate case,
     *                         since in infinity all nodes get the same PageRank score).
     * @param convergenceDelta the PageRank algorithm stops when the sum of the score differences to before is below
     *                         the given
     *                         value.
     * @param maxIterations    the maximum number of iterations the algorithm is run.
     * @return PageRank scores indexed by node no.
     */
    public static double[] forAdjacencyMatrix(final SparseMatrix adjacencyMatrix, final double dampingFactor, double
            convergenceDelta, int maxIterations) {
        return forAdjacencyMatrix(adjacencyMatrix, dampingFactor, convergenceDelta, maxIterations, false);
    }

    /**
     * Runs PageRank on a graph defined by an adjacency matrix. Uses a default convergence delta of {@value
     * #DEFAULT_CONVERGENCE} and a maximum of {@value #DEFAULT_MAX_ITERATIONS} iterations.
     *
     * @param matrix        an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *                      graph. The
     *                      value indicates the number of directed edges from first to second node, e.g.
     *                      matrix[0][1] = 2 means
     *                      there are two edges from node 0 to node 1.
     * @param dampingFactor The probability that the surfer follows the links. If set to 1.0, the surfer will always
     *                      follow links; if set to 0, the surfer jumps randomly from node to node (degenerate case,
     *                      since in infinity all nodes get the same PageRank score).
     * @return PageRank scores indexed by node no.
     * @see #forAdjacencyMatrix(SparseMatrix, double, double, int)
     */
    public static double[] forAdjacencyMatrix(SparseMatrix matrix, double dampingFactor) {
        return forAdjacencyMatrix(matrix, dampingFactor, DEFAULT_CONVERGENCE, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Runs PageRank on a graph defined by an adjacency matrix. Uses a default convergence delta of {@value
     * #DEFAULT_CONVERGENCE} and a maximum of {@value #DEFAULT_MAX_ITERATIONS} iterations.
     *
     * @param matrix        an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *                      graph. The
     *                      value indicates the number of directed edges from first to second node, e.g.
     *                      matrix[0][1] = 2 means
     *                      there are two edges from node 0 to node 1.
     * @param dampingFactor The probability that the surfer follows the links. If set to 1.0, the surfer will always
     *                      follow links; if set to 0, the surfer jumps randomly from node to node (degenerate case,
     *                      since in infinity all nodes get the same PageRank score).
     * @return PageRank scores indexed by node no.
     * @see #forAdjacencyMatrix(SparseMatrix, double, double, int)
     */
    public static double[] forAdjacencyMatrix(int[][] matrix, double dampingFactor) {
        return forAdjacencyMatrix(SparseMatrix.Factory.importFromArray(matrix), dampingFactor, DEFAULT_CONVERGENCE,
                DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Runs PageRank on a graph defined by an adjacency matrix. Uses a damping factor of {@value
     * #DEFAULT_DAMPING_FACTOR},
     * a convergence delta of {@value #DEFAULT_CONVERGENCE} and a maximum of {@value #DEFAULT_MAX_ITERATIONS}
     * iterations.
     *
     * @param matrix an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *               graph. The
     *               value indicates the number of directed edges from first to second node, e.g.
     *               matrix[0][1] = 2 means
     *               there are two edges from node 0 to node 1.
     * @return PageRank scores indexed by node no.
     * @see #forAdjacencyMatrix(SparseMatrix, double, double, int)
     */
    public static double[] forAdjacencyMatrix(int[][] matrix) {
        return forAdjacencyMatrix(SparseMatrix.Factory.importFromArray(matrix), DEFAULT_DAMPING_FACTOR,
                DEFAULT_CONVERGENCE, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Runs PageRank on a graph defined by an adjacency matrix. Uses a damping factor of {@value
     * #DEFAULT_DAMPING_FACTOR},
     * a convergence delta of {@value #DEFAULT_CONVERGENCE} and a maximum of {@value #DEFAULT_MAX_ITERATIONS}
     * iterations.
     *
     * @param matrix an adjacency matrix with n rows and n columns, where n is the number of nodes in the
     *               graph. The
     *               value indicates the number of directed edges from first to second node, e.g.
     *               matrix[0][1] = 2 means
     *               there are two edges from node 0 to node 1.
     * @return PageRank scores indexed by node no.
     * @see #forAdjacencyMatrix(SparseMatrix, double, double, int)
     */
    public static double[] forAdjacencyMatrix(SparseMatrix matrix) {
        return forAdjacencyMatrix(matrix, DEFAULT_DAMPING_FACTOR, DEFAULT_CONVERGENCE, DEFAULT_MAX_ITERATIONS);
    }

    /**
     * Returns a string representation of the given matrix as a 2D-table.
     */
    public static String matrixToString(int[][] matrix) {
        StringBuilder sb = new StringBuilder();

        sb.append("   ");

        for (int i = 0; i < matrix.length; i++) {
            sb.append(String.format("%2d ", i));
        }
        sb.append("\n");

        for (int i = 0; i < matrix.length; i++) {
            sb.append(String.format("%2d ", i));
            for (int j = 0; j < matrix[i].length; j++) {
                sb.append(String.format("%2d ", matrix[i][j]));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private static class AtomicDouble {
        private double value = 0.0;

        public synchronized void add(double value) {
            this.value += value;
        }

        public synchronized double getValue() {
            return value;
        }
    }
}
