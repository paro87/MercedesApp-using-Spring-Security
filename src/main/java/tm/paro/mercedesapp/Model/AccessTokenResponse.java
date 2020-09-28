package tm.paro.mercedesapp.Model;

import lombok.Getter;

@Getter
public class AccessTokenResponse {

    private String access_token;
    private String token_type;
    private String expires_in;
    private String scope;
    private String refresh_token;
}
