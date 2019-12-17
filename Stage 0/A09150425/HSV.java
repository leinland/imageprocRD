import ij.ImagePlus;
import ij.IJ;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import ij.plugin.filter.PlugInFilter;
import java.awt.Color; 
import java.util.*; 


public class HSV_Specification implements PlugInFilter {
	ImagePlus inputImage;
	ImagePlus referenceImage;
	ImageProcessor refImageProcessor;

    Map <String,Integer> hue = new TreeMap <String,Integer>();
    Map <String,Integer> satauration = new TreeMap <String,Integer>();
    Map <String,Integer> val = new TreeMap <String,Integer>();

    Map <String,Integer> hue_ref = new TreeMap <String,Integer>();
    Map <String,Integer> saturation_ref = new TreeMap <String,Integer>();
    Map <String,Integer> val_ref = new TreeMap <String,Integer>();

    Map <String,Float> hue_cum = new TreeMap <String,Float>();
    Map <String,Float> saturation_cum = new TreeMap <String,Float>();
    Map <String,Float> val_cum = new TreeMap <String,Float>();

    Map <String,Float> hue_cum_ref = new TreeMap <String,Float>();
    Map <String,Float> sat_cum__ref = new TreeMap <String,Float>();
    Map <String,Float> val_cum_ref = new TreeMap <String,Float>();
                        
    public int setup(String args, ImagePlus im) 
    {
		inputImage = im;
		return DOES_RGB;
	}

	public void run(ImageProcessor inputIP) {
		getRefImageDialog();
		computeHistogram(inputIP, hue, satauration, val);
		computeHistogram(refImageProcessor, hue_ref, saturation_ref, val_ref);
		computeCumulativeHist(inputIP, hue_cum, saturation_cum, val_cum, hue, satauration, val);
		computeCumulativeHist(refImageProcessor, hue_cum_ref, sat_cum__ref, val_cum_ref, hue_ref,saturation_ref, val_ref);
        displayHistograms();//   Uncomment for logging the histogram
        specify(inputIP);
	}

	private void getRefImageDialog() {
		referenceImage = IJ.openImage();
		refImageProcessor = referenceImage.getProcessor();
	}


	private void computeHistogram(ImageProcessor processor,Map<String,Integer> hue, Map<String,Integer> sat, Map<String,Integer> vl) {
		int height = processor.getHeight(), width = processor.getWidth();
		Color pixel;
		for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				pixel = new Color(processor.getPixel(u, v));
				int r = pixel.getRed();
				int g = pixel.getGreen();
				int b = pixel.getBlue();
                float[] hsv = new float[3];
                Color.RGBtoHSB(r,g,b,hsv);
                String h = Float.toString(hsv[0]);
                String s = Float.toString(hsv[1]);
                String value = Float.toString(hsv[2]);
                if(!hue.containsKey(h)){
                    hue.put(h, 0);
                }
                 else {
                    int val = hue.get(h);
                    ++val;
                    hue.replace(h, val);
                }

                if(!sat.containsKey(s)){
                    sat.put(s, 0);
                }
                 else {
                    int val = sat.get(s);
                    ++val;
                    sat.replace(s, val);
                }

                if(!vl.containsKey(value)){
                    vl.put(value, 0);
                }
                 else {
                    int val = vl.get(value);
                    ++val;
                    vl.replace(value, val);
                }
			}
		}
	}

    public void normalize(Map<String,Float> map, float val) {
        for (Map.Entry<String,Float> entry : map.entrySet()) {
            map.replace(entry.getKey(), entry.getValue()/val);
        }
    }

	private void computeCumulativeHist(ImageProcessor processor,Map<String,Float> cum_hue, Map<String,Float> cum_sat,Map<String,Float> cum_vl,
    Map<String,Integer> hue, Map<String,Integer> sat, Map<String,Integer> vl) {
        String[] result_hue = new String[hue.keySet().size()];
        hue.keySet().toArray(result_hue);

        String[] result_sat = new String[sat.keySet().size()];
        sat.keySet().toArray(result_sat);

        String[] result_vl = new String[vl.keySet().size()];
        vl.keySet().toArray(result_vl);

        cum_hue.put(result_hue[0], (float)hue.get(result_hue[0]));
        for(int ind = 1; ind < result_hue.length; ++ ind) {
            float test =  cum_hue.get(result_hue[ind-1]) + hue.get(result_hue[ind]);
            cum_hue.put(result_hue[ind], test);
        }

        cum_sat.put(result_sat[0], (float)sat.get(result_sat[0]));
        for(int ind = 1; ind < result_sat.length; ++ ind) {
            float test =  cum_sat.get(result_sat[ind-1]) + sat.get(result_sat[ind]);
            cum_sat.put(result_sat[ind], test);
        }
    
        cum_vl.put(result_vl[0], (float)vl.get(result_vl[0]));
        for(int ind = 1; ind < result_vl.length; ++ ind) {
            float test =  cum_vl.get(result_vl[ind-1]) + vl.get(result_vl[ind]);
            cum_vl.put(result_vl[ind], test);
        }

        normalize(cum_hue, (float) processor.getHeight() * processor.getWidth());
        normalize(cum_sat, (float) processor.getHeight() * processor.getWidth());
        normalize(cum_vl, (float) processor.getHeight() * processor.getWidth());    
	}

    private void displayHistograms() {
        for (Map.Entry<String,Float> entry : hue_cum.entrySet()) {
             IJ.log(entry.getKey() + " : " + Float.toString(entry.getValue()));
        }
    }

     private String getMapping(double value, Map<String, Float> histogram) {
        String key = "0.0";
        
        for (Map.Entry<String, Float> entry : histogram.entrySet()) {
            if (value < histogram.get(entry.getKey()))
            {
                return entry.getKey();
            }
            
            key = entry.getKey();
        }
        return key;
    }

    private void specify(ImageProcessor processor) 
    {
        int height = processor.getHeight(), width = processor.getWidth();
		Color pixel;
        for (int u = 0; u < width; u++) {
			for (int v = 0; v < height; v++) {
				pixel = new Color(processor.getPixel(u, v));
				int r = pixel.getRed();
				int g = pixel.getGreen();
				int b = pixel.getBlue();
                float[] hsv = new float[3];
                Color.RGBtoHSB(r,g,b,hsv);
                String h =  getMapping(hue_cum.get(Float.toString(hsv[0])), hue_cum_ref);
                String s =  getMapping(saturation_cum.get(Float.toString(hsv[1])), sat_cum__ref);
                String val =  getMapping(val_cum.get(Float.toString(hsv[2])), val_cum_ref);
                int rgb = Color.HSBtoRGB(Float.valueOf(h), Float.valueOf(s), Float.valueOf(val));
                processor.putPixel(u, v, rgb);
		    }
	    }
    }
}