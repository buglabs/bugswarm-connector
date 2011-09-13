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
			return new String(streamToByteArray(input));
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
	public static final ResponseDeserializer<InputStream> PASSTHROUGH = new ResponseDeserializer<InputStream>() {

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
				throw new IOException("HTTP Error " + code + " was returned from the server.");
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
				throw new IOException("HTTP Error " + code + " was returned from the server.");			
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

	//private ResponseDeserializer<?> responseDeserializers;
	
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
		
		if (headers != null && headers.size() > 0)
			for (Map.Entry<String, String> entry : headers.entrySet())
				connection.addRequestProperty(entry.getKey(), entry.getValue());

		for (ConnectionInitializer initializer : connectionInitializers)
			initializer.initialize(connection);

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
			break;
		case PUT:
			connection.setDoOutput(true);
			baos = new ByteArrayOutputStream();
			copy(content, baos);
			writeRequestBody(connection, baos.toByteArray());
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
				if (isError())
					if (errorHandler == null) {
						return null;
					} else {
						errorHandler.handleError(getCode());
						return null;
					}
				
				if (deserializer == null) {
					T response = (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream(), 0, null);
					callEnd = System.currentTimeMillis();
					done = true;
					return (T) response;
				}
				
				T response = (T) deserializer.deserialize(connection.getInputStream(), connection.getResponseCode(), connection.getHeaderFields());
				callEnd = System.currentTimeMillis();
				done = true;
				return response;				
			}
			
		};				
	}
	
	/**
	 * Execute GET method and return body as a string.
	 * @param url of server
	 * @return body as a String
	 * @throws IOException on I/O error
	 */
	public String getAsString(String url) throws IOException {		
		return getContent(url, STRING_DESERIALIZER);
	}
	
	
	/**
	 * Execute GET method and return body deserizalized.
	 * 
	 * @param url of server
	 * @param deserializer ResponseDeserializer
	 * @return T deserialized object
	 * @throws IOException on I/O error
	 */
	public <T> T getContent(String url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url, deserializer, null, null).getContent();
	}
	
	/**
	 * Execute GET method and deserialize response.
	 * 
	 * @param url of server
	 * @param deserializer class that can deserialize content into desired type.
	 * @return type specified by deserializer
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> get(String url, Map<String, String> headers, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url, deserializer, null, headers);
	}
	
	/**
	 * Execute GET method and deserialize response.
	 * 
	 * @param url of server
	 * @param deserializer class that can deserialize content into desired type.
	 * @return type specified by deserializer
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> get(String url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url, deserializer, null, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param body body of post as an input stream
	 * @return a response to the request
	 * @throws IOException on I/O error
	 */
	public Response<Integer> post(String url, InputStream body) throws IOException {
		return call(HttpMethod.POST, url, HTTP_CODE_DESERIALIZER, body, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> post(String url, Map<String, String> formData) throws IOException {
		return call(HttpMethod.POST, url, HTTP_CODE_DESERIALIZER, 
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
	public <T> Response<T> post(String url, InputStream body, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url, deserializer, body, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> post(String url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url, deserializer, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}
	
	/**
	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> postMultipart(String url, Map<String, Object> content) throws IOException {
		return call(HttpMethod.POST, url, HTTP_CODE_DESERIALIZER, createMultipartPostBody(content), null);
	}
	
	/**
	 * Send a multipart POST to the server.  Convenience method for post(url, createMultipartPostBody(content)).
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> postMultipart(String url, Map<String, Object> content, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.POST, url, deserializer, createMultipartPostBody(content), null);
	}
	
	/**
	 * Call PUT method on a server.
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> put(String url, InputStream content) throws IOException {
		return call(HttpMethod.PUT, url, HTTP_CODE_DESERIALIZER, content, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public Response<Integer> put(String url, Map<String, String> formData) throws IOException {
		return call(HttpMethod.PUT, url, HTTP_CODE_DESERIALIZER, 
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
	public <T> Response<T> put(String url, InputStream content, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.PUT, url, deserializer, content, null);
	}
	
	/**
	 * Send a POST to the server.
	 * 
	 * @param url url of server
	 * @param formData Form data as strings.  
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> put(String url, Map<String, String> formData, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.PUT, url, deserializer, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
	}

	
	/**
	 * Call DELETE method on a server.
	 * 
	 * @param url of server
	 * @return HTTP response from server
	 * @throws IOException on I/O error
	 */
	public Response<Integer> delete(String url) throws IOException {
		return call(HttpMethod.DELETE, url, HTTP_CODE_DESERIALIZER, null, null);
	}
	
	/**
	 * Call DELETE method on a server.
	 * 
	 * @param url of server
	 * @return HTTP response from server
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> delete(String url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.DELETE, url, deserializer, null, null);
	}
	
	/**
	 * Call HEAD method on a server.
	 * 
	 * @param url of server
	 * @return HTTP Response from server
	 * @throws IOException on I/O error
	 */
	public Response<Integer> head(String url) throws IOException {
		return call(HttpMethod.HEAD, url, HTTP_CODE_DESERIALIZER, null, null);
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
}
