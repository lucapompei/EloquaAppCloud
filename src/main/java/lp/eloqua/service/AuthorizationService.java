package lp.eloqua.service;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import lp.eloqua.dao.AuthorizationDAO;
import lp.eloqua.model.AuthorizationModel;
import lp.eloqua.model.GrantAppModel;
import lp.eloqua.model.RefreshAccessTokenModel;
import lp.eloqua.model.RefreshAccessTokenResponseModel;
import lp.eloqua.util.RestUtils;
import lp.eloqua.util.TextUtils;

/**
 * This service manages the authentication with Eloqua
 * 
 * @author lucapompei
 */
@Service
public class AuthorizationService {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = Logger.getLogger(AuthorizationService.class);

	/**
	 * Eloqua api endpoints
	 */
	private static final String ELOQUA_API_AUTHORIZE = "https://login.eloqua.com/auth/oauth2/authorize";
	private static final String ELOQUA_API_TOKEN = "https://login.eloqua.com/auth/oauth2/token";
	private static final String ELOQUA_API_ID = "https://login.eloqua.com/id";

	/**
	 * Basic auth properties
	 */
	@Value("${basic.company}")
	private String company;
	@Value("${basic.username}")
	private String username;
	@Value("${basic.password}")
	private String password;

	/**
	 * OAuth2 properties
	 */
	@Value("${oauth.client_id}")
	private String clientId;
	@Value("${oauth.client_secret}")
	private String clientSecret;
	@Value("${oauth.redirect_uri}")
	private String redirectUri;

	/**
	 * The rest template
	 */
	@Autowired
	private RestTemplate restTemplate;

	/**
	 * Cached object
	 */
	private String cachedBasicAuthentication;

	/**
	 * The authorization dao
	 */
	@Autowired
	private AuthorizationDAO authorizationDAO;

	/**
	 * Compose and retrieve the basic authentication
	 * 
	 * @return the basic authentication
	 * @throws Exception
	 */
	public String getBasicAuthentication() {
		if (cachedBasicAuthentication == null) {
			// lazy initialization
			String authentication = company + "\\" + username + ":" + password;
			cachedBasicAuthentication = "Basic " + TextUtils.base64Encode(authentication);
		}
		return cachedBasicAuthentication;
	}

	/**
	 * Return the install url used by Eloqua during installation
	 * 
	 * @param state,
	 *            optional parameter using during oauth authorization phases
	 * @return the install url used by Eloqua during installation
	 */
	public String getInstallUrl(String state) {
		return ELOQUA_API_AUTHORIZE + "?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri
				+ "&state=" + state;
	}

	/**
	 * Request a new access and refresh token from Eloqua using the given grant
	 * token
	 * 
	 * @param grantToken,
	 *            the grant token used to request a new access and refresh token
	 * @param installId,
	 *            the current install id
	 * @throws Exception,
	 *             an exception if some error occurs
	 */
	@Transactional
	public void getAccessAndRefreshTokenUsingGrantToken(String grantToken, String installId) throws Exception {
		// compose the header
		HttpHeaders headers = getAuthenticationHeader(getApplicationBasicAuthorization());
		// compose the body
		GrantAppModel grantAppModel = new GrantAppModel();
		grantAppModel.setGrantType("authorization_code");
		grantAppModel.setCode(grantToken);
		grantAppModel.setRedirectUri(redirectUri);
		// enclose the header and the body
		HttpEntity<GrantAppModel> grantAppTokenEntity = new HttpEntity<>(grantAppModel, headers);
		// request access and refresh token
		RefreshAccessTokenResponseModel refreshAccessTokenResponseModel = this.restTemplate
				.exchange(ELOQUA_API_TOKEN, HttpMethod.POST, grantAppTokenEntity, RefreshAccessTokenResponseModel.class)
				.getBody();
		AuthorizationModel authorizationModel = getAuthorizationUsingGrantResponse(refreshAccessTokenResponseModel,
				installId);
		this.authorizationDAO.insertOrUpdateAuthorization(authorizationModel);
	}

	/**
	 * Retrieve the authentication through oauth
	 * 
	 * @return the oauth authentication
	 * @throws Exception,
	 *             an exception if some error occurs
	 */
	@Transactional
	public String getOAuthAuthentication() throws Exception {
		// get authorization from db
		AuthorizationModel authorizationModel = this.authorizationDAO.getAuthorizationByCompany(company);
		if (authorizationModel == null) {
			LOGGER.error("Authorization not found on db for the company " + company);
			return null;
		}
		// validate authorization if it is expired or not valid
		if (!isValidAccessToken(authorizationModel.getAccessTokenExpirationTime())) {
			LOGGER.debug("Access token is expired or not valid, a new one will be asked");
			// require a new access and refresh token from Eloqua
			authorizationModel = updateAndGetAuthorization(authorizationModel);
		} else {
			// validate the current authorization
			authorizationModel = validateAuthorization(authorizationModel);
		}
		// return a ready-to-use authorization
		String authorization = "Bearer " + authorizationModel.getAccessToken();
		LOGGER.debug("Current valid authorization is " + authorization);
		return authorization;
	}

	/**
	 * Compose and retrieve the http headers with authentication
	 * 
	 * @param authorization,
	 *            the authorization
	 * @return the http headers with authentication
	 */
	private HttpHeaders getAuthenticationHeader(String authorization) {
		HttpHeaders headers = RestUtils.getHeaders();
		headers.set("Authorization", authorization);
		return headers;
	}

	/**
	 * Check if the current access token is valid or expired
	 * 
	 * @param accessTokenExpirationTime,
	 *            the access token expiration time
	 * @return a boolean indicating if the access token is valid or expired
	 */
	private boolean isValidAccessToken(Date accessTokenExpirationTime) {
		// preliminary check on expiration time
		if (accessTokenExpirationTime == null) {
			LOGGER.error("The expiration time is null, so the access token will be discarded");
			return false;
		}
		// check if the access token is near its expiration
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 1);
		return cal.getTime().after(accessTokenExpirationTime);
	}

	/**
	 * Validate the given authorization testing it
	 * 
	 * @param authorizationModel,
	 *            the authorization to validate
	 * @return a validated authorization
	 * @throws Exception,
	 *             an exception if some error occurs
	 */
	private AuthorizationModel validateAuthorization(AuthorizationModel authorizationModel) throws Exception {
		// test the current authorization and if necessary update it
		if (isEloquaCallTestPassed(authorizationModel.getAccessToken())) {
			LOGGER.debug("The current authorization has been validated");
			return authorizationModel;
		} else {
			LOGGER.debug("The current authorization has not been validated, a new one will be asked");
			return updateAndGetAuthorization(authorizationModel);
		}
	}

	/**
	 * Test the given access token using it calling Eloqua
	 * 
	 * @param accessToken,
	 *            the access token to test
	 * @return a boolean indicating if the access token is valid or not
	 */
	private boolean isEloquaCallTestPassed(String accessToken) {
		String authorization = "Bearer " + accessToken;
		HttpEntity<String> entity = new HttpEntity<>(getAuthenticationHeader(authorization));
		return restTemplate.exchange(ELOQUA_API_ID, HttpMethod.GET, entity, String.class)
				.getStatusCode() == HttpStatus.OK;
	}

	/**
	 * Update and retrieve a new authorization through Eloqua
	 * 
	 * @param authorizationModel,
	 *            the entity used to update the authorization
	 * @return a new valid authorization
	 * @throws Exception,
	 *             an exception if some error occurs
	 */
	private AuthorizationModel updateAndGetAuthorization(AuthorizationModel authorizationModel) throws Exception {
		// compose the header
		HttpHeaders headers = getAuthenticationHeader(getApplicationBasicAuthorization());
		// compose the body
		RefreshAccessTokenModel refreshAccessTokenModel = new RefreshAccessTokenModel();
		refreshAccessTokenModel.setGrantType("refresh_token");
		refreshAccessTokenModel.setRefreshToken(authorizationModel.getRefreshToken());
		refreshAccessTokenModel.setScope("full");
		refreshAccessTokenModel.setRedirectUri(this.redirectUri);
		// enclose the header and the body
		HttpEntity<RefreshAccessTokenModel> refreshAccessTokenEntity = new HttpEntity<>(refreshAccessTokenModel,
				headers);
		// refresh the token on Eloqua
		RefreshAccessTokenResponseModel refreshAccessTokenResponseModel = this.restTemplate.exchange(ELOQUA_API_TOKEN,
				HttpMethod.POST, refreshAccessTokenEntity, RefreshAccessTokenResponseModel.class).getBody();
		// update authorization model
		AuthorizationModel updatedAuthorizationModel = getAuthorizationUsingGrantResponse(
				refreshAccessTokenResponseModel, authorizationModel.getInstallId());
		// store it on db
		this.authorizationDAO.insertOrUpdateAuthorization(updatedAuthorizationModel);
		// use response for db update
		return updatedAuthorizationModel;
	}

	/**
	 * Compose the authorization model entity using the grant response and the
	 * install id
	 * 
	 * @param refreshAccessTokenResponseModel,
	 *            the response obtained after a request to Eloqua for updating
	 *            tokens
	 * @param installId,
	 *            the current install id
	 * @return the authorization model
	 */
	private AuthorizationModel getAuthorizationUsingGrantResponse(
			RefreshAccessTokenResponseModel refreshAccessTokenResponseModel, String installId) {
		// initialize authorization model
		AuthorizationModel authorizationModel = new AuthorizationModel();
		authorizationModel.setCompany(company);
		authorizationModel.setInstallId(installId);
		// complete authorization using eloqua grant response
		Calendar accessTokenExpirationTime = Calendar.getInstance();
		accessTokenExpirationTime.add(Calendar.SECOND, refreshAccessTokenResponseModel.getExpiresIn());
		Calendar refreshTokenExpirationTime = Calendar.getInstance();
		refreshTokenExpirationTime.add(Calendar.YEAR, 1);
		authorizationModel.setAccessToken(refreshAccessTokenResponseModel.getAccessToken());
		authorizationModel.setRefreshToken(refreshAccessTokenResponseModel.getRefreshToken());
		authorizationModel.setAccessTokenExpirationTime(accessTokenExpirationTime.getTime());
		authorizationModel.setRefreshTokenExpirationTime(refreshTokenExpirationTime.getTime());
		return authorizationModel;
	}

	/**
	 * Compose and retrieve the basic authentication for the current application
	 * 
	 * @return the basic authentication for the current application
	 */
	private String getApplicationBasicAuthorization() {
		String authentication = clientId + ":" + clientSecret;
		return "Basic " + TextUtils.base64Encode(authentication);
	}

}