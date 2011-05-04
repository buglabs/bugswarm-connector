package com.buglabs.bug.swarm.connector.ws;

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
	 */
	R200(200, "Request was received and handled without errors."),	
	R201(201, "The entity was created successfully."),
	R400(400, "Bad Request. There was something wrong with the specified data. It may be an unrecognizable format or a missing field. The platform will provide a descriptive message with the application error code for easier debugging."),
	R401(401, "Unauthorized. You may not have specified the HTTP header containing your API Key."),
	R404(404, "Not found. The specified url does not exist in the server or the entity itself does not exist in our database."),
	R409(409, "Conflict. There was a conflict with the request. i.e: trying to delete an API Key specified in the x-bugswarmapikey header of the same request."),
	R500(500, "Internal Error. Something went wrong with our server. If you got this error please notify it to our development team.");
	
	private final int code;
	private final String message;

	SwarmWSResponse(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return message + " (" + code + ")";
	}
	
	public static SwarmWSResponse fromCode(int code) {
		for (SwarmWSResponse r: values()) 
			if (r.code == code)
				return r;
		
		throw new RuntimeException("This is an undefined HTTP response: " + code);
	}
	
	/**
	 * Evaluate if response code is an error or normal response.
	 * @return
	 */
	public boolean isError() {
		return code != 200 && code != 201;
	}
}
