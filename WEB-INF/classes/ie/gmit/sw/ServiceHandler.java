package ie.gmit.sw;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
/*
 * INFORMATION ABOUT AUTHOUR:
 * Code by - Ultan Kearns,
 * Code adapted from Dr. John Healy's example,
 * Student Number: G00343745,
 * Fourth Year AI Module Project
 */

/*
 * ABOUT CLASS:
 * 
 * This class will handle creating the array of words and also 
 * creating the image that will be presented to the user, in 
 * addition this class also takes in the option that the user 
 * picks at the start of querying.
 * 
 */
public class ServiceHandler extends HttpServlet{
	static String url = "https://duckduckgo.com/html/?q=";


	private static final long serialVersionUID = 1L;
	private String ignoreWords = null;
	private File f;
	private Map<String, Integer> test = null;
	private int wordNum;
	public void init() throws ServletException {
		ServletContext ctx = getServletContext(); //Get a handle on the application context
		
		//Reads the value from the <context-param> in web.xml
		/*
		 * Redundant as I filter ignore words in nodeparser as each searchterm may need a different file
		 * for ignoring words say I were to search rome but wanted to exclude some well known prominent 
		 * Romans, I could use an ignore file with the words Cassius, Lepidus, Anthony etc. 
		 * more efficient
		 */
		ignoreWords = getServletContext().getRealPath(File.separator) + ctx.getInitParameter("IGNORE_WORDS_FILE_LOCATION"); 
		f = new File(ignoreWords); //A file wrapper around the ignore words...
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html"); //Output the MIME type
		PrintWriter out = resp.getWriter(); //Write out text. We can write out binary too and change the MIME type...
	
		//Initialise some request variables with the submitted form info. These are local to this method and thread safe...
		String option = req.getParameter("cmbOptions"); //Change options to whatever you think adds value to your assignment...
		this.wordNum = Integer.parseInt(option); // get number from options
		String query = req.getParameter("query");
 		out.print("<html><head><title>Artificial Intelligence Assignment - Ultan Kearns </title>");		
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

 		WordFrequency[] words = null;
		try {
			words = new WeightedFont().getFontSizes(getWordFrequencyKeyValue(option,query));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		//search stats depth branching factor - not binary tree so branching depth is probably wrong leaving it in anyway
		//for some reason java.math doesn't provide arbitrary logarithms?
		out.print("<P><i><b>Nodes in tree: " + NodeParser.MAX + "</b></i><br/>" + " <i><b>Search Term: " + query + "</b></i><br/>" + "<i><b>Approximate Tree depth = " + Math.round(Math.log(NodeParser.MAX) / Math.log(2)) + "</b></i><br/>" + "<p>");		
		out.print("<a href=\"./\">Return to Start Page</a>");
		out.print("</body>");	
		out.print("</html>");	
	 
 	}
 
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		doGet(req, resp);
 	}

	//Place most frequent words on this list, this also sets the size of the array of words 
	//using the options field from the form
	private WordFrequency[] getWordFrequencyKeyValue(String option,String query) throws IOException, InterruptedException {
		int value = Integer.parseInt(option);
		
		Worker w = new Worker(url,"test",wordNum);
		w.run();
		w.join();
		test = new ConcurrentHashMap<String, Integer>(w.wordMap);
		WordFrequency[]  wf = new WordFrequency[value]; 
 		for(int i = 0; i < wf.length; i++) {
			//wf[i] = new WordFrequency(test.keySet().toArray()[i].toString(),(int)test.values().toArray()[i]);
 			wf[i] = new WordFrequency(test.keySet().toString(),65);

		}
	
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
	public static void main(String[] args) {
		
		Worker w = new Worker(url,"Gallic Wars",20);
		Map<String, Integer> test = null;

		try {
			w.run();
			w.join();
			test = new ConcurrentHashMap<String, Integer> (w.getMap());
			 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//for some reason I cannot get output to the image
		System.out.println("TOP WORDS: " + test.toString());
		System.out.println(test.keySet().toArray()[1].toString());
		System.out.println((int)test.values().toArray()[1]);
	}
 
}