
package lp.eloqua.model;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "grant_type", "code", "redirect_uri" })
public class GrantAppModel {

	@JsonProperty("grant_type")
	private String grantType;
	@JsonProperty("code")
	private String code;
	@JsonProperty("redirect_uri")
	private String redirectUri;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("grant_type")
	public String getGrantType() {
		return grantType;
	}

	@JsonProperty("grant_type")
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}

	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	@JsonProperty("code")
	public void setCode(String code) {
		this.code = code;
	}

	@JsonProperty("redirect_uri")
	public String getRedirectUri() {
		return redirectUri;
	}

	@JsonProperty("redirect_uri")
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
