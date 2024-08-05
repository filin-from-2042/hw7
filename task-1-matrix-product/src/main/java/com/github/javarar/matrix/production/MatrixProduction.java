package com.github.javarar.matrix.production;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

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
        private static final ForkJoinPool commonPool = ForkJoinPool.commonPool();
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

            // Проверка на возможность перемножения матриц
            if (this.getCols() != second.getRows()) {
                throw new IllegalArgumentException("Размеры матриц неверны");
            }

            List<ComputeResultItemTask> tasks = new ArrayList<>();
            // Перемножение матриц
            for (int i = 0; i < this.getRows(); i++) {
                for (int j = 0; j < second.getCols(); j++) {
                    for (int k = 0; k < this.getCols(); k++) {
                        ComputeResultItemTask task = new ComputeResultItemTask(new ComputeParamsDto(i, j, this.getValue(i, k), second.getValue(k, j)));
                        tasks.add(task);
                        commonPool.invoke(new ComputeResultItemTask(new ComputeParamsDto(i, j, this.getValue(i, k), second.getValue(k, j))));
                    }
                }
            }

            int[][] resultContent = new int[this.getRows()][second.getCols()];
            for (ComputeResultItemTask task : tasks) {
                ComputeResultDto result = task.join();
                resultContent[result.getRow()][result.getCol()] = result.getValue();
            }

            return new Matrix(this.rows, this.cols, resultContent);
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
            return new ComputeResultDto(params.getResultRow(), params.getResultCol(), params.getFirstValue() * params.getSecondValue());
        }
    }


    public static class ComputeParamsDto {
        private final int resultRow;
        private final int resultCol;
        private final int firstValue;
        private final int secondValue;

        public ComputeParamsDto(int resultRow, int resultCol, int firstValue, int secondValue) {
            this.resultRow = resultRow;
            this.resultCol = resultCol;
            this.firstValue = firstValue;
            this.secondValue = secondValue;
        }

        public int getResultRow() {
            return resultRow;
        }

        public int getResultCol() {
            return resultCol;
        }

        public int getFirstValue() {
            return firstValue;
        }

        public int getSecondValue() {
            return secondValue;
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
