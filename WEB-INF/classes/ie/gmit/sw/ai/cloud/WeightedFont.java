package ie.gmit.sw.ai.cloud;

import java.util.Arrays;

public class WeightedFont {
	private static final double MAX_FONT_SIZE = 72.00d;
	private static final double MIN_FONT_SIZE = 8.00d;
	
	public WordFrequency[] getFontSizes(WordFrequency[] words) {
		//Get the max and min frequencies and scale these to a natural log scale to smooth out the range  
		double max = Math.log(Arrays.stream(words).mapToDouble(n -> n.getFrequency()).max().getAsDouble());
		double min = Math.log(Arrays.stream(words).mapToDouble(n -> n.getFrequency()).min().getAsDouble());

		for (WordFrequency wf : words) {
			//Use a log scale and word frequency to compute the font size for the word
			wf.setFontSize((int)getScaledFontSize(Math.log(wf.getFrequency()), min, max));
		}
		return words;
	}
	
	//Compute the initial font size for the word 
	public double getScaledFontSize(double value, double min, double max){
		double scaled = (MAX_FONT_SIZE * (value - min))/(max-min);
		return (scaled > MIN_FONT_SIZE) ? scaled : MIN_FONT_SIZE;
	}
}