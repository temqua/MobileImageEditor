package education.artem.contrastchangeandroid;

public class Matrix {

    public static double[][] Laplacian3x3 = new double[][]{
            {-1, -1, -1,},
            {-1, 8, -1,},
            {-1, -1, -1,}
    };

    public static double[][] Laplacian5x5 = new double[][]{
            {-1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1,},
            {-1, -1, 24, -1, -1,},
            {-1, -1, -1, -1, -1,},
            {-1, -1, -1, -1, -1}
    };

    public static double[][] Sobel3x3Horizontal = new double[][]{
            {-1, 0, 1,},
            {-2, 0, 2,},
            {-1, 0, 1,}
    };

    public static double[][] Sobel3x3Vertical = new double[][]{
            {1, 2, 1,},
            {0, 0, 0,},
            {-1, -2, -1,}
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

    public static final double[][] BLUR = new double[][]{
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
    };
    public static final double[][] GAUSSIAN_BLUR = new double[][]{
            {1, 4, 6, 4, 1},
            {4, 16, 24, 16, 4},
            {6, 24, 36, 24, 6},
            {4, 16, 24, 16, 4},
            {1, 4, 6, 4, 1}
    };
    public static final double[][] SHARPEN = new double[][]{
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
    };
    public static final double[][] EMBOSS = new double[][]{
            {-2, -1, 0},
            {-1, 1, 1},
            {0, 1, 2}
    };

    public static double[][] IDENTITY = new double[][]{
            {0, 0, 0},
            {0, 1, 0},
            {0, 0, 0}
    };
}
