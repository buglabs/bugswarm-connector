package com.buglabs.bug.swarm.connector.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Read from an input stream and give information to other threads about what has happened.
 * 
 * @author kgilmer
 *
 */
class StreamScanner extends Thread {

	private final InputStream istream;
	private int inputLineCount = 0;
	private List<String> responses = new CopyOnWriteArrayList<String>();

	/**
	 * @param istream input stream
	 */
	public StreamScanner(final InputStream istream) {
		this.istream = istream;
	}

	@Override
	public void run() {
		BufferedReader br = new BufferedReader(new InputStreamReader(istream));

		String line = null;

		try {
			while (!Thread.interrupted() && (line = br.readLine()) != null) {
				System.out.println("OUTPUT: " + line);

				if (line.trim().length() > 0) {
					inputLineCount++;
					responses.add(line.trim());
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	public boolean hasInputBeenRecieved() {
		return inputLineCount > 0;
	}

	public int getInputLineCount() {
		return inputLineCount;
	}

	public Iterable<String> getResponses() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				// TODO Auto-generated method stub
				return responses.iterator();
			}
		};
	}
}
