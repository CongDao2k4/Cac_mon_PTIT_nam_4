package org.example.ImproveImageQuality;


import edu.princeton.cs.introcs.Picture;
import org.example.directoriesCreate.CreateFolderOrFile;
import org.example.operations.Matrix_Image;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/*****
 * UsePointOperator - cải thiện ảnh sử dụng toán tử điểm:
 * Phép toán xử lý ảnh có dạng: g(x,y) = T[f(x,y)]
 *  f(x,y) là ảnh đầu vào
 *  g(x,y) là ảnh đầu ra
 *  T là một số toán tử tác động lên các điểm ảnh xung quanh (x,y)
 * Nếu T chỉ tác động lên 1 điểm ảnh tại (x,y) thì T là hàm biến đổi mức xám hay toán tử xử lý điểm ảnh
 * Các toán tử xử lý điểm ảnh có dạng: s = T(r)
 * r: mức xám của ảnh đầu vào tại (x,y)
 * s: mức xám của ảnh đầu ra tại (x,y)
 *
 * MỘT SỐ TOÁN TỬ ĐIỂM ẢNH:
 * - Biến đổi âm bản
 * - Phân ngưỡng
 * - Một số hàm biến đổi mức xám cơ bản: tuyến tính, logarithm, hàm mũ.
 */
public class UsePointOperator {
    private static final Path OUTPUT_PATH = Paths.get("src","main","resources","chapter3");
    private static final int INTENSIVE_MAX = 255;
    private static final double THRESHOLD = 50.0;
    private static final double C = 255.0 / Math.log(1 + 255.0);;

    /*****
     * với đầu vào là ma trận pixel xám sau khi chuyển ảnh màu sang ảnh xám
     * Biến đổi âm bản :
     * s = T(r) = Intensive[max] - r
     * T là hàm biến đổi, r là iểm ảnh ban đầu, s là điểm ảnh sau đó.
     * @param gray_matrix ma trận xám
     * // @param intensive_max ngưỡng sáng alpha ừ 0 -> 255
     * @return
     */
    public static double[][] transformNegativeForm(double[][] gray_matrix /*, double intensive_max */) {
        return Arrays.stream(gray_matrix)
                .parallel()           // Xử lý song song các hàng
                .map(row -> Arrays.stream(row) // Biến đổi từng hàng
                        .map(pixel -> 255.0 - pixel) // Áp dụng x' = 255 - x
                        .toArray())
                .toArray(double[][]::new);
    }

    /*****
     * Phân ngưỡng :
     * s = 1.0 nếu r > thresold ; s = 0.0 nếu r <= thresold
     * với s là điểm ảnh sau đó, t là điểm ảnh ban đầu
     * thresold là ngưỡng sáng từ 0->255, THRESOLD càng nhỏ thì càng nhiều điểm màu trắng tinh
     * @param gray_matrix ma trận xám
     * @return
     */
    public static double[][] thresholding(double[][] gray_matrix /*, double thresold*/) {
        final double thresold = THRESHOLD;
        return Arrays.stream(gray_matrix)
                .parallel()           // Xử lý song song các hàng
                .map(row -> Arrays.stream(row) // Biến đổi từng hàng
                        // Sửa ở đây: Ánh xạ 1.0 thành 255.0
//                        .map(pixel -> (pixel > thresold) ? 1.0 : 0.0)
                        .map(pixel -> (pixel > thresold) ? 255.0 : 0.0)
                        .toArray())
                .toArray(double[][]::new);
    }

    /****
     * Biến đổi Logarithm với s = c * log(1 + r)
     * @param gray_matrix
     * @return
     */
    public static double[][] logarithTransform(double[][] gray_matrix /*, double C*/) {
        final double c = C;
        return Arrays.stream(gray_matrix)
                .parallel()           // Xử lý song song các hàng
                .map(row -> Arrays.stream(row) // Biến đổi từng hàng
                        .map(pixel -> c * Math.log(1.0 + pixel))
                        .toArray())
                .toArray(double[][]::new);
    }

    /****
     * Biến đổi hàm mũ hệ số y với s = c * r^y
     * @param gray_matrix
     * @return
     */
    public static double[][] expTransform(double[][] gray_matrix , double gamma) {
        final  double c = 255.0 / Math.pow(255.0, gamma);
        return Arrays.stream(gray_matrix)
                .parallel()           // Xử lý song song các hàng
                .map(row -> Arrays.stream(row) // Biến đổi từng hàng
                        .map(pixel -> c * Math.pow(pixel, gamma))
                        .toArray())
                .toArray(double[][]::new);
    }

    public static void showImage(double[][] matrix) {
        Picture pic = Matrix_Image.createImageFromMatrix(matrix);
//        Path savelink = OUTPUT_PATH;
//        CreateFolderOrFile.createFolder(savelink);
//        savelink = savelink.resolve("ouput.png");
//        pic.save(savelink.toString());
        pic.show();
    }

    public static void main(String[] args) {
        CreateFolderOrFile.createFolder(OUTPUT_PATH);
//        showImage(transformNegativeForm(
//            Matrix_Image.createGrayMatrixFromImage(
//                 OUTPUT_PATH.resolve("negativeForm.png").toString())
//            )
//        );
//        showImage(thresholding(
//                        Matrix_Image.createGrayMatrixFromImage(
//                                OUTPUT_PATH.resolve("thresolding.png").toString())
//                )
//        );
//        showImage(logarithTransform(
//                        Matrix_Image.createGrayMatrixFromImage(
//                                OUTPUT_PATH.resolve("logarith.png").toString())
//                )
//        );
        showImage(
            expTransform(
                   Matrix_Image.createGrayMatrixFromImage(OUTPUT_PATH.resolve("exp2.png").toString())
                    , 5.0
            )
        );
    }
}

