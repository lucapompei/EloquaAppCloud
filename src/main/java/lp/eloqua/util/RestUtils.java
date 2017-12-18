package lp.eloqua.util;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * This class exposes utils to handle rest operations
 * 
 * @author lucapompei
 *
 */
public class RestUtils {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(JsonUtils.class);

	/**
	 * Cached object
	 */
	private static HttpHeaders cachedHeaders;

	/**
	 * Private constructor for a new {@code RestUtils}
	 */
	private RestUtils() {
		// Empty implementation
	}

	/**
	 * Get a generic http headers
	 * 
	 * @return a generic http headers
	 */
	public static HttpHeaders getHeaders() {
		if (cachedHeaders != null) {
			LOGGER.debug("Using cached HttpHeader");
			return cachedHeaders;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=utf-8");
		// update cache
		cachedHeaders = headers;
		return headers;
	}

	/**
	 * Get a response entity with the given httpStatus using object
	 * 
	 * @param object,
	 *            an optional param to indicate the entity to return
	 * @param httpStatus,
	 *            the httpsStatus to return
	 * @return response entity with the given httpStatus using object
	 */
	public static ResponseEntity<String> getResponseEntity(Object object, HttpStatus httpStatus) {
		return new ResponseEntity<>(object == null ? null : JsonUtils.toJson(object), getHeaders(), httpStatus);
	}

	/**
	 * Get a response entity with 200 OK HTTP status using object
	 * 
	 * @param object,
	 *            an optional param to indicate the entity to return
	 * @return response entity with 200 OK HTTP status using object
	 */
	public static ResponseEntity<String> getResponseEntity(Object object) {
		return getResponseEntity(object, HttpStatus.OK);
	}

}
