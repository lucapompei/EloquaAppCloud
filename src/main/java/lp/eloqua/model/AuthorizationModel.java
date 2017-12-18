package lp.eloqua.model;

import java.util.Date;

public class AuthorizationModel {

	/**
	 * Entity model properties
	 */
	private String accessToken;
	private Date accessTokenExpirationTime;
	private String company;
	private String refreshToken;
	private Date refreshTokenExpirationTime;
	private String installId;

	/**
	 * Construct a new empty {@link AuthorizationModel}
	 */
	public AuthorizationModel() {
		// Empty implementation
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public Date getAccessTokenExpirationTime() {
		return accessTokenExpirationTime;
	}

	public void setAccessTokenExpirationTime(Date accessTokenExpirationTime) {
		this.accessTokenExpirationTime = accessTokenExpirationTime;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Date getRefreshTokenExpirationTime() {
		return refreshTokenExpirationTime;
	}

	public void setRefreshTokenExpirationTime(Date refreshTokenExpirationTime) {
		this.refreshTokenExpirationTime = refreshTokenExpirationTime;
	}

	public String getInstallId() {
		return installId;
	}

	public void setInstallId(String installId) {
		this.installId = installId;
	}

}
