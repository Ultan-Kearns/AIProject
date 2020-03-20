package ie.gmit.sw;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;

import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;

import ie.gmit.sw.ai.cloud.LogarithmicSpiralPlacer;
import ie.gmit.sw.ai.cloud.WeightedFont;
import ie.gmit.sw.ai.cloud.WordFrequency;

/*
 * -------------------------------------------------------------------------------------------------------------------
 * PLEASE READ THE FOLLOWING CAREFULLY. MOST OF THE "ISSUES" STUDENTS HAVE WITH DEPLOYMENT ARISE FROM NOT READING
 * AND FOLLOWING THE INSTRUCTIONS BELOW.
 * -------------------------------------------------------------------------------------------------------------------
 *
 * To compile this servlet, open a command prompt in the web application directory and execute the following commands:
 *
 * Linux/Mac													Windows
 * ---------													---------	
 * cd WEB-INF/classes/											cd WEB-INF\classes\
 * javac -cp .:$TOMCAT_HOME/lib/* ie/gmit/sw/*.java				javac -cp .:%TOMCAT_HOME%/lib/* ie/gmit/sw/*.java
 * cd ../../													cd ..\..\
 * jar -cf wcloud.war *											jar -cf wcloud.war *
 * 
 * Drag and drop the file ngrams.war into the webapps directory of Tomcat to deploy the application. It will then be 
 * accessible from http://localhost:8080. The ignore words file at res/ignorewords.txt will be located using the
 * IGNORE_WORDS_FILE_LOCATION mapping in web.xml. This works perfectly, so don't change it unless you know what
 * you are doing...
 * 
*/

public class ServiceHandler extends HttpServlet {
	private String ignoreWords = null;
	private File f;
	
	public void init() throws ServletException {
		ServletContext ctx = getServletContext(); //Get a handle on the application context
		
		//Reads the value from the <context-param> in web.xml
		ignoreWords = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION"); 
		f = new File(ignoreWords); //A file wrapper around the ignore words...
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); //Output the MIME type
		PrintWriter out = resp.getWriter(); //Write out text. We can write out binary too and change the MIME type...
		
		//Initialise some request varuables with the submitted form info. These are local to this method and thread safe...
		String option = req.getParameter("cmbOptions"); //Change options to whatever you think adds value to your assignment...
		String s = req.getParameter("query");

		out.print("<html><head><title>Artificial Intelligence Assignment</title>");		
		out.print("<link rel=\"stylesheet\" href=\"includes/style.css\">");
		
		out.print("</head>");		
		out.print("<body>");		
		out.print("<div style=\"font-size:48pt; font-family:arial; color:#990000; font-weight:bold\">Web Opinion Visualiser</div>");	
		
		out.print("<p><h2>Please read the following carefully</h2>");
		out.print("<p>The &quot;ignore words&quot; file is located at <font color=red><b>" + f.getAbsolutePath() + "</b></font> and is <b><u>" + f.length() + "</u></b> bytes in size.");
		out.print("You must place any additional files in the <b>res</b> directory and access them in the same way as the set of ignore words.");
		out.print("<p>Place any additional JAR archives in the WEB-INF/lib directory. This will result in Tomcat adding the library of classes ");	
		out.print("to the CLASSPATH for the web application context. Please note that the JAR archives <b>jFuzzyLogic.jar</b>, <b>encog-core-3.4.jar</b> and "); 		
		out.print("<b>jsoup-1.12.1.jar</b> have already been added to the project.");	
			
		out.print("<p><fieldset><legend><h3>Result</h3></legend>");
		
		WordFrequency[] words = new WeightedFont().getFontSizes(getWordFrequencyKeyValue());
		Arrays.sort(words, Comparator.comparing(WordFrequency::getFrequency, Comparator.reverseOrder()));
		//Arrays.stream(words).forEach(System.out::println);

		//Spira Mirabilis
		LogarithmicSpiralPlacer placer = new LogarithmicSpiralPlacer(800, 600);
		for (WordFrequency word : words) {
			placer.place(word); //Place each word on the canvas starting with the largest
		}

		BufferedImage cloud = placer.getImage(); //Get a handle on the word cloud graphic
		out.print("<img src=\"data:image/png;base64," + encodeToString(cloud) + "\" alt=\"Word Cloud\">");
		
		
		out.print("</fieldset>");	
		out.print("<P>Maybe output some search stats here, e.g. max search depth, effective branching factor.....<p>");		
		out.print("<a href=\"./\">Return to Start Page</a>");
		out.print("</body>");	
		out.print("</html>");	
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doGet(req, resp);
 	}

	//A sample array of WordFrequency for demonstration purposes
	private WordFrequency[] getWordFrequencyKeyValue() {
		WordFrequency[] wf = new WordFrequency[32];
		wf[0] = new WordFrequency("Galway", 65476);
		wf[1] = new WordFrequency("Sligo", 43242);
		wf[2] = new WordFrequency("Roscommon", 2357);
		wf[4] = new WordFrequency("Clare", 997);
		wf[5] = new WordFrequency("Donegal", 876);
		wf[17] = new WordFrequency("Armagh", 75);
		wf[6] = new WordFrequency("Waterford", 811);
		wf[7] = new WordFrequency("Tipperary", 765);
		wf[8] = new WordFrequency("Westmeath", 643);
		wf[9] = new WordFrequency("Leitrim", 543);		
		wf[10] = new WordFrequency("Mayo", 456);
		wf[11] = new WordFrequency("Offaly", 321);
		wf[12] = new WordFrequency("Kerry", 221);
		wf[13] = new WordFrequency("Meath", 101);
		wf[14] = new WordFrequency("Wicklow", 98);
		wf[18] = new WordFrequency("Antrim", 67);
		wf[3] = new WordFrequency("Limerick", 1099);
		wf[15] = new WordFrequency("Kildare", 89);
		wf[16] = new WordFrequency("Fermanagh", 81);
		wf[19] = new WordFrequency("Dublin", 12);
		wf[20] = new WordFrequency("Carlow", 342);
		wf[21] = new WordFrequency("Cavan", 234);
		wf[22] = new WordFrequency("Down", 65);
		wf[23] = new WordFrequency("Kilkenny", 45);
		wf[24] = new WordFrequency("Laois", 345);
		wf[25] = new WordFrequency("Derry", 7);
		wf[26] = new WordFrequency("Longford", 8);
		wf[27] = new WordFrequency("Louth", 34);
		wf[28] = new WordFrequency("Monaghan", 101);
		wf[29] = new WordFrequency("Tyrone", 121);
		wf[30] = new WordFrequency("Wexford", 144);
		wf[31] = new WordFrequency("Cork", 522);
		return wf;
	}
	
	private String encodeToString(BufferedImage image) {
	    String s = null;
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();

	    try {
	        ImageIO.write(image, "png", bos);
	        byte[] bytes = bos.toByteArray();

	        Base64.Encoder encoder = Base64.getEncoder();
	        s = encoder.encodeToString(bytes);
	        bos.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return s;
	}
	
	private BufferedImage decodeToImage(String imageString) {
	    BufferedImage image = null;
	    byte[] bytes;
	    try {
	        Base64.Decoder decoder = Base64.getDecoder();
	        bytes = decoder.decode(imageString);
	        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	        image = ImageIO.read(bis);
	        bis.close();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return image;
	}
}