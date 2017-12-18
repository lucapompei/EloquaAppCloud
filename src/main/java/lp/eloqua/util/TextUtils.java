package lp.eloqua.util;

import org.apache.commons.codec.binary.Base64;

/**
 * This class exposes utils to handle generic text operations
 *
 * @author lucapompei
 */
public class TextUtils {

	/**
	 * Private constructor for an utility class, construct a new {@code TextUtils}
	 */
	private TextUtils() {
		// Empty implementation
	}

	/**
	 * This method checks if the passed value is {@code null} or empty
	 *
	 * @param value,
	 *            the string value to check
	 * @return a {@code boolean} indicating if the checked string value is
	 *         {@code null} or empty or not
	 */
	public static boolean isNullOrEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * Encode the given value in base 64
	 * 
	 * @param value,
	 *            the value to encode
	 * @return the encoded value
	 */
	public static String base64Encode(String value) {
		return Base64.encodeBase64String(value.getBytes());
	}

}
