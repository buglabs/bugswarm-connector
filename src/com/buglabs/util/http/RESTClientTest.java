package com.buglabs.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.buglabs.util.http.RestClient.HttpMethod;
import com.buglabs.util.http.RestClient.Response;
import com.buglabs.util.simplerestclient.FormFile;

public class RESTClientTest {

	
	public static void main(String[] args) {
		RESTClientTest rct = new RESTClientTest();
		
		rct.run();
	}

	private void run() {
		//Create a rest client that will deserialize the server response to a string.
		RestClient<String> restClient = new RestClient<String>();
		
		try {
			//Call GET on localhost, pass in a predefined deserializer, no body (since GET), and no custom headers.
			Response<String> resp = restClient.call(HttpMethod.GET, "localhost", RestClient.STRING_DESERIALIZER, null, null);
			
			//API allows for code that specifically checks for errors, or relies on exception handling.
			if (!resp.isError())
				System.out.println(resp.getBody());
			
			//do  GET with the short-form method
			System.out.println(
					restClient.get("localhost").getBody());
			
			// do a POST with the short-form method
			resp = restClient.post("localhost", new ByteArrayInputStream("My POST content".getBytes()));
			
			if (resp.isError())
				System.out.println("boo!");
			
			// Creat a rest client that passes the InputStream back to the client unmolested.
			RestClient<InputStream> restClient2 = new RestClient<InputStream>(RestClient.PASSTHROUGH);
			
			System.out.println(
					restClient2.get("localhost")
						.getBody().available());
			
			// Create a rest client that will throw exceptions on all HTTP and I/O errors.
			RestClient<String> rc3 = new RestClient<String>();
			rc3.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException on any error
			Response<String> rs = rc3.get("localhost");
			
			System.out.println(rs.getBody());
			
			// Subsequent calls to this rest client will not throw exceptions on HTTP errors.
			rc3.setErrorHandler(null);
						
			//following line will never throw IOException 
			rs = rc3.get("localhost");
			
			if (!rs.isError())
				System.out.println(rs.getBody());
			
			//Multipart POST with a file upload.
			Map<String, Object> body = new HashMap<String, Object>();
			
			body.put("tkey", "tval");
			body.put("myfile", new RestClient.FormFile("/tmp/boo.txt", "text/plain"));
			
			rc3.postMultipart("localhost", body);
			
			//PUT, short form, throw exception on error
			rc3.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			rc3.put("localhost", new ByteArrayInputStream("boo".getBytes()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
