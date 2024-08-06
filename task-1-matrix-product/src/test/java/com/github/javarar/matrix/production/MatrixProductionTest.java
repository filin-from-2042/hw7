package com.github.javarar.matrix.production;

import com.github.javarar.matrix.production.MatrixProduction.Matrix;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MatrixProductionTest {

    @DisplayName("Задание 2. Вычисление произведения квадратных матриц")
    @ParameterizedTest
    @MethodSource("matrixProducer")
    public void validateMatrixProduction(Matrix a, Matrix b) {
        MatrixProduction.Matrix result = MatrixProduction.product(a, b);

        int lastIndexRow = a.getRows() - 1;
        int lastIndexCol = b.getRows() - 1;

        int expectedValueAt00 = computeMatrixProductValue(a, b, 0, 0);
        int expectedValueAtLast = computeMatrixProductValue(a, b, lastIndexRow, lastIndexCol);

        assertEquals(expectedValueAt00, result.getValue(0, 0), "Value at [0][0] should be correct");
        assertEquals(expectedValueAtLast, result.getValue(lastIndexRow, lastIndexCol), "Value at [" + lastIndexRow + "][" + lastIndexCol + "] should be correct");

    }

    private int computeMatrixProductValue(MatrixProduction.Matrix matrixA, MatrixProduction.Matrix matrixB, int row, int col) {
        int sum = 0;
        for (int k = 0; k < matrixA.getCols(); k++) {
            sum += matrixA.getValue(row, k) * matrixB.getValue(k, col);
        }
        return sum;
    }


    private static Stream<Arguments> matrixProducer() {
        return Stream.of(
                Arguments.of(new Matrix(3,3), new Matrix(3,3)), // null strings should be considered blank
                Arguments.of(new Matrix(100,100), new Matrix(100,100)),
                Arguments.of(new Matrix(1000,1000), new Matrix(1000,1000))
        );
    }
}
