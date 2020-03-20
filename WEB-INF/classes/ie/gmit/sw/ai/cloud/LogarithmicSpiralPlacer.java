package ie.gmit.sw.ai.cloud;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;

public class LogarithmicSpiralPlacer {
	private Random rand = new Random(); //Random int generator for colours	
	private Graphics g = null; //The "canvas" to draw the word cloud on
	private BufferedImage img = null; //Rasterises the "canvas" to a PNG
	private java.util.List<Rectangle> placed = new ArrayList<>(); //The list of placed words
	private CollisionDetector detector = new CollisionDetector(); //Detects overlapping words
	private int width = 1600; //Image width. The bigger the canvas, the easier it is to place a word.
	private int height = 1000; //Image height	
	private int turn = 29; //The weight of the turn in the spiral

	public LogarithmicSpiralPlacer(int w, int h) {
		this.width = w;
		this.height = h;
		img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		g = img.getGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
	}
	
	/*
	 * The basic algorithm for placing a word is taken from Wordle (see http://www.wordle.net).
	 * This implementation uses a logarithmic spiral to place the word. See 
	 * https://en.wikipedia.org/wiki/Logarithmic_spiral
	 *
	 * Count the frequency of occurrence of each word and ignore ant commonly occurring words
	 * that are listed in an "ignore words" set.  Sort the set of words in descending order by
	 * their frequency. Only retain the top n user-specified words . Assign each word a font 
	 * size proportional to its frequency of occurrence. Paint the word onto a canvas using a 
	 * Logarithmic or Archimedean sprial (Spira Mirabilis) as follows: 
     *
	 *		Place the word where it wants to be
	 *		While it intersects any of previously placed word
     *			Move it one step along an ever-increasing spiral
	 *
	 */
	public void place(WordFrequency wf) {
		int i = width / 2; //Get the horizontal centre
		int j = height / 2; //Get the vertical centre
		int k = 1; //Step to move along spiral
		
		Font font = new Font("Tahoma", 0, wf.getFontSize()); //Create a font with a size proportional to the word frequency
		g.setColor(new Color(rand.nextInt(0xFFFFFF))); //Set the colour of the graphics "brush"
		g.setFont(font); //Set the font of the graphics "brush"
		
		//Get the "size" of the word string as a rectangle
		Rectangle2D bounds = this.g.getFontMetrics(font).getStringBounds(wf.getWord(), g); 
		
		//Start with the word placed at the centre of the spiral
		Rectangle word = new Rectangle(i, j - (int) (bounds.getHeight() * 0.8d), (int) bounds.getWidth(), (int) bounds.getHeight()); 
		
		//If the word collides with any existing words, move it along the spiral
		while (detector.collides(word, placed)) { 
			int l = k * turn % 360;
			double d = k * 0.1d;
			int x = (int) Math.round(i + d * Math.cos(l * Math.PI / 180.0d));
			int y = (int) Math.round(j + d * Math.sin(l * Math.PI / 180.0d));
			i = x;
			j = y;
			word = new Rectangle(i, j - (int) (bounds.getHeight() * 0.8d), (int) bounds.getWidth(), (int) bounds.getHeight());
			k++;
		}
		 
		g.drawString(wf.getWord(), i, j);//Draw the word on the graphics canvas 
		placed.add(word); //Add the word to the list of placed words
	}
	
	//Clean up the graphics context (close streams). Can only be done once...
	public BufferedImage getImage() {
		g.dispose(); 
		return img;
	}
}