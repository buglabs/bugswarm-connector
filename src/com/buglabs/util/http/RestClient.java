package com.buglabs.util.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author kgilmer
 *
 * @param <T> Type that will be returned from Response.getBody().
 */
public class RestClient {
	
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	
	private static final int COPY_BUFFER_SIZE = 1024 * 4;
	private static final int RANDOM_CHAR_COUNT = 15;
	private static final String HEADER_TYPE = HEADER_CONTENT_TYPE;
	private static final String HEADER_PARA = "Content-Disposition: form-data";
	private static final String CONTENT_TYPE = "multipart/form-data";
	private static final String FILE_NAME = "filename";
	private static final String LINE_ENDING = "\r\n";
	private static final String BOUNDARY = "boundary=";
	private static final String PARA_NAME = "name";
	
	private static final Map<Integer, String> HTTP_RESPONSE_TEXT = createResponseMap();

	
	/**
	 * HTTP methods supported by REST client.
	 *
	 */
	public enum HttpMethod {
		GET, POST, PUT, DELETE, HEAD
	}
	
	/**
	 * implement to provide an HttpURLConnection w/ some properties pre-set see
	 * BasicAuthenticationConnectionProvider for an example.
	 * 
	 * This is passed into HttpRequest on creation to provide it w/ the connection
	 * 
	 * @author bballantine
	 * 
	 */
	public interface ConnectionProvider {
		/**
		 * @param urlStr url that is used to source the connection
		 * @return an appropriate instance of HttpURLConnection
		 * @throws IOException on I/O error.
		 */
		HttpURLConnection getConnection(String urlStr) throws IOException;
	}

	/**
	 * Implementors can configure the http connection before every call is made.
	 * Useful for setting headers that always need to be present in every WS
	 * call to a given server.
	 * 
	 */
	public interface ConnectionInitializer {
		/**
		 * @param connection
		 *            HttpURLConnection
		 */
		void initialize(HttpURLConnection connection);
	}
	
	/**
	 * Utility interface for building URLs from String segments.
	 */
	public interface URLBuilder extends Cloneable {
		/**
		 * Append a segment to the URL.  Will handle leading and trailing slashes, and schemes.
		 * 
		 * @param segment to be appended
		 * @return instance of builder
		 */
		URLBuilder append(String ... segment);
		
		/**
		 * @param value if true, scheme is set to https, otherwise http.
		 * @return instance of builder
		 */
		URLBuilder setHttps(boolean value);		
		
		/**
		 * @return URL as a String with scheme
		 */
		String toString();
		
		/**
		 * @return A new instance of URLBuilder with same path and scheme as parent.
		 */
		URLBuilder copy();
		
		/**
		 * @param segment new segment to append to new copy of URLBuilder
		 * @return A new instance of URLBuilder with same path and scheme as parent, with segment appended.
		 */
		URLBuilder copy(String ... segments);
	}
	
	/**
	 * 
	 *
	 * @param <T>
	 */
	public interface ResponseDeserializer<T>  {			
		/**
		 * Deserialize the input.
		 * @param input input stream of response
		 * @return deserialized representation of response
		 * @throws IOException on I/O error
		 */
		T deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException;
	}
	
	/**
	 * A HTTPResponseDeserializer that returns the entire response as a String.
	 */
	public static final ResponseDeserializer<String> STRING_DESERIALIZER = new ResponseDeserializer<String>() {

		@Override
		public String deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {			
			if (input != null)
				return new String(streamToByteArray(input));
			
			return null;
		}
	};
	
	/**
	 * A HTTPResponseDeserializer that returns true if the response from the server was not an error.
	 */
	public static final ResponseDeserializer<Integer> HTTP_CODE_DESERIALIZER = new ResponseDeserializer<Integer>() {

		@Override
		public Integer deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {			
			return responseCode;
		}
	};
	
	/**
	 * A HTTPResponseDeserializer that simply returns the internal inputstream.
	 * Useful for clients that wish to handle the response input stream manually.
	 */
	public static final ResponseDeserializer<InputStream> INPUTSTREAM_DESERIALIZER = new ResponseDeserializer<InputStream>() {

		@Override
		public InputStream deserialize(InputStream input, int responseCode, Map<String, List<String>> headers) throws IOException {			
			return input;
		}
	};
	
	/**
	 *
	 */
	public static final ErrorHandler THROW_ALL_ERRORS = new ErrorHandler() {

		@Override
		public void handleError(int code) throws IOException {
			if (code > 0)
				throw new IOException("HTTP Error " + code + " was returned from the server: " + HTTP_RESPONSE_TEXT.get(code));
			else 
				throw new IOException("A non-HTTP error was returned from the server.");
		}
		
	};
	
	/**
	 *
	 */
	public static final ErrorHandler THROW_5XX_ERRORS = new ErrorHandler() {

		@Override
		public void handleError(int code) throws IOException {
			if (code > 499 && code < 600)
				throw new IOException("HTTP Error " + code + " was returned from the server: " + HTTP_RESPONSE_TEXT.get(code));			
		}
		
	};
	
	/**
	 * The response from the server for a given request.
	 *
	 * @param <T>
	 */
	public interface Response<T> {
	    /**
	     * Cancel the request.
	     * 
	     * @param mayInterruptIfRunning
	     * @return
	     */
	    public abstract boolean cancel(boolean mayInterruptIfRunning);

	    /**
	     * @return true if the request has been canceled
	     */
	    public abstract boolean isCancelled();

	    /**
	     * @return true if the request has been completed
	     */
	    public abstract boolean isDone();

	    /**
	     * @return the content (body) of the response.
	     * 
	     * @throws IOException
	     */
	    public abstract T getContent() throws IOException;	   
	    
		/**
		 * @return The HttpURLConnection associated with the request.
		 */
		HttpURLConnection getConnection();
		/**
		 * @return The HTTP method that was used in the call.
		 */
		HttpMethod getRequestMethod();
		/**
		 * @return The URL that was used in the call.
		 */
		String getRequestUrl();
		/**
		 * @return The HTTP Response code from the server.
		 * @throws IOException on I/O error.
		 */
		int getCode() throws IOException;
		
		/**
		 * @return the amount of time in millis that occured while waiting for the server.
		 */
		long getCallTime();
		
		/**
		 * @return the total amount of time the client experiences from the call() method.
		 */
		long getTotalTime();
		
		/**
		 * @return true if error code or an exception is raised, false otherwise.
		 */
		boolean isError();				
	}
	
	/**
	 * The ErrorHander does something based on an HTTP or I/O error.
	 *
	 */
	public interface ErrorHandler {
		/**
		 * @param code the HTTP code of the error
		 * @throws IOException on I/O error
		 */
		void handleError(int code) throws IOException;
	}
	
	/**
	 * Used to specify a file to upload in a multipart POST.
	 *
	 */
	public static class FormFile extends File {
		private static final long serialVersionUID = 2957338960806476533L;
		private final String mimeType;

		/**
		 * @param pathname
		 */
		public FormFile(String pathname, String mimeType) {
			super(pathname);
			this.mimeType = mimeType;					
		}
		
		/**
		 * @return Mime type of file.
		 */
		public String getMimeType() {
			return mimeType;
		}
	}
	
	/**
	 * Used to specify a file to upload in a multipart POST.
	 *
	 */
	public static class FormInputStream extends InputStream {
		private static final long serialVersionUID = 2957338960806476533L;
		private final String mimeType;
		private final InputStream parent;
		private final String name;

		/**
		 * @param pathname
		 */
		public FormInputStream(InputStream parent, String name, String mimeType) {

			this.parent = parent;
			this.name = name;
			this.mimeType = mimeType;					
		}
		
		/**
		 * @return Mime type of file.
		 */
		public String getMimeType() {
			return mimeType;
		}

		@Override
		public int read() throws IOException {			
			return parent.read();
		}
		
		@Override
		public int read(byte[] b) throws IOException {		
			return parent.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return parent.read(b, off, len);
		}

		public String getName() {
			return name;
		}
	}

	private static Random RNG;

	private ConnectionProvider connectionProvider;

	private List<ConnectionInitializer> connectionInitializers;
	
	private ErrorHandler errorHandler;
		
	/**
	 * Default constructor.
	 */
	public RestClient() {
		this.connectionProvider = new DefaultConnectionProvider();
		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
		//this.responseDeserializers = null;
		this.errorHandler = null;
	}
	
	/**
	 * @param connectionProvider ConnectionProvider
	 */
	public RestClient(ConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
		//this.responseDeserializers = null;
		this.errorHandler = null;
	}
	
	/**
	 * @param initializer ConnectionInitializer
	 */
	public RestClient(ConnectionInitializer initializer) {
		this();
		connectionInitializers.add(initializer);
	}
	
	/**
	 * @param connectionProvider ConnectionProvider
	 * @param initializer ConnectionInitializer
	 */
	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer) {
		this(connectionProvider);
		connectionInitializers.add(initializer);
	}
	
	/**
	 * @param initializer ConnectionInitializer
	 * @param deserializer ResponseDeserializer<T>
	 */
	public RestClient(ConnectionInitializer initializer, ResponseDeserializer<?> deserializer) {
		this(initializer);
		//this.responseDeserializers = deserializer;
	}
	
	/**
	 * @param connectionProvider ConnectionProvider
	 * @param initializer ConnectionInitializer
	 * @param deserializer ResponseDeserializer<T>
	 */
	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, ResponseDeserializer<?> deserializer) {
		this(connectionProvider, initializer);
		//this.responseDeserializers = deserializer;
	}
	
	/**
	 * @param connectionProvider ConnectionProvider
	 * @param initializer ConnectionInitializer
	 * @param deserializer ResponseDeserializer<T>
	 * @param errorHandler ErrorHandler
	 */
	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, 
			ResponseDeserializer<?> deserializer, ErrorHandler errorHandler) {
		this.connectionProvider = connectionProvider;
		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
		//this.responseDeserializers = deserializer;
		this.errorHandler = errorHandler;
		connectionInitializers.add(initializer);		
	}
	
	// Public methods
	
	/**
	 * @return ErrorHandler
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}
	
	/**
	 * Sets an error handler for the client.  If no error handler is set, HTTP (application level) errors will be ignored 
	 * by the client.
	 * 
	 * Creating a custom ErrorHandler let's the client handle specific errors from the server in an application specific way.
	 * 
	 * See also: THROW_ALL_ERRORS, THROW_5XX_ERRORS
	 * 
	 * @param handler ErrorHandler
	 */
	public void setErrorHandler(ErrorHandler handler) {
		this.errorHandler = handler;
	}
	
	/**
	 * @param provider ConnectionProvider
	 */
	public void setConnectionProvider(ConnectionProvider provider) {
		this.connectionProvider = provider;
	}
	
	/**
	 * @return ConnectionProvider
	 */
	public ConnectionProvider getConnectionProvider() {
		return connectionProvider;
	}
	
	/**
	 * @param initializer
	 */
	public void addConnectionInitializer(ConnectionInitializer initializer) {
		if (!connectionInitializers.contains(initializer))
			connectionInitializers.add(initializer);
	}
	
	/**
	 * @param initializer ConnectionInitializer
	 * @return ConnectionInitializer
	 */
	public boolean removeConnectionInitializer(ConnectionInitializer initializer) {
		return connectionInitializers.remove(initializer);
	}
	
	/**
	 * @param method
	 * @param url
	 * @param deserializer
	 * @return
	 * @throws IOException
	 */
	public <T> Response<T> call(final HttpMethod method, final String url, final ResponseDeserializer<T> deserializer, InputStream content, Map<String, String> headers) throws IOException {
		validateArguments(method, url);
		
		long timeStart = System.currentTimeMillis();
		
		String httpUrl = url;
		if (!url.toLowerCase().startsWith("http://"))
			httpUrl = "http://" + url;
				
		final HttpURLConnection connection = connectionProvider.getConnection(httpUrl);
		connection.setRequestMethod(method.toString());
		
		for (ConnectionInitializer initializer : connectionInitializers)
			initializer.initialize(connection);
		
		if (headers != null && headers.size() > 0)
			for (Map.Entry<String, String> entry : headers.entrySet())
				connection.addRequestProperty(entry.getKey(), entry.getValue());

		ByteArrayOutputStream baos;
		switch(method) {
		case GET:			
			connection.setDoInput(true);
			connection.setDoOutput(false);
			break;
		case POST:
			connection.setDoOutput(true);	
			baos = new ByteArrayOutputStream();
			copy(content, baos);					
			writeRequestBody(connection, baos.toByteArray());	
			baos.close();
			break;
		case PUT:
			connection.setDoOutput(true);
			baos = new ByteArrayOutputStream();
			copy(content, baos);
			writeRequestBody(connection, baos.toByteArray());
			baos.close();
			break;
		case DELETE:
			connection.setDoInput(true);
			break;
		case HEAD:
			connection.setDoInput(true);
			connection.setDoOutput(false);
			break;
		default:
			throw new RuntimeException("Unhandled HTTP method.");
		}	
		
		return new Response<T>() {

			private boolean done;
			private long callEnd;
			private boolean cancelled;

			@Override
			public int getCode() throws IOException {			
				return connection.getResponseCode();
			}
			
			@Override
			public String getRequestUrl() {
				return url;
			}
			
			@Override
			public HttpMethod getRequestMethod() {
				return method;			
			}
			
			@Override
			public HttpURLConnection getConnection() {
				return connection;			
			}

			@Override
			public boolean isError() {
				int code;
				try {
					code = getCode();
					return code >= HttpURLConnection.HTTP_BAD_REQUEST && code < HttpURLConnection.HTTP_VERSION;
				} catch (IOException e) {
					return true;
				}			
			}

			@Override
			public long getCallTime() {			
				//return callEnd - callStart;
				return 0;
			}

			@Override
			public long getTotalTime() {		
				//return callEnd - timeStart;
				return 0;
			}

			@Override
			public boolean cancel(boolean flag) {
				connection.disconnect();
				cancelled = true;
				return cancelled;
			}

			@Override
			public boolean isCancelled() {			
				return cancelled;				
			}

			@Override
			public boolean isDone() {
				return done;				
			}

			@Override
			public T getContent() throws IOException {									
				if (isError()) {
					if (errorHandler != null) 
						errorHandler.handleError(getCode());
						
					return (T) deserializer.deserialize(null, connection.getResponseCode(), 
							connection.getHeaderFields());
				}
				
				if (deserializer == null) {
					T response = (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream(), 0, null);
					callEnd = System.currentTimeMillis();
					done = true;
					return (T) response;
				}
				
				T response = (T) deserializer.deserialize(connection.getInputStream(), 
						connection.getResponseCode(), connection.getHeaderFields());
				
				callEnd = System.currentTimeMillis();
				done = true;
				return response;				
			}
			
		};				
	}
	
	/**
	 * Execute GET method and return body as a string.
	 * @param url of server.  If not String, toString() will be called.
	 * @return body as a String
	 * @throws IOException on I/O error
	 */
	public String getAsString(Object url) throws IOException {		
		return getContent(url.toString(), STRING_DESERIALIZER);
	}
	
	
	/**
	 * Execute GET method and return body deserizalized.
	 * 
	 * @param url of server.  If not String, toString() will be called.
	 * @param deserializer ResponseDeserializer
	 * @return T deserialized object
	 * @throws IOException on I/O error
	 */
	public <T> T getContent(Object url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url.toString(), deserializer, null, null).getContent();
	}
	
	/**
	 * Execute GET method and deserialize response.
	 * 
	 * @param url of server  If not String, toString() will be called.
	 * @param deserializer class that can deserialize content into desired type.
	 * @return type specified by deserializer
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> get(Object url, Map<String, String> headers, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url.toString(), deserializer, null, headers);
	}
	
	/**
	 * Execute GET method and deserialize response.
	 * 
	 * @param url of server.  If not String, toString() will be called.
	 * @param deserializer class that can deserialize content into desired type.
	 * @return type specified by deserializer
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> get(Object url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url.toString(), deserializer, null, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server.  If not String, toString() will be called.
	 * @param body body of post as an input stream
	 * @return a response to the request
	 * @throws IOException on I/O error
	 */
	public Response<Integer> post(Object url, InputStream body) throws IOException {
		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, body, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> post(Object url, Map<String, String> formData) throws IOException {
		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param body body of post as an input stream
	 * @return a response to the request
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> post(Object url, InputStream body, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url.toString(), deserializer, body, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server.   If not String, toString() will be called.
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> post(Object url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url.toString(), deserializer, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}
	
	/**
	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
	 * 
	 * @param url url of server.  If not String, toString() will be called.
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> postMultipart(Object url, Map<String, Object> content) throws IOException {
		return call(HttpMethod.POST, url.toString(), HTTP_CODE_DESERIALIZER, createMultipartPostBody(content), null);
	}
	
	/**
	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
	 * 
	 * @param url url of server.  If not String, toString() will be called.
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> postMultipart(Object url, Map<String, Object> content, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url.toString(), deserializer, createMultipartPostBody(content), null);
	}
	
	/**
	 * Call PUT method on a server.
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> put(Object url, InputStream content) throws IOException {
		return call(HttpMethod.PUT, url.toString(), HTTP_CODE_DESERIALIZER, content, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> put(Object url, Map<String, String> formData) throws IOException {
		return call(HttpMethod.PUT, url.toString(), HTTP_CODE_DESERIALIZER, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}
	
	/**
	 * Call PUT method on a server.
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> put(Object url, InputStream content, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.PUT, url.toString(), deserializer, content, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> put(Object url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.PUT, url.toString(), deserializer, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}

	
	/**
	 * Call DELETE method on a server.
	 * 
	 * @param url of server.  If not String, toString() will be called.
	 * @return HTTP response from server
	 * @throws IOException on I/O error
	 */
	public Response<Integer> delete(Object url) throws IOException {
		return call(HttpMethod.DELETE, url.toString(), HTTP_CODE_DESERIALIZER, null, null);
	}
	
	/**
	 * Call DELETE method on a server.
	 * 
	 * @param url of server.  If not String, toString() will be called.
	 * @return HTTP response from server
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> delete(Object url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.DELETE, url.toString(), deserializer, null, null);
	}
	
	/**
	 * Call HEAD method on a server.
	 * 
	 * @param url of server.  If not String, toString() will be called.
	 * @return HTTP Response from server
	 * @throws IOException on I/O error
	 */
	public Response<Integer> head(Object url) throws IOException {
		return call(HttpMethod.HEAD, url.toString(), HTTP_CODE_DESERIALIZER, null, null);
	}

	
	// Public static methods
	
	/**
	 * Create a buffer for a multi-part POST body, and return an input stream to the buffer.
	 * 
	 * @param content A map of <String, Object>  The values can either be of type String, RestClient.FormInputStream, or 
	 * type RestClient.FormFile.  Other types will cause an IllegalArgumentException.
	 * @return an input stream of buffer of POST body.
	 * @throws IOException on I/O error.
	 */
	public static InputStream createMultipartPostBody(Map<String, Object> content) throws IOException {
		String boundary = createMultipartBoundary();
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();	
		byte[] header = getPartHeader(boundary);
		
		for (Map.Entry<String, Object> entry : content.entrySet()) {
			baos.write(header);
			baos.write(entry.getKey().getBytes());
			baos.write('"');
			
			if (entry.getValue() instanceof String) {
				baos.write(LINE_ENDING.getBytes());
				baos.write(LINE_ENDING.getBytes());
				baos.write(((String) entry.getValue()).getBytes());
			} else if (entry.getValue() instanceof FormFile) {
				FormFile ffile = (FormFile) entry.getValue();
				baos.write("; ".getBytes());
				baos.write(FILE_NAME.getBytes());
				baos.write("=\"".getBytes());
				baos.write(ffile.getName().getBytes());
				baos.write('"');
				baos.write(LINE_ENDING.getBytes());				
				baos.write(HEADER_TYPE.getBytes());
				baos.write(": ".getBytes());
				baos.write(ffile.getMimeType().getBytes());
				baos.write(';');
				baos.write(LINE_ENDING.getBytes());
				baos.write(LINE_ENDING.getBytes());
				baos.write(streamToByteArray(new FileInputStream(ffile)));
			} else if (entry.getValue() instanceof FormInputStream) {
				FormInputStream ffile = (FormInputStream) entry.getValue();
				baos.write("; ".getBytes());
				baos.write(FILE_NAME.getBytes());
				baos.write("=\"".getBytes());
				baos.write(ffile.getName().getBytes());
				baos.write('"');
				baos.write(LINE_ENDING.getBytes());				
				baos.write(HEADER_TYPE.getBytes());
				baos.write(": ".getBytes());
				baos.write(ffile.getMimeType().getBytes());
				baos.write(';');
				baos.write(LINE_ENDING.getBytes());
				baos.write(LINE_ENDING.getBytes());
				baos.write(streamToByteArray(ffile));
			} else if (entry.getValue() == null) {
				throw new IllegalArgumentException("Content value is null.");
			} else {
				throw new IllegalArgumentException("Unhandled type: " + entry.getValue().getClass().getName());
			}
			
			baos.write(LINE_ENDING.getBytes());
		}
		
		return new ByteArrayInputStream(baos.toByteArray());
	}
	
	/**
	 * Get the general response text associated with an HTTP Response code.
	 * 
	 * @param httpResponseCode HTTP code, ex 404
	 * @return Short description of response or null if invalid or unknown code.
	 */
	public static String getHttpResponseText(int httpResponseCode) {
		return  HTTP_RESPONSE_TEXT.get(httpResponseCode);
	}
	
	/**
	 * Build a URL with the URLBuilder utility interface.  This interface
	 * will clean extra/missing path segment terminators and handle schemes.
	 * 
	 * @param segment set of segments that compose the url.
	 * @return an instance of URLBuilder with complete url.
	 */
	public URLBuilder buildURL(String ... segment) {
		URLBuilderImpl builder = new URLBuilderImpl();
		
		if (segment != null)
			if (segment.length == 0)
				return builder;
			else if (segment.length == 1)
				return builder.append(segment[0]);
			else
				for (String seg : Arrays.asList(segment))
					builder.append(seg);
				
		return builder;
	}
	
	/**
	 * @param boundary
	 * @return
	 */
	private static byte[] getPartHeader(String boundary) {
		StringBuilder sb = new StringBuilder();
		
		sb.append("--");
		sb.append(boundary);
		sb.append(LINE_ENDING);
		sb.append(HEADER_PARA);
		sb.append("; ");
		sb.append(PARA_NAME);
		sb.append("=\"");
		
		return sb.toString().getBytes();
	}

	/**
	 * Turns a map into a key=value property string.
	 * 
	 * @param props
	 *            Map of <String, String> properties
	 * @return A querystring as String
	 * @throws IOException
	 *             on string encoding error
	 */
	public static String propertyString(Map<String, String> props) throws IOException {
		StringBuilder sb = new StringBuilder();
				
		for (Iterator<String> i = props.keySet().iterator(); i.hasNext();) {
			String key = i.next();		
			sb.append(URLEncoder.encode(key, "UTF-8"));
			sb.append("=");
			sb.append(URLEncoder.encode((String) props.get(key), "UTF-8"));

			if (i.hasNext()) {
				sb.append("&");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Given a variable number of <String, String> pairs, construct a Map and
	 * return it with values loaded.
	 * 
	 * @param elements
	 *            name1, value1, name2, value2...
	 * @return a Map and return it with values loaded.
	 */
	public static Map<String, String> toMap(String... elements) {
		if (elements.length % 2 != 0) {
			throw new IllegalStateException("Input parameters must be even.");
		}

		Iterator<String> i = Arrays.asList(elements).iterator();
		Map<String, String> m = new HashMap<String, String>();

		while (i.hasNext()) {
			m.put(i.next().toString(), i.next());
		}

		return m;
	}
	
// Private methods
	
	/**
	 * Create a byte array from the contents of an input stream.
	 * 
	 * @param in
	 *            InputStream to turn into a byte array
	 * @return byte array (byte[]) w/ contents of input stream
	 * @throws IOException
	 *             on I/O error
	 */
	private static byte[] streamToByteArray(InputStream in) throws IOException {
		validateArguments(in);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int read = 0;
		byte[] buff = new byte[COPY_BUFFER_SIZE];

		while ((read = in.read(buff)) > 0) {
			os.write(buff, 0, read);
		}
		os.close();

		return os.toByteArray();
	}
	
	/**
	 * Create multipart form boundary.
	 * 
	 * @return boiundry as a String
	 */
	private static String createMultipartBoundary() {
		if (RNG == null)
			RNG = new Random();
		
		StringBuilder buf = new StringBuilder(42);
		buf.append("---------------------------");

		for (int i = 0; i < RANDOM_CHAR_COUNT; i++) {
			if (RNG.nextBoolean())
				buf.append((char) (RNG.nextInt(25) + 65));
			else
				buf.append((char) (RNG.nextInt(25) + 98));
		}
		
		return buf.toString();
	}

	
	/**
	 * Create a byte array from the contents of an input stream.
	 * 
	 * @param in
	 *            InputStream to turn into a byte array
	 * @return byte array (byte[]) w/ contents of input stream
	 * @throws IOException
	 *             on I/O error
	 */
	private static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
		
		int read = 0;
		long size = 0;
		byte[] buff = new byte[COPY_BUFFER_SIZE];

		while ((read = inputStream.read(buff)) > 0) {
			outputStream.write(buff, 0, read);
			size += read;
		}
		
		outputStream.flush();
		
		return size;
	}	
	
	private static void validateArguments(Object ... args) {
		for (int i = 0; i < args.length; ++i)
			if (args[i] == null)
				throw new IllegalArgumentException("An input parameter is null.");
	}

	private class DefaultConnectionProvider implements ConnectionProvider {

		public HttpURLConnection getConnection(String urlStr) throws IOException {
			URL url = new URL(urlStr);
			return (HttpURLConnection) url.openConnection();
		}

	}
	
	private void writeRequestBody(HttpURLConnection connection, byte[] content) throws IOException {
		if (content != null) {
			connection.setRequestProperty("Content-Length", Long.toString(content.length));
			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(content);			
			outputStream.close();			
		} else {
			connection.setRequestProperty("Content-Length", Long.toString(0));
		}
	}
	
	private final class URLBuilderImpl implements URLBuilder {
		private final List<String> segments;
		private boolean httpsScheme;
		
		public URLBuilderImpl() {
			segments = new ArrayList<String>();
			httpsScheme = false;
		}
		
		private URLBuilderImpl(List<String> segments, boolean httpsScheme) {
			this.segments = segments;
			this.httpsScheme = httpsScheme;
		}
		
		@Override
		public URLBuilder append(String ... sgmnts) {
			validateArguments(sgmnts);
			
			if (sgmnts.length == 1)
				appendSingle(sgmnts[0]);
			else
				for (String segment : Arrays.asList(sgmnts)) {
					appendSingle(segment);
				}
			
			return this;
		}
		
		private URLBuilder appendSingle(String segment) {
			segment = segment.trim();
			
			if (segment.length() == 0)
				return this;
			else if (segment.indexOf('/', 1) > -1) {
				for (String nseg : Arrays.asList(segment.split("/"))) 
					this.append(nseg);
			} else if (segment.length() > 0) {
				if (segment.toUpperCase().startsWith("HTTP:"))					
						return this;
				else if (segment.toUpperCase().startsWith("HTTPS:")) {
					httpsScheme = true;
					return this;
				}
				
				segments.add(stripIllegalChars(segment));
			}
			
			return this;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			
			if (httpsScheme)
				sb.append("https://");
			else 
				sb.append("http://");
			
			for (Iterator<String> i = segments.iterator(); i.hasNext(); ) {
				sb.append(i.next());
				
				if (i.hasNext())
					sb.append('/');
			}
				
			return sb.toString();
		}

		private String stripIllegalChars(String segment) {
			return segment.replaceAll("/", "");			
		}

		@Override
		public URLBuilder setHttps(boolean value) {
			httpsScheme = value;
			return this;
		}
		
		@Override
		protected Object clone() throws CloneNotSupportedException {			
			return new URLBuilderImpl(new ArrayList<String>(segments), httpsScheme);
		}

		@Override
		public URLBuilder copy() {
			try {
				return (URLBuilder) this.clone();
			} catch (CloneNotSupportedException e) {
				throw new RuntimeException("Invalid state", e);
			}
		}

		@Override
		public URLBuilder copy(String ... segments) {	
			validateArguments(segments);
			
			return this.copy().append(segments);
		}
	}
	
	/**
     * Create map of known HTTP response codes and their text labels.
     * 
     * @return a Map<Integer, String> of responses 
     */
    private static final Map<Integer, String> createResponseMap()
    {
        Map<Integer, String> responses = new HashMap<Integer, String>();

        //Responses is a map, with error code as key, and a 2 dimensional String array with header and HTML error response, segmented where a user error-defined can be specified.
        responses.put(Integer.valueOf(100), "Continue");
        responses.put(Integer.valueOf(101), "Switching Protocols");

        responses.put(Integer.valueOf(200), "OK");
        responses.put(Integer.valueOf(201), "Created");
        responses.put(Integer.valueOf(202), "Accepted");
        responses.put(Integer.valueOf(203), "Non-Authoritative Information");
        responses.put(Integer.valueOf(204), "No Content");
        responses.put(Integer.valueOf(205), "Reset Content");
        responses.put(Integer.valueOf(206), "Partial Content");

        //TODO: Add all 3xx codes
        responses.put(Integer.valueOf(300), "Multiple Choices");
        responses.put(Integer.valueOf(301), "Moved Permanently");
        responses.put(Integer.valueOf(302), "Found");
        responses.put(Integer.valueOf(307), "Temporary Redirect");

        responses.put(Integer.valueOf(400),
            "Bad Request - HTTP 1.1 requests must include the Host: header.");
        responses.put(Integer.valueOf(401), "Unauthorized");
        responses.put(Integer.valueOf(402), "Payment Required");
        responses.put(Integer.valueOf(403), "Forbidden");
        responses.put(Integer.valueOf(404), "Not Found");
        responses.put(Integer.valueOf(405), "Method not Allowed");
        responses.put(Integer.valueOf(406), "Not Acceptable");
        responses.put(Integer.valueOf(407), "Proxy Authentication Required");
        responses.put(Integer.valueOf(408), "Request Timeout");
        responses.put(Integer.valueOf(409), "Conflict");
        responses.put(Integer.valueOf(410), "Gone");
        responses.put(Integer.valueOf(411), "Length Required");
        responses.put(Integer.valueOf(412), "Precondition Failed");
        responses.put(Integer.valueOf(413), "Request Entity too Large");
        responses.put(Integer.valueOf(414), "Request-URI too Long");
        responses.put(Integer.valueOf(415), "Unsupported Media Type");
        responses.put(Integer.valueOf(416), "Requested Range not Satisfiable");
        responses.put(Integer.valueOf(417), "Expectation Failed");

        responses.put(Integer.valueOf(500), "Internal Server Error");
        responses.put(Integer.valueOf(501), "Not Implemented");
        responses.put(Integer.valueOf(502), "Bad Gateway");
        responses.put(Integer.valueOf(503), "Service Unavailable");
        responses.put(Integer.valueOf(504), "Gateway Timeout");
        responses.put(Integer.valueOf(505), "HTTP Version not Supported");

        return responses;
    }
}
