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
				System.out.println(resp.get());
			
			//do  GET with the short-form method
			System.out.println(
					restClient.get("localhost").get());
			
			// do a POST with the short-form method
			Response<Boolean> r = restClient.post("localhost", RestClient.BOOLEAN_DESERIALIZER, new ByteArrayInputStream("My POST content".getBytes()));
			
			if (resp.isError())
				System.out.println("boo!");
						
			System.out.println(
					restClient.get("localhost", RestClient.PASSTHROUGH)
						.get().available());
			
			// Create a rest client that will throw exceptions on all HTTP and I/O errors.
		
			restClient.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException on any error
			Response<String> rs = restClient.get("localhost");
			
			System.out.println(rs.get());
			
			// Subsequent calls to this rest client will not throw exceptions on HTTP errors.
			restClient.setErrorHandler(null);
						
			//following line will never throw IOException 
			rs = restClient.get("localhost");
			
			if (!rs.isError())
				System.out.println(rs.get());
			
			//Multipart POST with a file upload.
			Map<String, Object> body = new HashMap<String, Object>();
			
			body.put("tkey", "tval");
			body.put("myfile", new RestClient.FormFile("/tmp/boo.txt", "text/plain"));
			
			restClient.postMultipart("localhost", RestClient.BOOLEAN_DESERIALIZER, body);
			
			//PUT, short form, throw exception on error
			restClient.setErrorHandler(RestClient.THROW_ALL_ERRORS);
			Response<Boolean> mr = restClient.put("localhost", RestClient.BOOLEAN_DESERIALIZER, new ByteArrayInputStream("boo".getBytes()));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
