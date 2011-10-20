package com.buglabs.bug.swarm.connector.ws;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.touge.restclient.ReSTClient;

/**
 * An enumeration for HTTP response code as they pertain to bugswarm.
 * 
 * See https://github.com/buglabs/bugswarm/wiki/HTTP-Response-Codes
 * 
 * @author kgilmer
 * 
 */
public enum SwarmWSResponse {
	
	/*
	 * These codes and descriptions were copied/pasted from specification.
	 * @formatter:off
	 */	
	R200(200, "Request was received and handled without errors."), 
	R201(201, "The entity was created successfully."), 
	R204(204, "The entity was created successfully, no content returned."), 
	R400(400, "Bad Request. There was something wrong with the specified data. It may be an unrecognizable format or a missing field. The platform will provide a descriptive message with the application error code for easier debugging."), 
	R401(401, "Unauthorized. You may not have specified the HTTP header containing your API Key."), 
	R403(403, "Forbidden. The resource may exists but you don't have access to it due to application constraints."), 
	R404(404, "Not found. The specified url does not exist in the server or the entity itself does not exist in our database."), 
	R409(409, "Conflict. There was a conflict with the request. i.e: trying to delete an API Key specified in the x-bugswarmapikey header of the same request."), 
	R500(500, "Internal Error. Something went wrong with our server. If you got this error please notify it to our development team.");
	// @formatter:on

	private final int code;
	private final String message;
	
	/**
	 * Deserialize to SwarmWSResponse.
	 */
	public static final ReSTClient.ResponseDeserializer<SwarmWSResponse> Deserializer = 
		new ReSTClient.ResponseDeserializer<SwarmWSResponse>() {
		@Override
		public SwarmWSResponse deserialize(InputStream input, int responseCode, Map<String, List<String>> headers)
			throws IOException {
			return SwarmWSResponse.fromCode(responseCode);
		}
	};

	/**
	 * @param code
	 *            HTTP error code
	 * @param message
	 *            Swarm-specific message associated with HTTP code
	 */
	SwarmWSResponse(final int code, final String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return message + " (" + code + ")";
	}

	/**
	 * @return HTTP code
	 */
	public int getCode() {
		return code;
	}

	/**
	 * @return Swarm-specific message associated with response.
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param code
	 *            HTTP code
	 * @return instance
	 */
	public static SwarmWSResponse fromCode(final int code) {
		for (SwarmWSResponse r : values())
			if (r.code == code)
				return r;

		throw new RuntimeException("This is an undefined HTTP response: " + code);
	}

	/**
	 * Evaluate if response code is an error or normal response.
	 * 
	 * @return true if the response is an error, false otherwise.
	 */
	public boolean isError() {
		return code != R200.code && code != R201.code;
	}
}
