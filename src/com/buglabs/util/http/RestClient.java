package com.buglabs.util.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author kgilmer
 *
 * @param <T> Type that will be returned from Response.getBody().
 */
public class RestClient<T> {
	
	private static final String HEADER_CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
	
	private static final int COPY_BUFFER_SIZE = 1024 * 4;
	
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
	 * The response from the server for a given request.
	 *
	 * @param <T>
	 */
	public interface Response<T> {
		/**
		 * @return The deserialized content of the request.
		 * @throws IOException on I/O error
		 */
		T getBody() throws IOException;
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
		 * @return true if error code or an exception is raised, false otherwise.
		 */
		boolean isError();
		
		/**
		 * @throws IOException if the response is an error
		 */
		void throwOnError() throws IOException;
	}

	private ConnectionProvider connectionProvider;

	private List<ConnectionInitializer> connectionInitializers;

	private ResponseDeserializer<T> responseDeserializers;
	
	/**
	 * Default constructor.
	 */
	public RestClient() {
		this.connectionProvider = new DefaultConnectionProvider();
		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
		this.responseDeserializers = null;
	}
	
	/**
	 * @param connectionProvider
	 */
	public RestClient(ConnectionProvider connectionProvider) {
		this.connectionProvider = connectionProvider;
		this.connectionInitializers = new ArrayList<ConnectionInitializer>();
		this.responseDeserializers = null;
	}
	
	/**
	 * @param initializer
	 */
	public RestClient(ConnectionInitializer initializer) {
		this();
		connectionInitializers.add(initializer);
	}
	
	/**
	 * @param connectionProvider
	 * @param initializer
	 */
	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer) {
		this(connectionProvider);
		connectionInitializers.add(initializer);
	}
	
	/**
	 * @param deserializer
	 */
	public RestClient(ResponseDeserializer<T> deserializer) {
		this();
		this.responseDeserializers = deserializer;
	}
	
	/**
	 * @param initializer
	 * @param deserializer
	 */
	public RestClient(ConnectionInitializer initializer, ResponseDeserializer<T> deserializer) {
		this(initializer);
		this.responseDeserializers = deserializer;
	}
	
	/**
	 * @param connectionProvider
	 * @param initializer
	 * @param deserializer
	 */
	public RestClient(ConnectionProvider connectionProvider, ConnectionInitializer initializer, ResponseDeserializer<T> deserializer) {
		this(connectionProvider, initializer);
		this.responseDeserializers = deserializer;
	}
	
	// Public methods
	
	/**
	 * @param provider
	 */
	public void setConnectionProvider(ConnectionProvider provider) {
		this.connectionProvider = provider;
	}
	
	/**
	 * @return
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
	 * @param initializer
	 * @return
	 */
	public boolean removeConnectionInitializer(ConnectionInitializer initializer) {
		return connectionInitializers.remove(initializer);
	}
	
	/**
	 * @param deserializer
	 */
	public void setResponseDeserializer(ResponseDeserializer<T> deserializer) {	
		responseDeserializers = deserializer;
	}
	
	/**
	 * @param method
	 * @param url
	 * @param deserializer
	 * @return
	 * @throws IOException
	 */
	public Response<T> call(HttpMethod method, String url, ResponseDeserializer<T> deserializer, InputStream content, Map<String, String> headers) throws IOException {
		validateArguments(method, url);
		
		String httpUrl = url;
		if (!url.toLowerCase().startsWith("http://"))
			httpUrl = "http://" + url;
				
		HttpURLConnection connection = connectionProvider.getConnection(httpUrl);
		
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
			OutputStream outputStream = connection.getOutputStream();
			long length = copy(content, outputStream);
			outputStream.close();
			connection.setRequestProperty("Content-Length", Long.toString(length));
			break;
		case PUT:
			throw new RuntimeException("Unimplemented");
		case DELETE:
			throw new RuntimeException("Unimplemented");
		case HEAD:
			throw new RuntimeException("Unimplemented");
		default:
			throw new RuntimeException("Unhandled HTTP method.");
		}	
		
		return new ResponseImpl(method, url, connection, deserializer);
	}
	
	/**
	 * @param method
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Response<T> call(HttpMethod method, String url) throws IOException {
		return call(method, url, responseDeserializers, null, null);
	}
	
	/**
	 * @param method
	 * @param url
	 * @param inputStream
	 * @return
	 * @throws IOException
	 */
	public Response<T> call(HttpMethod method, String url, InputStream inputStream) throws IOException {
		return call(method, url, responseDeserializers, inputStream, null);
	}
	
	/**
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Response<T> get(String url) throws IOException {
		return call(HttpMethod.GET, url, null);
	}
	
	/**
	 * @param url
	 * @param deserializer
	 * @return
	 * @throws IOException
	 */
	public Response<T> get(String url, ResponseDeserializer<T> deserializer) throws IOException {
		return call(HttpMethod.GET, url, deserializer, null, null);
	}
	
	/**
	 * @param url
	 * @param content
	 * @return
	 * @throws IOException
	 */
	public Response<T> post(String url, InputStream content) throws IOException {
		return call(HttpMethod.POST, url, content);
	}
	
	/**
	 * @param url
	 * @param formData
	 * @return
	 * @throws IOException
	 */
	public Response<T> post(String url, Map<String, String> formData) throws IOException {
		return call(HttpMethod.POST, url, null, 
				new ByteArrayInputStream(propertyString(formData).getBytes()), 
				toMap(HEADER_CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED));
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
	
	/**
	 * @author kgilmer
	 *
	 */
	private class ResponseImpl implements Response<T> {

		private final HttpMethod method;
		private final String url;
		private final HttpURLConnection connection;
		private final ResponseDeserializer<T> deserializer;

		/**
		 * @param method
		 * @param url
		 * @param connection
		 * @param deserializer
		 */
		public ResponseImpl(HttpMethod method, String url, HttpURLConnection connection, ResponseDeserializer<T> deserializer) {
			this.method = method;
			this.url = url;
			this.connection = connection;
			this.deserializer = deserializer;
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
		public T getBody() throws IOException {		
			if (deserializer == null) {
				return (T) RestClient.STRING_DESERIALIZER.deserialize(connection.getInputStream());
			}
				
			return deserializer.deserialize(connection.getInputStream());
		}

		@Override
		public void throwOnError() throws IOException {
			int code = getCode();
				
			if (code >= 400 && code < 500)
				throw new IOException("Server returned HTTP error code " + code);
		}		
	}
}
