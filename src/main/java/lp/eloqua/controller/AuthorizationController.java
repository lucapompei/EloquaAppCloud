package lp.eloqua.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import lp.eloqua.service.AuthorizationService;
import lp.eloqua.util.Endpoints;
import lp.eloqua.util.RestUtils;

/**
 * The REST controller used to handle all Eloqua rest requests and authorization
 * request
 * 
 * @author lucapompei
 *
 */
@RestController
public class AuthorizationController {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = Logger.getLogger(AuthorizationController.class);

	/**
	 * The authorization service
	 */
	@Autowired
	private AuthorizationService authorizationService;

	/**
	 * Basic auth properties
	 */
	@Value("${auth.isOAuthEnabled}")
	private boolean isOAuthEnabled;

	/**
	 * Construct a new empty {@link AuthenticationController}
	 */
	public AuthorizationController() {
		// Empty implementation
	}

	/**
	 * Home page entry point
	 */
	@RequestMapping(value = Endpoints.ENDPOINT_HOME, method = RequestMethod.GET)
	@ResponseStatus(code = HttpStatus.OK)
	public ModelAndView getHomepage() {
		LOGGER.info("Serving home page");
		return new ModelAndView("index");
	}

	/**
	 * Install and configure endpoint
	 */
	@RequestMapping(value = { Endpoints.ENDPOINT_ENABLE_URL + "/{installId}",
			Endpoints.ENDPOINT_CONFIGURE_URL + "/{installId}" }, method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseStatus(code = HttpStatus.OK)
	public void getEnable(@PathVariable String installId,
			@RequestParam(value = "callbackURL", required = false) String callbackURL, HttpServletResponse response) {
		LOGGER.info("[INSTALL/CONFIGURE PHASE] Requesting install for AppCloud with callBackUrl " + callbackURL);
		String state = installId + "|" + callbackURL;
		String installUrl = this.authorizationService.getInstallUrl(state);
		try {
			LOGGER.debug("[INSTALL/CONFIGURE PHASE] redirecting to " + installUrl);
			response.sendRedirect(installUrl);
		} catch (IOException ex) {
			LOGGER.error("Error during redirecting", ex);
		}
	}

	/**
	 * Status endpoint
	 */
	@RequestMapping(value = Endpoints.ENDPOINT_STATUS_URL, method = RequestMethod.GET)
	@ResponseStatus(code = HttpStatus.OK)
	public ResponseEntity<String> getStatusUrl() {
		LOGGER.info("[STATUS PHASE] Requesting status for AppCloud");
		return RestUtils.getResponseEntity(true);
	}

	/**
	 * Home page entry point
	 */
	@RequestMapping(value = Endpoints.ENDPOINT_UNISTALL_URL + "/{installId}", method = RequestMethod.GET)
	@ResponseStatus(code = HttpStatus.OK)
	public ResponseEntity<String> getUninstallUrl(@PathVariable String installId) {
		LOGGER.info("[UNINSTALL PHASE] Requesting uninstall for AppCloud with install id " + installId);
		LOGGER.info("[UNINSTALL PHASE] Uninstall denied");
		return RestUtils.getResponseEntity(false, HttpStatus.UNAUTHORIZED);
	}

	/**
	 * Get grant application endpoit
	 */
	@RequestMapping(value = Endpoints.ENDPOINT_GRANT_URL, method = RequestMethod.GET)
	@ResponseStatus(code = HttpStatus.OK)
	public void grantApplication(@RequestParam("code") String code, @RequestParam("state") String state,
			HttpServletResponse response) {
		LOGGER.info("[GRANT PHASE] Getting request for asking install application on Eloqua with code " + code);
		try {
			LOGGER.debug("Getting state as " + state);
			String[] stateSplit = state.split("\\|");
			String installId = stateSplit[0];
			String callBackUrl = stateSplit[1];
			this.authorizationService.getAccessAndRefreshTokenUsingGrantToken(code, installId);
			LOGGER.debug("Final redirecting to " + callBackUrl);
			response.sendRedirect(callBackUrl);
		} catch (Exception ex) {
			LOGGER.error("Error during the GRANT PHASE", ex);
		}
	}

	/**
	 * Get authorization endpoint
	 */
	@RequestMapping(value = Endpoints.ENDPOINT_GET_AUTHORIZATION, method = RequestMethod.GET)
	@ResponseStatus(code = HttpStatus.OK)
	public synchronized String getAuthorization() {
		LOGGER.info(
				"[REQUESTING AUTH PHASE] Getting request for the Eloqua authorization with isOauthEnabled properties: "
						+ isOAuthEnabled);
		if (isOAuthEnabled) {
			try {
				return this.authorizationService.getOAuthAuthentication();
			} catch (Exception ex) {
				LOGGER.error(
						"[REQUESTING AUTH PHASE] Authorization not found on db. Please, re-install the application.",
						ex);
				return null;
			}
		} else {
			try {
				return this.authorizationService.getBasicAuthentication();
			} catch (Exception ex) {
				LOGGER.error("[REQUESTING AUTH PHASE] Unable to compose basic authentication", ex);
				return null;
			}
		}

	}

}