import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.plugin.filter.PlugInFilter;
import ij.*;

import java.awt.Color;


public class Normalized_Cumulative_Histogram implements PlugInFilter {
    float[] r_hist = new float[256];
    float[] g_hist = new float[256];
    float[] b_hist = new float[256];

    void setHistogram(ImageProcessor ip) {
        int r;
        int g;
        int b;
        
        int width = ip.getWidth();
        int height = ip.getHeight();
        int size = width * height;
        
        Color pix_color;
        
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y)
             {
                pix_color = new Color(ip.getPixel(x, y));

                r = pix_color.getRed();
                g = pix_color.getGreen();
                b = pix_color.getBlue();

                ++r_hist[r];
                ++g_hist[g];
                ++b_hist[b];
            }
        }

        //Normalized Histogram
        for (int x = 0; x < r_hist.length; x++) 
        {
            r_hist[x] /= size;
        }

        for (int x = 0; x < g_hist.length - 1; x++) 
        {
            g_hist[x] /= size;
        }

        for (int x = 0; x < b_hist.length - 1; x++) 
        {
            b_hist[x] /= size;
        }

        // Get cumulative normalized Histogram
        for (int x = 1; x < 256; x++) 
        {    
            r_hist[x] += r_hist[x - 1];
            g_hist[x] += g_hist[x - 1];
            b_hist[x] += b_hist[x - 1];
        }

    }

    void logHist(float[] hist)
    {
        for (int x = 0; x < hist.length; x++) {
            IJ.log( Integer.toString(x) + " : " +  Double.toString(hist[x]));
        }
    }

    public int setup(String args, ImagePlus im) {
        return DOES_RGB;
    }

    public void run(ImageProcessor ip) {
        setHistogram(ip);
        logHist(r_hist);
        logHist(b_hist);
        logHist(g_hist);
    }

}


