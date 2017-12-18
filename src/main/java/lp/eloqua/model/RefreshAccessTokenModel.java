
package lp.eloqua.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "grant_type", "refresh_token", "scope", "redirect_uri" })
public class RefreshAccessTokenModel {

	@JsonProperty("grant_type")
	private String grantType;
	@JsonProperty("refresh_token")
	private String refreshToken;
	@JsonProperty("scope")
	private String scope;
	@JsonProperty("redirect_uri")
	private String redirectUri;

	@JsonProperty("grant_type")
	public String getGrantType() {
		return grantType;
	}

	@JsonProperty("grant_type")
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	@JsonProperty("refresh_token")
	public String getRefreshToken() {
		return refreshToken;
	}

	@JsonProperty("refresh_token")
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@JsonProperty("scope")
	public String getScope() {
		return scope;
	}

	@JsonProperty("scope")
	public void setScope(String scope) {
		this.scope = scope;
	}

	@JsonProperty("redirect_uri")
	public String getRedirectUri() {
		return redirectUri;
	}

	@JsonProperty("redirect_uri")
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

}
