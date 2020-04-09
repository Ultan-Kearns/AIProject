package ie.gmit.sw;

import java.io.IOException;

public class Worker extends Thread implements Runnable{
	String url,term;
	public Worker(String url, String term) {
		this.term = term;
		this.url = url;
 
	}
 	public void run() {
		NodeParser p;
		System.out.println("IN");
		try {
			p = new NodeParser(url, term);
			p.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
