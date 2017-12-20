package lp.eloqua.dao;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lp.eloqua.model.AuthorizationModel;

/**
 * The authorization DAO responsible for db operation for the authorization
 * model entity
 * 
 * @author lucapompei
 *
 */
@Repository
public class AuthorizationDAO {

	/**
	 * The logger
	 */
	private static final Logger LOGGER = Logger.getLogger(AuthorizationDAO.class);

	/**
	 * Database properties
	 */
	private static final String APPCLOUD_AUTH_TABLE = "appcloud_authentication";

	/**
	 * The date formatter
	 */
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * The jdbc template
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * Construct a new {@link AuthorizationDAO}
	 */
	public AuthorizationDAO() {
		// Empty implementation
	}

	/**
	 * Insert or update authorization data using the given authorization model
	 * entity
	 * 
	 * @param authorizationModel,
	 *            the authorization model used to insert or update authorization
	 *            data on db
	 * @throws Exception,
	 *             an exception if some error occurs
	 */
	public void insertOrUpdateAuthorization(AuthorizationModel authorizationModel) throws Exception {
		// check if auth data are already present on db
		String sqlCheck = "SELECT count(*) AS cnt FROM " + APPCLOUD_AUTH_TABLE + " WHERE company='"
				+ authorizationModel.getCompany() + "'";
		boolean isAlreadyPresent = jdbcTemplate.queryForObject(sqlCheck, Integer.class) == 0 ? false : true;
		String sql = null;
		if (isAlreadyPresent) {
			// update the current auth data
			sql = "UPDATE " + APPCLOUD_AUTH_TABLE + " SET " + "access_token = '" + authorizationModel.getAccessToken()
					+ "', " + "access_token_expiration_time = '"
					+ dateFormatter.format(authorizationModel.getAccessTokenExpirationTime()) + "', "
					+ "refresh_token = '" + authorizationModel.getRefreshToken() + "', "
					+ "refresh_token_expiration_time = '"
					+ dateFormatter.format(authorizationModel.getRefreshTokenExpirationTime()) + "', "
					+ "install_id = '" + authorizationModel.getInstallId() + "' " + "WHERE company = '"
					+ authorizationModel.getCompany() + "'";
		} else {
			// insert new auth data
			sql = "INSERT INTO " + APPCLOUD_AUTH_TABLE + " ( " + "access_token, " + "access_token_expiration_time, "
					+ "company, " + "refresh_token,  " + "refresh_token_expiration_time, " + "install_id "
					+ ") VALUES (" + "'" + authorizationModel.getAccessToken() + "', " + "'"
					+ dateFormatter.format(authorizationModel.getAccessTokenExpirationTime()) + "', " + "'"
					+ authorizationModel.getCompany() + "', " + "'" + authorizationModel.getRefreshToken() + "', " + "'"
					+ dateFormatter.format(authorizationModel.getRefreshTokenExpirationTime()) + "', " + "'"
					+ authorizationModel.getInstallId() + "')";
		}
		LOGGER.debug("Inserting authorization on db");
		jdbcTemplate.execute(sql);
	}

	/**
	 * Get the authorization stored on db for the given company
	 * 
	 * @param company,
	 *            the company used to select and retrieve authorization data
	 * @return the authorization data for the given company
	 */
	public AuthorizationModel getAuthorizationByCompany(String company) {
		LOGGER.debug("Getting authorization from db");
		try {
			String sql = "SELECT access_token as accessToken, "
					+ "access_token_expiration_time as accessTokenExpirationTime, " + "company, "
					+ "refresh_token as refreshToken, "
					+ "refresh_token_expiration_time as refreshTokenExpirationTime, " + "install_id as installId "
					+ "FROM " + APPCLOUD_AUTH_TABLE + " WHERE company='" + company + "'";
			return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(AuthorizationModel.class));
		} catch (Exception ex) {
			LOGGER.error("Unable to retrieve authorization from db");
			return null;
		}
	}

}