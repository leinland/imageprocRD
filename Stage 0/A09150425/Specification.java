import ij.ImagePlus;
import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import ij.plugin.filter.PlugInFilter;
import java.awt.Color;

public class Specification implements PlugInFilter {
    
    ImagePlus inputImage;
	ImagePlus referenceImage;
	ImageProcessor refImageProcessor;
	int hist[][] = new int[3][256];
	int refHist[][] = new int[3][256];
	double cumulativeHist[][] = new double[3][256];
	double refCumulativeHist[][] = new double[3][256];
	int specificationMapping[][] = new int[3][256];


	public int setup(String args, ImagePlus im) {
		inputImage = im;
		return DOES_RGB;
	}

	public void run(ImageProcessor inputIP) {
		getRefImageDialog();
		computeHistogram(inputIP, hist);
		computeHistogram(refImageProcessor, refHist);
		computeCumulativeHist(inputIP, hist, cumulativeHist);
		computeCumulativeHist(refImageProcessor, refHist, refCumulativeHist);
		getSpecificationMapping();
		specification(inputIP);
	}

	private void getRefImageDialog() {
		referenceImage = IJ.openImage();
		refImageProcessor = referenceImage.getProcessor();
	}

	private void computeHistogram(ImageProcessor processor, int[][] destination) {
		int height = processor.getHeight(), width = processor.getWidth();
		Color pixel;
		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				pixel = new Color(processor.getPixel(u, v));
				int r = pixel.getRed();
				int g = pixel.getGreen();
				int b = pixel.getBlue();

				destination[0][r]++;
				destination[1][g]++;
				destination[2][b]++;
			}
		}
	}

	private void computeCumulativeHist(ImageProcessor processor, int[][] hist, double[][] destination) {
		for (int channel = 0; channel < 3; channel++) {
			destination[channel][0] = hist[channel][0];
			for (int intensity = 1; intensity < 256; intensity++) {
				destination[channel][intensity] =
						destination[channel][intensity - 1] + hist[channel][intensity];
			}

			for (int intensity = 0; intensity < 256; intensity++) {
				destination[channel][intensity] /=
						(double) processor.getHeight() * processor.getWidth();
			}
		}
	}

	private void getSpecificationMapping() {
		for (int channel = 0; channel < 3; channel++) {
			for (int intensity = 0; intensity < 256; intensity++) {
				int newIntensity = 255;
				do {
					specificationMapping[channel][intensity] = newIntensity;
					newIntensity--;
				} while (
					newIntensity >= 0 &&
						cumulativeHist[channel][intensity] <= refCumulativeHist[channel][newIntensity]
				);
			}
		}
	}

	private void specification(ImageProcessor processor) {
		int height = processor.getHeight(), width = processor.getWidth();
		ImageProcessor outputIP = new ColorProcessor(width, height);
		Color pixel;

		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				pixel = new Color(processor.getPixel(u, v));
				int newR = specificationMapping[0][pixel.getRed()];
				int newG = specificationMapping[1][pixel.getGreen()];
				int newB = specificationMapping[2][pixel.getBlue()];

				outputIP.putPixel(u, v, newR * 65536 + newG * 256 + newB);
			}
		}

		(new ImagePlus("balanced_all_" + inputImage.getShortTitle(), outputIP)).show();
	}
}