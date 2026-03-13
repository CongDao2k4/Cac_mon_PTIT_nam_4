package org.example.operations;

import edu.princeton.cs.introcs.Picture;

import java.awt.*;
import java.util.Arrays;

public class Matrix_Image {
    public static Picture createImageFromMatrix(double[][] matrix) {
        Picture pic = new Picture(matrix.length, matrix[0].length);
        for(int i = 0; i< matrix.length; i++)
            for(int j =0; j< matrix[0].length; j++) {

                int grayValue = (int) Math.round(matrix[i][j]);
                grayValue = Math.max(0, Math.min(255, grayValue));

                pic.set( i , j ,
                        new Color(grayValue, grayValue, grayValue) // <-- Sửa tại đây
                );
            }
        return pic;
    }

    public static double[][] createGrayMatrixFromImage(String linkImage) {
        Picture picture = new Picture(linkImage);
        double[][] matrix = new double[picture.width()][picture.height()];
        for(int w = 0; w< picture.width(); w++)
            for(int h =0; h< picture.height(); h++) {
                matrix[w][h] = 0.299 * picture.get(w,h).getRed() + 0.587 * picture.get(w,h).getGreen()
                        + 0.114 * picture.get(w,h).getBlue();
            }
        return matrix;
    }

    public static Object[][] createMatrixFromImage(String linkImage) {
        Picture picture = new Picture(linkImage);
        Object[][] matrix = new Object[picture.width()][picture.height()];
        for(int w = 0; w< picture.width(); w++)
            for(int h =0; h< picture.height(); h++) {
                matrix[w][h] = new int[]{picture.get(w, h).getRed(), picture.get(w, h).getGreen(), picture.get(w, h).getBlue()};
            }
        return matrix;
    }
}
