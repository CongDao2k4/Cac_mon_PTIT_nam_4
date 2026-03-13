package org.example.ImproveImageQuality;

import edu.princeton.cs.introcs.Picture;

import java.awt.*;

public class AverageWeightedFilter {
    private static int[][] WEIGHT = {{1, 2, 1}, {2, 4, 2}, {1, 2, 1}};
    private static int K_NEIGHBOR;
    private static double THETA;
    private static double[][] ORG_MATRIX;

    public AverageWeightedFilter(String linkImage) {
        takeMatrix(linkImage);
    }
    private void takeMatrix(String linkImage) {
        Picture picture = new Picture(linkImage);
//        StdOut.print("Type the size of window: ");
//        K_NEIGHBOR = StdIn.readInt();
//        StdOut.print("Type the measure score Theta to compare with AVG: ");
//        THETA = StdIn.readDouble();
        ORG_MATRIX = new double[picture.width()][picture.height()];
        for(int w = 0; w< picture.width(); w++)
            for(int h =0; h< picture.height(); h++) {
                ORG_MATRIX[w][h] = 0.299 * picture.get(w,h).getRed() + 0.587 * picture.get(w,h).getGreen()
                        + 0.114 * picture.get(w,h).getBlue();
            }
        return;
    }

    //Tìm K_NEIGHBOR gần giá trị nhất với điểm pixel P hiện tại trong cửa sổ k x k với P là điểm trung tâm
    // Sau đó so sánh với Theta
    private static double calculateAverage(int row, int col, int k, int theta) {
        return 0;
    }

    private Picture createGrayPicture() {
        double[][] m = ORG_MATRIX;
        Picture picture = new Picture(m.length, m[0].length);
        for(int w = 0; w< picture.width(); w++)
            for(int h =0; h< picture.height(); h++) {
                int gray = (int) Math.round(m[w][h]);
                // clamp về [0,255]
                gray = Math.max(0, Math.min(255, gray));
                Color c = new Color(gray, gray, gray);
                picture.set(w, h, c);
            }
        return picture;
    }

    public static void main(String[] args) {
        AverageWeightedFilter a = new AverageWeightedFilter("src/main/resources/thresolding.png");
        a.createGrayPicture().save("src/main/resources/imggray.png");
        a.createGrayPicture().show();
    }
}
