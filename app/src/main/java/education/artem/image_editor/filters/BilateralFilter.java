package education.artem.image_editor.filters;

public class BilateralFilter {
    private float distanceSigma;
    private float intensitySigma;
    private int kernelSize;

    // It is filled by computing Gaussian function depend on kernelSize.
    private float[][] gaussianKernelMatrix;

    // It is filled by computing intensity part of bilateral function.
    private float[] intensityVector;

    public BilateralFilter(float distanceSigma, float intensitySigma) {
        this.distanceSigma = distanceSigma;
        this.intensitySigma = intensitySigma;

        // Using 3σ rule for filter size. Addition performing to kernelSize will be an odd number.
        this.kernelSize = (int) Math.floor(6 * distanceSigma) + 1;
        createGaussianKernel();
        createIntensityVector();
    }

    /**
     * Gaussian part of bilateral filter. It will be calculated in full formula.
     *
     * @param x Gaussian parameter X
     * @param y Gaussian parameter Y
     * @return Gaussian value
     */
    private float gaussianFunction(int x, int y) {
        return (float) Math.exp(-(x * x + y * y) / (2 * distanceSigma * distanceSigma));
    }

    /**
     * Calculate Gaussian kernel for kernelSize range.
     */
    private void createGaussianKernel() {
        gaussianKernelMatrix = new float[kernelSize][kernelSize];
        int halfKernelSize = (int) Math.floor(kernelSize / 2);

        for (int i = -halfKernelSize; i < halfKernelSize + 1; i++) {
            for (int j = -halfKernelSize; j < halfKernelSize + 1; j++) {
                gaussianKernelMatrix[i + halfKernelSize][j + halfKernelSize] = gaussianFunction(i, j);
            }
        }
    }


    /**
     * Calculate intensity vector for performance reason.
     * Test shows that this approach gives fourfold performance improvement.
     */
    private void createIntensityVector() {

        // It needs to increase performance. Compute intensity difference for each possible value.
        // There are 442 values since filter measure intensity difference as
        // √((R2 - R1)^2 + (G2 - G1)^2 + (B2 - B1)^2). So, maximal value is √(255^2 + 255^2 + 255^2). That is 442.
        intensityVector = new float[442];

        for (int i = 0; i < intensityVector.length; i++) {
            intensityVector[i] = (float) Math.exp(-((i) / (2 * intensitySigma * intensitySigma)));
        }
    }

    /**
     * Fast way to compute intensity difference. But it is not as precise as transfer to CIELAB color space.
     *
     * @param firstColor  Subtrahend
     * @param secondColor Subtractor
     * @return Difference between intensity of colors
     */
    public int getIntensityDifference(int firstColor, int secondColor) {
        int rDifference = (secondColor >> 16 & 0xFF) - (firstColor >> 16) & 0xFF;
        int gDifference = (secondColor >> 8 & 0xFF) - (firstColor >> 8) & 0xFF;
        int bDifference = (secondColor & 0xFF) - (firstColor & 0xFF);

        // Calculate intensity difference. It is used in not Gaussian part of bilateral filter formula.
        return (int) (Math.sqrt(rDifference * rDifference + gDifference * gDifference +
                bDifference * bDifference));
    }

    public float[][] getGaussianKernelMatrix() {
        return gaussianKernelMatrix;
    }

    public float[] getIntensityVector() {
        return intensityVector;
    }

    public int getKernelSize() {
        return kernelSize;
    }
}
