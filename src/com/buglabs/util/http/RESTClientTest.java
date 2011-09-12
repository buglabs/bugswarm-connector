package com.buglabs.util.http;

import java.io.IOException;
import java.io.InputStream;

import com.buglabs.util.http.RestClient.HttpMethod;
import com.buglabs.util.http.RestClient.Response;

public class RESTClientTest {

	
	public static void main(String[] args) {
		RESTClientTest rct = new RESTClientTest();
		
		rct.run();
	}

	private void run() {
		RestClient<String> restClient = new RestClient<String>();
		
		try {
			Response<String> resp = restClient.call(HttpMethod.GET, "localhost", RestClient.STRING_DESERIALIZER, null, null);
			
			if (!resp.isError())
				System.out.println(resp.getBody());
			
			System.out.println(restClient.get("localhost").getBody());
			
			RestClient<InputStream> restClient2 = new RestClient<InputStream>(RestClient.PASSTHROUGH);
			
			System.out.println(restClient2.get("localhost").getBody().available());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
