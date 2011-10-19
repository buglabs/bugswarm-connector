package com.buglabs.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.buglabs.util.http.ReSTClient.HttpMethod;
import com.buglabs.util.http.ReSTClient.Response;
import com.buglabs.util.http.ReSTClient.URLBuilder;

public class ReSTClientTest {

	
	public static void main(String[] args) {
		ReSTClientTest rct = new ReSTClientTest();
		
		rct.run();
	}

	private void run() {
		//Create a rest client that will deserialize the server response to a string.
		ReSTClient restClient = new ReSTClient();
		
		try {
			//Test URLBuilder			
			System.out.println(restClient.buildURL("htTPS://citibank.com/secureme/").append("/halp"));
			
			System.out.println(restClient.buildURL("yahoo.com")
											.append("a")
											.append("mystore/")
											.append("toodles?index=5"));
						
			System.out.println(restClient.buildURL("me.com/").append("/you/").append("/andi/").append("like/each/ohter/"));
			
			System.out
					.println(restClient.buildURL("myhost.com", "first/", "//second", "third/forth/fith", "mypage.asp?i=1&b=2&c=3").setHttps(true));
			
			URLBuilder origurl = restClient.buildURL("myhost.net/","/homepage");
			URLBuilder newurl = origurl.copy().append("asdf/adf/reqotwoetiywer").setHttps(true);
							
			System.out.println(origurl);
			System.out.println(newurl);
			
			URLBuilder localhost = restClient.buildURL("localhost");
			
			//Simplest GET, with the short-form method
			System.out.println(
					restClient.callGet(localhost));
			
			//Call GET on localhost using long form, pass in a predefined deserializer, no body (since GET), and no custom headers.
			Response<String> resp = restClient.call(HttpMethod.GET, localhost.toString(), ReSTClient.STRING_DESERIALIZER, null, null);
			
			//API allows for code that specifically checks for errors, or relies on exception handling.
			if (!resp.isError())
				System.out.println(resp.getContent());			
			
			// do a POST with the short-form method
			Response<Integer> rc = restClient.callPost(localhost, new ByteArrayInputStream("My POST content".getBytes()));
			
			if (rc.isError())
				System.out.println("boo!");
						
			System.out.println(
					restClient.callGet(localhost, ReSTClient.INPUTSTREAM_DESERIALIZER)
						.getContent().available());
			
			// Create a rest client that will throw exceptions on all HTTP and I/O errors.
		
			restClient.setErrorHandler(ReSTClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException on any error
			Response<String> rs = restClient.callGet(localhost, ReSTClient.STRING_DESERIALIZER);
			
			System.out.println(rs.getContent());
			
			// Subsequent calls to this rest client will not throw exceptions on HTTP errors.
			restClient.setErrorHandler(null);
						
			//following line will never throw IOException 
			rs = restClient.callGet(localhost.copy("asdf"), ReSTClient.STRING_DESERIALIZER);
			
			if (rs.isError())
				System.out.println("Error: " + rs.getCode());
			
			restClient.setErrorHandler(ReSTClient.THROW_ALL_ERRORS);
			
			//following line will throw IOException 
			try {
				rs = restClient.callGet(localhost.copy("/asdf"), ReSTClient.STRING_DESERIALIZER);
				//Error is thrown when trying to get content.
				System.out.println(rs.getContent());
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//following line will throw IOException 
			try {
				//Error is thrown when trying to get content.
				String respstr = restClient.callGet("localhost/asdf");				
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			// Only throw errors relating to server problems.
			restClient.setErrorHandler(ReSTClient.THROW_5XX_ERRORS);
			
			//following line will throw IOException 
			try {
				rs = restClient.callGet("localhost/asdf", ReSTClient.STRING_DESERIALIZER);
				//Error is not thrown when trying to get content because it is not a server error, but rather a 404.
				
				System.out.println("Should be true: " + rs.isError());
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//following line will throw IOException 
			restClient.setErrorHandler(ReSTClient.THROW_ALL_ERRORS);
			try {
				//Error is thrown when trying to get content.
				String respstr = restClient.callGetContent(localhost.copy("/asdf"), ReSTClient.STRING_DESERIALIZER);				
			} catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
			}
			
			//Multipart POST with a file upload.
			Map<String, Object> body = new HashMap<String, Object>();
			
			body.put("tkey", "tval");
			body.put("myfile", new ReSTClient.FormFile("/tmp/boo.txt", "text/plain"));
			
			restClient.callPostMultipart("localhost", body);
			
			//PUT, short form, throw exception on error
			restClient.setErrorHandler(ReSTClient.THROW_ALL_ERRORS);
			Response<Integer> mr = restClient.callPut("localhost", new ByteArrayInputStream("boo".getBytes()));
			
			//HTTP DELETE
			Response<Integer> drc = restClient.callDelete(localhost.copy("/deleteurl"));
			System.out.println("should be true: " + drc.isError());
			
			//HTTP HEAD
			Response<Integer> mrc = restClient.callHead(localhost);
			
			System.out.println("should be false: " + mrc.isError());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
}
