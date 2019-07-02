package education.artem.contrastchangeandroid;

public class Matrix {

    public static double[][] Laplacian3x3 = new double[][]{
            {-1, -1, -1,},
            {-1, 8, -1,},
            {-1, -1, -1,}
    };

    public static double[][] Prewitt3x3Horizontal = new double[][]{
            {-1, 0, 1,},
            {-1, 0, 1,},
            {-1, 0, 1,}
    };

    public static double[][] Prewitt3x3Vertical = new double[][]{
            {1, 1, 1,},
            {0, 0, 0,},
            {-1, -1, -1,}
    };
}
