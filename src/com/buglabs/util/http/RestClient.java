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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		T deserialize(InputStream input) throws IOException;
	}
	
	/**
	 * A HTTPResponseDeserializer that returns the entire response as a String.
	 */
	public static final ResponseDeserializer<String> STRING_DESERIALIZER = new ResponseDeserializer<String>() {

		@Override
		public String deserialize(InputStream input) throws IOException {			
			return new String(streamToByteArray(input));
		}
	};
	
	/**
	 * A HTTPResponseDeserializer that always returns true.  Good when the response content is ignored.
	 */
	public static final ResponseDeserializer<Boolean> BOOLEAN_DESERIALIZER = new ResponseDeserializer<Boolean>() {

		@Override
		public Boolean deserialize(InputStream input) throws IOException {			
			return Boolean.TRUE;
		}
	};
	
	/**
	 * A HTTPResponseDeserializer that simply returns the internal inputstream.
	 * Useful for clients that wish to handle the response input stream manually.
	 */
	public static final ResponseDeserializer<InputStream> PASSTHROUGH = new ResponseDeserializer<InputStream>() {

		@Override
		public InputStream deserialize(InputStream input) throws IOException {			
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
	public interface Response<T> extends Future<T> {
		
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
	 * @param deserializer ResponseDeserializer
	 */
	/*public void setResponseDeserializer(ResponseDeserializer<?> deserializer) {	
		responseDeserializers = deserializer;
	}*/
	
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

		switch(method) {
		case GET:			
			connection.setDoInput(true);
			connection.setDoOutput(false);
			break;
		case POST:
			connection.setDoOutput(true);						
			writeRequestBody(connection, content);			
			break;
		case PUT:
			connection.setDoOutput(true);
			writeRequestBody(connection, content);
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
			public T get() throws InterruptedException, ExecutionException {
				try {				
					if (deserializer == null) {
						T response = (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream());
						callEnd = System.currentTimeMillis();
						done = true;
						return (T) response;
					}
					
					T response = (T) deserializer.deserialize(connection.getInputStream());
					callEnd = System.currentTimeMillis();
					done = true;
					return response;
				} catch (IOException e) {
					throw new ExecutionException(e);
				}
			}

			@Override
			public T get(long l, TimeUnit timeunit) throws InterruptedException, ExecutionException, TimeoutException {
				throw new ExecutionException("Unimplemented", null);
			}		
		};
				
	}

	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Response<String> get(String url) throws IOException {
		return call(HttpMethod.GET, url, STRING_DESERIALIZER, null, null);
	}
	
	/**
	 * @param url
	 * @param deserializer
	 * @return
	 * @throws IOException
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
	public <T> Response<T> post(String url, ResponseDeserializer<T> deserializer, InputStream body) throws IOException {
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
	public <T> Response<T> post(String url, ResponseDeserializer<T> deserializer, Map<String, String> formData) throws IOException {
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
	public <T> Response<T> postMultipart(String url, ResponseDeserializer<T> deserializer, Map<String, Object> content) throws IOException {
		return post(url, deserializer, createMultipartPostBody(content));
	}
	
	/**
	 * Call PUT method on a server.
	 * 
	 * @param url url of server
	 * @param content See createMultipartPostBody() for details on this parameter.
	 * @return a response from the POST
	 * @throws IOException on I/O error
	 */
	public <T> Response<T> put(String url, ResponseDeserializer<T> deserializer, InputStream content) throws IOException {
		return call(HttpMethod.PUT, url, deserializer, content, null);
	}
	
	// Public static methods
	
	/**
	 * Create a buffer for a multi-part POST body, and return an input stream to the buffer.
	 * 
	 * @param content A map of <String, Object>  The values can either be of type String or 
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
			} else if (entry.getValue() == null) {
				throw new IllegalArgumentException("Content value is null.");
			} else {
				throw new IllegalArgumentException("Unhandled type: " + entry.getValue().getClass().getName());
			}
			
			baos.write(LINE_ENDING.getBytes());
		}
		
		return new ByteArrayInputStream(baos.toByteArray());
		/*
		// add parameters
		Object[] elems = parameters.keySet().toArray();
		StringBuffer buf; // lil helper
		IFormFile file;
		for (int i = 0; i < elems.length; i++) {
			String key = (String) elems[i];
			Object obj = parameters.get(key);
			// System.out.println("--" + key);

			buf = new StringBuffer();
			if (obj instanceof IFormFile) {
				file = (IFormFile) obj;
				buf.append("--" + boundary + LINE_ENDING);
				buf.append(HEADER_PARA);
				buf.append("; " + PARA_NAME + "=\"" + key + "\"");
				buf.append("; " + FILE_NAME + "=\"" + file.getFilename() + "\"" + LINE_ENDING);
				buf.append(HEADER_TYPE + ": " + file.getContentType() + ";");
				buf.append(LINE_ENDING);
				buf.append(LINE_ENDING);
				os.write(buf.toString().getBytes());
				os.write(file.getBytes());
			} else if (obj != null) {
				buf.append("--" + boundary + LINE_ENDING);
				buf.append(HEADER_PARA);
				buf.append("; " + PARA_NAME + "=\"" + key + "\"");
				buf.append(LINE_ENDING);
				buf.append(LINE_ENDING);
				buf.append(obj.toString());
				os.write(buf.toString().getBytes());
			}
			os.write(LINE_ENDING.getBytes());
		}
		os.write(("--" + boundary + "--" + LINE_ENDING).getBytes());*/
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
	
	private void writeRequestBody(HttpURLConnection connection, InputStream content) throws IOException {
		if (content != null) {
			OutputStream outputStream = connection.getOutputStream();
			long length = copy(content, outputStream);
			outputStream.close();
			connection.setRequestProperty("Content-Length", Long.toString(length));
		} else {
			connection.setRequestProperty("Content-Length", Long.toString(0));
		}
	}
	
	
	/**
	 * @author kgilmer
	 *
	 */
	/*private abstract class ResponseImpl implements Response<?> {

		private final HttpMethod method;
		private final String url;
		private final HttpURLConnection connection;
		private final ResponseDeserializer<?> deserializer;
		private final long timeStart;
		private final long callStart;
		private long callEnd;
		private boolean done;
		private boolean cancelled;

		*//**
		 * Constructs a Response.  This constructor will block until the response has been recievied.
		 * @param method
		 * @param url
		 * @param connection
		 * @param deserializer
		 * @param errorHandler 
		 * @param timeStart
		 * @throws IOException 
		 *//*
		public ResponseImpl(HttpMethod method, String url, HttpURLConnection connection, ResponseDeserializer<?> deserializer, ErrorHandler errorHandler, long timeStart) throws IOException {
			this.method = method;
			this.url = url;
			this.connection = connection;
			this.deserializer = deserializer;
			this.timeStart = timeStart;
			
			this.callStart = System.currentTimeMillis();			
		}

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
			return callEnd - callStart;
		}

		@Override
		public long getTotalTime() {		
			return callEnd - timeStart;
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
		public T get() throws InterruptedException, ExecutionException {
			try {				
				if (deserializer == null) {
					T response = (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream());
					callEnd = System.currentTimeMillis();
					done = true;
					return (T) response;
				}
				
				T response = (T) deserializer.deserialize(connection.getInputStream());
				callEnd = System.currentTimeMillis();
				done = true;
				return response;
			} catch (IOException e) {
				throw new ExecutionException(e);
			}
		}

		@Override
		public T get(long l, TimeUnit timeunit) throws InterruptedException, ExecutionException, TimeoutException {
			throw new ExecutionException("Unimplemented", null);
		}		
	}*/
	
}
