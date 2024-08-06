package com.github.javarar.matrix.production;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;


/**
 * Сделано на коленке чтобы успеть к защите,извиняюсь :-(
 */
public class MatrixProduction {

    public static void main(String[] args) {
        Matrix first = new Matrix(3, 3);
        Matrix second = new Matrix(3, 3);

        Matrix result = product(first, second);

        System.out.println(result);
    }

    public static Matrix product(Matrix a, Matrix b) {
        return a.multiply(b);
    }


    public static class Matrix {
        private static final ForkJoinPool forkJoinPool = new ForkJoinPool();
        private static final Random random = new Random();
        private final int rows;
        private final int cols;
        private final int[][] content;

        public Matrix(int rows, int cols) {
            this.rows = rows;
            this.cols = cols;
            content = fill(rows, cols);
        }

        public Matrix(int rows, int cols, int[][] content) {
            this.rows = rows;
            this.cols = cols;
            this.content = content;
        }

        public Matrix multiply(Matrix second) {
            if (this.getCols() != second.getRows()) {
                throw new IllegalArgumentException("Размеры матриц неверны");
            }

            int numRows = this.getRows();
            int numCols = second.getCols();

            List<ComputeResultItemTask> tasks = new ArrayList<>();
            for (int rowNumber = 0; rowNumber < numRows; rowNumber++) {
                for (int colNumber = 0; colNumber < numCols; colNumber++) {
                    ComputeResultItemTask task = new ComputeResultItemTask(new ComputeParamsDto(rowNumber, colNumber, this, second));
                    tasks.add(task);
                }
            }

            List<ComputeResultDto> results = tasks.stream()
                    .map(forkJoinPool::invoke)
                    .collect(Collectors.toList());

            int[][] resultContent = new int[numRows][numCols];
            for (ComputeResultDto result : results) {
                resultContent[result.getRow()][result.getCol()] = result.getValue();
            }

            return new Matrix(numRows, numCols, resultContent);
        }

        public int getRows() {
            return rows;
        }

        public int getCols() {
            return cols;
        }

        public int getValue(int row, int col) {
            return content[row][col];
        }

        private int[][] fill(int rows, int cols) {
            int[][] content = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    content[i][j] = random.nextInt(100);
                }
            }
            return content;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    sb.append(content[i][j]).append(' ');
                }
                sb.append('\n');
            }
            return sb.toString();
        }
    }

    public static class ComputeResultItemTask extends RecursiveTask<ComputeResultDto> {
        private final ComputeParamsDto params;

        public ComputeResultItemTask(ComputeParamsDto params) {
            this.params = params;
        }

        @Override
        protected ComputeResultDto compute() {
            int value = 0;
            for (int k = 0; k < params.getMatrixA().getCols(); k++) {
                value += params.getMatrixA().getValue(params.getResultRow(), k) * params.getMatrixB().getValue(k, params.getResultCol());
            }
            return new ComputeResultDto(params.getResultRow(), params.getResultCol(), value);
        }
    }

    public static class ComputeParamsDto {
        private final int resultRow;
        private final int resultCol;
        private final Matrix matrixA;
        private final Matrix matrixB;

        public ComputeParamsDto(int resultRow, int resultCol, Matrix matrixA, Matrix matrixB) {
            this.resultRow = resultRow;
            this.resultCol = resultCol;
            this.matrixA = matrixA;
            this.matrixB = matrixB;
        }

        public int getResultRow() {
            return resultRow;
        }

        public int getResultCol() {
            return resultCol;
        }

        public Matrix getMatrixA() {
            return matrixA;
        }

        public Matrix getMatrixB() {
            return matrixB;
        }
    }

    public static class ComputeResultDto {
        private final int row;
        private final int col;
        private final int value;

        public ComputeResultDto(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public int getValue() {
            return value;
        }
    }
}
