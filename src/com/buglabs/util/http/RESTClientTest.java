package com.buglabs.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
		RestClient restClient = new RestClient();
		
		try {
			//Call GET on localhost, pass in a predefined deserializer, no body (since GET), and no custom headers.
			Response<String> resp = restClient.call(HttpMethod.GET, "localhost", RestClient.STRING_DESERIALIZER, null, null);
			
			//API allows for code that specifically checks for errors, or relies on exception handling.
			if (!resp.isError())
				System.out.println(resp.getContent());
			
			//do  GET with the short-form method
			System.out.println(
					restClient.getAsString("localhost"));
			
			// do a POST with the short-form method
			Response<Integer> rc = restClient.post("localhost", new ByteArrayInputStream("My POST content".getBytes()));
			
			if (rc.isError())
				System.out.println("boo!");
						
			System.out.println(
					restClient.get("localhost", RestClient.PASSTHROUGH)
						.getContent().available());
			
			// Create a rest client that will throw exceptions on all HTTP and I/O errors.
		
			restClient.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException on any error
			Response<String> rs = restClient.get("localhost", RestClient.STRING_DESERIALIZER);
			
			System.out.println(rs.getContent());
			
			// Subsequent calls to this rest client will not throw exceptions on HTTP errors.
			restClient.setErrorHandler(null);
						
			//following line will never throw IOException 
			rs = restClient.get("localhost/asdf", RestClient.STRING_DESERIALIZER);
			
			if (rs.isError())
				System.out.println("Error: " + rs.getCode());
			
			restClient.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException 
			try {
				rs = restClient.get("localhost/asdf", RestClient.STRING_DESERIALIZER);
				//Error is thrown when trying to get content.
				System.out.println(rs.getContent());
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//following line will throw IOException 
			try {
				//Error is thrown when trying to get content.
				String respstr = restClient.getAsString("localhost/asdf");				
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			// Only throw errors relating to server problems.
			restClient.setErrorHandler(RestClient.THROW_5XX_ERRORS);
			
			//following line will throw IOException 
			try {
				rs = restClient.get("localhost/asdf", RestClient.STRING_DESERIALIZER);
				//Error is not thrown when trying to get content because it is not a server error, but rather a 404.
				
				System.out.println("Should be true: " + rs.isError());
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//following line will throw IOException 
			try {
				//Error is thrown when trying to get content.
				String respstr = restClient.getContent("localhost/asdf", RestClient.STRING_DESERIALIZER);				
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//Multipart POST with a file upload.
			Map<String, Object> body = new HashMap<String, Object>();
			
			body.put("tkey", "tval");
			body.put("myfile", new RestClient.FormFile("/tmp/boo.txt", "text/plain"));
			
			restClient.postMultipart("localhost", body);
			
			//PUT, short form, throw exception on error
			restClient.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			Response<Integer> mr = restClient.put("localhost", new ByteArrayInputStream("boo".getBytes()));
			
			//HTTP DELETE
			Response<Integer> drc = restClient.delete("localhost/deleteurl");
			System.out.println("should be true: " + drc.isError());
			
			//HTTP HEAD
			Response<Integer> mrc = restClient.head("localhost");
			
			System.out.println("should be false: " + mrc.isError());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
