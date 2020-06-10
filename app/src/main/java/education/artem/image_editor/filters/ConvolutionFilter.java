package education.artem.image_editor.filters;

import android.graphics.Bitmap;

import java.nio.ByteBuffer;

public class ConvolutionFilter {

    private Bitmap sourceBitmap;

    public ConvolutionFilter(Bitmap sourceBitmap) {
        this.sourceBitmap = sourceBitmap;
    }

    public Bitmap convolutionFilter(Bitmap sourceBitmap,
                                    double[][] filterMatrix,
                                    double factor,
                                    int bias) {
        ByteBuffer pixelBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        ByteBuffer resultBuffer = ByteBuffer.allocate(sourceBitmap.getByteCount());
        int height = sourceBitmap.getHeight();
        int width = sourceBitmap.getWidth();
        int stride = sourceBitmap.getRowBytes();
        sourceBitmap.copyPixelsToBuffer(pixelBuffer);

        double blue;
        double green;
        double red;

        int filterWidth = filterMatrix[0].length;

        int filterOffset = (filterWidth - 1) / 2;
        int calcOffset;

        int byteOffset;
        double progress;
        int top = height - filterOffset;

        for (int offsetY = filterOffset; offsetY <
                height - filterOffset; offsetY++) {
            for (int offsetX = filterOffset; offsetX <
                    width - filterOffset; offsetX++) {
                blue = 0;
                green = 0;
                red = 0;

                byteOffset = offsetY *
                        stride +
                        offsetX * 4;

                for (int filterY = -filterOffset;
                     filterY <= filterOffset; filterY++) {
                    for (int filterX = -filterOffset;
                         filterX <= filterOffset; filterX++) {

                        calcOffset = byteOffset +
                                (filterX * 4) +
                                (filterY * stride);

                        blue += (double) (pixelBuffer.array()[calcOffset]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];

                        green += (double) (pixelBuffer.array()[calcOffset + 1]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];

                        red += (double) (pixelBuffer.array()[calcOffset + 2]) *
                                filterMatrix[filterY + filterOffset][filterX + filterOffset];
                    }
                }

//                blue = factor * blue + bias;
//                green = factor * green + bias;
//                red = factor * red + bias;

                blue = blue / factor + bias;
                green = green / factor + bias;
                red = red / factor + bias;

                if (blue > 255) {
                    blue = 255;
                } else if (blue < 0) {
                    blue = 0;
                }

                if (green > 255) {
                    green = 255;
                } else if (green < 0) {
                    green = 0;
                }

                if (red > 255) {
                    red = 255;
                } else if (red < 0) {
                    red = 0;
                }

                resultBuffer.array()[byteOffset] = (byte) (blue);
                resultBuffer.array()[byteOffset + 1] = (byte) (green);
                resultBuffer.array()[byteOffset + 2] = (byte) (red);
                resultBuffer.array()[byteOffset + 3] = (byte) 255;
            }
            progress = (double) offsetY / top * 100;
//            publishProgress((int) progress);
        }


        Bitmap resultBitmap = sourceBitmap.copy(sourceBitmap.getConfig(), true);

        resultBitmap.copyPixelsFromBuffer(resultBuffer);
        return resultBitmap;

    }
}
