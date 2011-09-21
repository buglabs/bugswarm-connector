package com.buglabs.bug.swarm.connector.test;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import junit.framework.TestCase;

import com.buglabs.bug.swarm.connector.model.FeedRequest;

public class FeedRequestTests extends TestCase {

	
	public void testParseJson() throws JsonParseException, JsonMappingException, IOException {
		String input = "{ \"type\": \"get\", \"feed\": \"location\", \"params\": { \"format\": \"fmt\", \"frequency\": 20, \"foo\": [\"val1\", \"val2\"] } }";
		
		System.out.println(input);

		FeedRequest fr = FeedRequest.parseJSON(input);
		
		assertTrue(fr.getType().toString().equals("get"));
		assertTrue(fr.getName().equals("location"));
		assertTrue(fr.getParams() != null);
		assertTrue(fr.getParams().size() == 3);
		
		assertTrue(fr.getParams().containsKey("format"));
		assertTrue(fr.getParams().containsKey("foo"));
		assertTrue(fr.getParams().get("foo") instanceof List);
		assertTrue(((List) fr.getParams().get("foo")).size() == 2);
	}
}
