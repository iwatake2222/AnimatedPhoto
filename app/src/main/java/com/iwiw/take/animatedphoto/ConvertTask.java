package com.iwiw.take.animatedphoto;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Message;
import android.view.View;


import com.iwiw.take.animatedphoto.Utility;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * Created by takeshi on 12/06/16.
 * Note: Overwrite outputImage every h, s and v
 * In order to overwrite, base value(color) must be 0x00. However, 0x00 is black so looks bad and cannot show edge
 * So, when showing progress use converted image. so that every fill color is also converted
 */
public class ConvertTask extends AsyncTaskLoader<Bitmap> {
    static boolean m_isCvLoaded = false;

    static {
        if (!OpenCVLoader.initDebug()) {
            Utility.logError("Unable to open OpenCV");
            m_isCvLoaded = false;
        }
        m_isCvLoaded = true;
    }

    Uri m_orgUri;
    int m_size;
    int m_numH, m_numS, m_numV;
    int m_blurFilterSize;
    boolean m_isShowEdge;
    Context m_context;

    double m_gapH, m_gapS, m_gapV;


    public ConvertTask(Context context, Uri orgUri, int size, int numH, int numS, int numV, int blur, boolean isShowEdge) {
        super(context);
        m_context = context;
        m_orgUri = orgUri;
        m_size = size;
        m_numH = numH;
        m_numS = numS;
        m_numV = numV;
        m_blurFilterSize = blur;
        m_isShowEdge = isShowEdge;
        // (0 ~ 100) to (210 ~ 10)

        m_gapH = 180.0f / m_numH;
        m_gapS = 256.0f / m_numS;
        m_gapV = 256.0f / m_numV;
    }

    @Override
    public Bitmap loadInBackground() {
        if (!m_isCvLoaded) return null;

        /* read original image file and resize */
        Mat inputImage = Imgcodecs.imread(m_orgUri.getPath());
        Size size;
        if (inputImage.width() > inputImage.height()) {
            if (inputImage.width() > m_size) {
                size = new Size(m_size, m_size * inputImage.height() / inputImage.width());
                Imgproc.resize(inputImage, inputImage, size);
            }
        } else {
            if (inputImage.height() > m_size) {
                size = new Size(m_size * inputImage.width() / inputImage.height(), m_size);
                Imgproc.resize(inputImage, inputImage, size);
            }
        }
        int width = inputImage.width();
        int height = inputImage.height();

        /* create output image, blur image, hsv format image, grayscaled image */
        Mat outputImage = Mat.zeros(height, width, CvType.CV_8UC3);
//        Mat outputImage = new Mat(height, width, CvType.CV_8UC3, Scalar.all(0xff));
        Mat hsvImage = new Mat();
        Mat grayImage = new Mat();
        Mat edgeImage = new Mat();
        Imgproc.cvtColor(inputImage, grayImage, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(inputImage, inputImage, m_blurFilterSize);
        Imgproc.cvtColor(inputImage, hsvImage, Imgproc.COLOR_BGR2HSV);

        /* add edge */
        if (m_isShowEdge) {
            Imgproc.Canny(grayImage, edgeImage, Consts.FILTER_EDGE_LOWER, Consts.FILTER_EDGE_UPPER);
            Imgproc.cvtColor(edgeImage, outputImage, Imgproc.COLOR_GRAY2BGR);
        }

        /* Reduce HSV */
        Mat mask = new Mat();
        Mat filledImage = new Mat(height, width, CvType.CV_8UC3);
        Mat tempImage = new Mat();
        int hsvRange[][] = new int[3][3];
        for (int h = 0; h < m_numH; h++) {
            for (int s = 0; s < m_numS; s++) {
                for (int v = 0; v < m_numV; v++) {
                    createHSVRange(h, s, v, hsvRange);
                    Core.inRange(hsvImage,
                            new Scalar(hsvRange[0][0], hsvRange[0][1], hsvRange[0][2]),
                            new Scalar(hsvRange[1][0], hsvRange[1][1], hsvRange[1][2]),
                            mask);
                    // Scalar(hsvRange[2][] is color to be filled in hsv. So, convert it inverted RGB
                    filledImage.setTo(convHSV2RGB_inv(hsvRange[2][0], hsvRange[2][1], hsvRange[2][2]));
                    Core.bitwise_or(filledImage, outputImage, outputImage, mask);
                }
            }
//            Utility.logDebug(Integer.toString(h));
            Message msgBitmap = new Message();
            Bitmap progressBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Core.bitwise_not(outputImage, tempImage);
            Utils.matToBitmap(tempImage, progressBitmap);
            msgBitmap.obj = progressBitmap;
            ((ViewActivity) m_context).m_bitmapProgressHandler.sendMessage(msgBitmap);
            Message msgProgress = new Message();
            msgProgress.obj = 100 * (h + 1) / m_numH;
            ((ViewActivity) m_context).m_progressHandler.sendMessage(msgProgress);
        }
        tempImage = null;
        filledImage = null;
        mask = null;

        /* Generate empty Bitmap and convert Mat into generated bitmap */
        Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Core.bitwise_not(outputImage, outputImage);
        Utils.matToBitmap(outputImage, outputBitmap);
        return outputBitmap;
    }


    private void createHSVRange(int h, int s, int v, int hsvRange[][]) {
        int hHigh = (h != m_numH - 1) ? (int) (m_gapH * (h + 1) - 1) : 180;
        int sHigh = (s != m_numS - 1) ? (int) (m_gapS * (s + 1) - 1) : 255;
        int vHigh = (v != m_numV - 1) ? (int) (m_gapV * (v + 1) - 1) : 255;
        hsvRange[0][0] = (int) (m_gapH * h);
        hsvRange[0][1] = (int) (m_gapS * s);
        hsvRange[0][2] = (int) (m_gapV * v);
        hsvRange[1][0] = hHigh;
        hsvRange[1][1] = sHigh;
        hsvRange[1][2] = vHigh;
        // use Upper or Lower value for fill color to make picture more vivid
        hsvRange[2][0] = (hsvRange[0][0] + hsvRange[1][0]) / 2;
//        if (s <= (m_numS-1) * 0.2) {
        if (s == 0) {   // to make "white" "white". otherwise "white" becomes "mustle color"
            hsvRange[2][1] = hsvRange[0][1];
        } else if (s > (m_numS - 1) * 0.8) {
            hsvRange[2][1] = hsvRange[1][1];
        } else {
            hsvRange[2][1] = (hsvRange[0][1] + hsvRange[1][1]) / 2;
        }
        if (v <= (m_numV - 1) * 0.2) {
            hsvRange[2][2] = hsvRange[0][2];
        } else if (v > (m_numV - 1) * 0.8) {
            hsvRange[2][2] = hsvRange[1][2];
        } else {
            hsvRange[2][2] = (hsvRange[0][2] + hsvRange[1][2]) / 2;
        }
//        Utility.logDebug(
//                Integer.toString(hsvRange[0][0]) + " " + Integer.toString(hsvRange[0][1]) + " " + Integer.toString(hsvRange[0][2]) + ", " +
//                Integer.toString(hsvRange[1][0]) + " " + Integer.toString(hsvRange[1][1]) + " " + Integer.toString(hsvRange[1][2]) + ", " +
//                Integer.toString(hsvRange[2][0]) + " " + Integer.toString(hsvRange[2][1]) + " " + Integer.toString(hsvRange[2][2])
//        );
    }

    private Scalar convHSV2RGB_inv(int h, int s, int v) {
        Scalar rgb = convHSV2RGB(h, s, v);
        rgb.val[0] = 255 - rgb.val[0];
        rgb.val[1] = 255 - rgb.val[1];
        rgb.val[2] = 255 - rgb.val[2];
        return rgb;
    }

    private Scalar convHSV2RGB(int h, int s, int v) {
        float f;
        int i, p, q, t;
        int r = 0, g = 0, b = 0;
        h *= 2; // make the range 0 to 360

        i = (int) Math.floor(h / 60.0f) % 6;
        f = (h / 60.0f) - (float) Math.floor(h / 60.0f);
        p = Math.round(v * (1.0f - (s / 255.0f)));
        q = Math.round(v * (1.0f - (s / 255.0f) * f));
        t = Math.round(v * (1.0f - (s / 255.0f) * (1.0f - f)));

        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
        }

        return new Scalar(r, g, b);
    }

}
