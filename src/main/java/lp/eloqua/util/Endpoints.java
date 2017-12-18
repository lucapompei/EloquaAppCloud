package lp.eloqua.util;

/**
 * This class exposes the endpoint urls used by the app
 * 
 * @author lucapompei
 */
public class Endpoints {

	/**
	 * Private constructor for an utility class, construct a new empty
	 * {@link Endpoints}
	 */
	private Endpoints() {
		// Empty implementation
	}

	/**
	 * List of cloud content endpoints
	 */
	public static final String ENDPOINT_HOME = "/";
	public static final String ENDPOINT_ENABLE_URL = "/enable";
	public static final String ENDPOINT_CONFIGURE_URL = "/configure";
	public static final String ENDPOINT_STATUS_URL = "/status";
	public static final String ENDPOINT_UNISTALL_URL = "/uninstall";
	public static final String ENDPOINT_GRANT_URL = "/grant";
	public static final String ENDPOINT_GET_AUTHORIZATION = "/getAuthorization";

}