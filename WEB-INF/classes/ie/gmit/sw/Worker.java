package ie.gmit.sw;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Worker extends Thread implements Runnable{
	String url,term;
	Map <String,Integer> wordMap;
	public Worker(String url, String term) {
		this.term = term;
		this.url = url;
		wordMap = new ConcurrentHashMap<String, Integer>();
	}
 	public void run() {
		NodeParser p;
		System.out.println("IN");
		try {
			p = new NodeParser(url, term);
			p.process();
			wordMap = p.getMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
 	public Map<String,Integer> getWordMap() {
 		return wordMap;
 	}
}
