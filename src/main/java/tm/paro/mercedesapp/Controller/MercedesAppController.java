package tm.paro.mercedesapp.Controller;

import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tm.paro.mercedesapp.Model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MercedesAppController {

    private final String clientID = "b1874353-7c46-4c1c-a6ee-b49a59ca31e0";
    @Value("${secret.encoded}")
    private String idSecretEncoded;
    private final String carId = "200EF110887E00D778";        // curl -X GET "https://api.mercedes-benz.com/experimental/connectedvehicle/v1/vehicles" -H "accept: application/json" -H "authorization: Bearer <insert_the_access_token_here>"
    private final String authEndpoint = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize";
    private final String tokenEndpoint = "https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token";
    private final String connectedVehicleEndPoint = "https://api.mercedes-benz.com/experimental/connectedvehicle/v1/";
    private final String vehicleDataEndpoint = "https://api.mercedes-benz.com/vehicledata/v1/vehicles/" + carId + "/resources/doorstatusfrontleft";
    //private final String scopeGeneral="mb:vehicle:status:general";
    //private final String scopeRead="mb:user:pool:reader";
    private final String scope = "mb:vehicle:status:general+mb:user:pool:reader";
    private final String redirectURI = "http://localhost:9090/redirect";
    private final String vehicles = connectedVehicleEndPoint + "vehicles/" + carId;
    private final String doors = connectedVehicleEndPoint + "vehicles/" + carId + "/doors";
    private final String tires = connectedVehicleEndPoint + "vehicles/" + carId + "/tires";
    private final String location = connectedVehicleEndPoint + "vehicles/" + carId + "/location";
    private final String fuel = connectedVehicleEndPoint + "vehicles/" + carId + "/fuel";
    private final String odometer = connectedVehicleEndPoint + "vehicles/" + carId + "/odometer";
    private final String stateOfCharge = connectedVehicleEndPoint + "vehicles/" + carId + "/stateofcharge";
    private AccessTokenResponse tokenResponse;

    private final HttpClient client = HttpClientBuilder.create().build();

    private static final Logger LOGGER = LoggerFactory.getLogger(MercedesAppController.class);
    private final Gson gson = new Gson();

    @GetMapping(value = "/")
    public String startAuthorizationCodeFlow(ModelMap model) {
        //https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/authorize?response_type=code&client_id=<insert_your_client_id_here>&redirect_uri=<insert_redirect_uri_here>&scope=<insert_scopes_of_API_here>&state=<insert_client_state_here>
        String authRequest = authEndpoint.concat("?response_type=code")
                .concat("&client_id=").concat(clientID)
                .concat("&redirect_uri=").concat(redirectURI)
                .concat("&scope=").concat(scope)
                //.concat("&prompt=login,consent")
                .concat("&state=1234");
        LOGGER.info("MercedesAppController: startAuthorizationCodeFlow - Authorization Uri built: {}", authRequest);
        model.addAttribute("authEndpoint", authRequest);
        return "index";
    }

    @GetMapping(value = "/redirect")
    public String callbackHandler(@RequestParam(value = "code", required = true) String code, ModelMap model) throws IOException {
        LOGGER.info("callbackHandler - Authorization code received: {}", code);
        HttpPost post = new HttpPost(tokenEndpoint);

        //curl --request POST \
        //  --url https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token \
        //  --header 'Authorization: Basic <insert_your_base64_encoded_client_id_and_client_secret_here>' \
        //  --header 'content-type: application/x-www-form-urlencoded' \
        //  --data 'grant_type=authorization_code&code=<insert_the_code_obtained_in_step_3_here>&redirect_uri=<insert_redirect_uri_here>'

        post.setHeader("Authorization", "Basic " + idSecretEncoded);
        post.setHeader("content-type", "application/x-www-form-urlencoded");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        urlParameters.add(new BasicNameValuePair("code", code));
        urlParameters.add(new BasicNameValuePair("redirect_uri", redirectURI));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        LOGGER.info("callbackHandler -  POST request send, token Uri: {} \n" +
                        "Header: Authorization Basic (encoded ClientID and ClientSecret): {} \n" +
                        "Header: Content-Type: application/x-www-form-urlencoded \n" +
                        "Parameters: Grand-Type: authorization_code \n" +
                        "Parameters: Authorization code: {} \n" +
                        "Parameters: Redirect Uri: {} \n"
                , tokenEndpoint, idSecretEncoded, code, redirectURI);
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        tokenResponse = gson.fromJson(streamReader, AccessTokenResponse.class);

        LOGGER.info("callbackHandler -  Response received. Token :\n" +
                        "Access token: {} \n" +
                        "Token type: {} \n" +
                        "Expires in: {} \n" +
                        "Refresh token: {} \n" +
                        "Scope: {} \n"
                , tokenResponse.getAccess_token(), tokenResponse.getToken_type(), tokenResponse.getExpires_in(), tokenResponse.getRefresh_token(), tokenResponse.getScope());
        return "redirect:/main";
    }

    public void getRefreshToken() throws IOException {
        String refreshToken = tokenResponse.getRefresh_token();
        LOGGER.info("getRefreshToken - Exchanging refresh token: {}", refreshToken);
        HttpPost post = new HttpPost(tokenEndpoint);

        //curl --request POST \
        //  --url https://api.secure.mercedes-benz.com/oidc10/auth/oauth/v2/token \
        //  --header 'Authorization: Basic <insert_your_base64_encoded_client_id_and_client_secret_here>' \
        //  --header 'content-type: application/x-www-form-urlencoded' \
        //  --data 'grant_type=refresh_token&refresh_token=<insert_your_refreseh_token_received_in_step_4_here>'

        post.setHeader("Authorization", "Basic " + idSecretEncoded);
        post.setHeader("content-type", "application/x-www-form-urlencoded");
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "refresh_token"));
        urlParameters.add(new BasicNameValuePair("refresh_token", refreshToken));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        HttpResponse response = client.execute(post);
        LOGGER.info("getRefreshToken -  POST request send, token Uri: {} \n" +
                        "Header: Authorization Basic (encoded ClientID and ClientSecret): {} \n" +
                        "Header: Content-Type: application/x-www-form-urlencoded \n" +
                        "Parameters: Grand-Type: refresh_token \n" +
                        "Parameters: Refresh Token: {} \n"
                , tokenEndpoint, idSecretEncoded, refreshToken);

        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        tokenResponse = gson.fromJson(streamReader, AccessTokenResponse.class);

        LOGGER.info("getRefreshToken -  Response received. Token :\n" +
                        "Access token: {} \n" +
                        "Token type: {} \n" +
                        "Expires in: {} \n" +
                        "Refresh token: {} \n" +
                        "Scope: {} \n"
                , tokenResponse.getAccess_token(), tokenResponse.getToken_type(), tokenResponse.getExpires_in(), tokenResponse.getRefresh_token(), tokenResponse.getScope());

    }

    @GetMapping("/main")
    public String getMainPage() {
        LOGGER.info("getMainPage -  Main page. Token :\n" +
                        "Access token: {} \n" +
                        "Token type: {} \n" +
                        "Expires in: {} \n" +
                        "Refresh token: {} \n" +
                        "Scope: {} \n"
                , tokenResponse.getAccess_token(), tokenResponse.getToken_type(), tokenResponse.getExpires_in(), tokenResponse.getRefresh_token(), tokenResponse.getScope());
        return "main";
    }

    @GetMapping(value = "/vehicles")
    public String getVehicleStatus(ModelMap model) throws IOException {
        HttpGet get = new HttpGet(vehicles);
        get.setHeader("accept", "application/json");
        get.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());
        HttpResponse response = client.execute(get);
        LOGGER.info("getVehicleStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , vehicles, tokenResponse.getAccess_token());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }

        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        Vehicles vehicleObj = gson.fromJson(streamReader, Vehicles.class);
        LOGGER.info("Vehicle info received: {}", vehicleObj);
        model.addAttribute("vehicles", vehicleObj);
        return "vehicle";
    }

    @GetMapping(value = "/doors")
    public String getDoorStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(doors);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());

        HttpResponse response = client.execute(post);
        LOGGER.info("getDoorStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , doors, tokenResponse.getAccess_token());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }

        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        Doors doorsObj = gson.fromJson(streamReader, Doors.class);
        LOGGER.info("Doors status received: {}", doorsObj);
        model.addAttribute("doorsObj", doorsObj);

        return "doors";
    }

    @PostMapping(value = "/doors")
    public String doorCommand(@ModelAttribute("door") Doors door, ModelMap model) throws IOException {
        //curl -X POST "https://api.mercedes-benz.com/experimental/connectedvehicle/v1/vehicles/<insert_your_vehicle_id_here>/doors" -H "Content-Type: application/json" -H "authorization: Bearer <insert_the_access_token_here>" -d "{ \"command\": \"LOCK\"}"
        String command = door.getCommand().toString();
        LOGGER.info("Command received: {}", command);
        HttpPost post = new HttpPost(doors);

        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());
        post.setHeader("Content-Type", "application/json");
        String json="{ \"command\": \""+command+"\"}";
        StringEntity entity = new StringEntity(json);
        post.setEntity(entity);
        //post.setEntity(new UrlEncodedFormEntity(urlParameters));

        LOGGER.info("doorCommand -  POST request send to: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n" +
                        "Body: Command: {}"
                , doors, tokenResponse.getAccess_token(), command);
        HttpResponse response = client.execute(post);
        displayJSON(response);
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        return "main";
    }

    @GetMapping(value = "/tires")
    public String getTireStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(tires);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());

        HttpResponse response = client.execute(post);
        LOGGER.info("getTireStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , tires, tokenResponse.getAccess_token());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }

        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        Tires tiresObj = gson.fromJson(streamReader, Tires.class);
        LOGGER.info("Tires status received: {}", tiresObj);
        model.addAttribute("tires", tiresObj);
        return "tires";
    }

    @GetMapping(value = "/location")
    public String getLocationStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(location);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());
        HttpResponse response = client.execute(post);
        LOGGER.info("getLocationStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , location, tokenResponse.getAccess_token());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }

        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        Location locationObj = gson.fromJson(streamReader, Location.class);
        LOGGER.info("Location status received: {}", locationObj);
        model.addAttribute("location", locationObj);
        return "location";
    }

    @GetMapping(value = "/odometer")
    public String getOdometerStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(odometer);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());
        HttpResponse response = client.execute(post);
        LOGGER.info("getOdometerStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , odometer, tokenResponse.getAccess_token());

        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        Odometer odometerObj = gson.fromJson(streamReader, Odometer.class);
        LOGGER.info("Odometer status received: {}", odometerObj);
        model.addAttribute("odometer", odometerObj);
        return "odometer";
    }

    @GetMapping(value = "/fuel")
    public String getFuelStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(fuel);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());

        HttpResponse response = client.execute(post);
        LOGGER.info("getFuelStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , fuel, tokenResponse.getAccess_token());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        FuelLevel fuelLevelObj = gson.fromJson(streamReader, FuelLevel.class);
        LOGGER.info("Fuel level status received: {}", fuelLevelObj);
        model.addAttribute("fuellevel", fuelLevelObj);
        return "fuellevel";
    }

    @GetMapping(value = "/stateofcharge")
    public String getStateOfChargeStatus(ModelMap model) throws IOException {
        HttpGet post = new HttpGet(stateOfCharge);
        post.setHeader("accept", "application/json");
        post.setHeader("authorization", "Bearer " + tokenResponse.getAccess_token());
        HttpResponse response = client.execute(post);
        LOGGER.info("getStateOfChargeStatus -  POST request send, token Uri: {} \n" +
                        "Header: accept: application/json \n" +
                        "Header: Authorization Bearer : {} \n"
                , stateOfCharge, tokenResponse.getAccess_token());

        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        StateOfCharge stateOfChargeObj = gson.fromJson(streamReader, StateOfCharge.class);
        LOGGER.info("State of charge status received: {}", stateOfChargeObj);
        model.addAttribute("stateofcharge", stateOfChargeObj);
        return "stateofcharge";
    }

    // Endpoint for getting token via Client Credentials
    @GetMapping(value = "/cc")
    public String startClientCredentialsFlow(ModelMap model) throws IOException {
        HttpPost post = new HttpPost(tokenEndpoint);
        post.setHeader("Content-Type", "application/x-www-form-urlencoded");
        post.setHeader("Authorization", "Basic " + idSecretEncoded);
        List<NameValuePair> urlParameters = new ArrayList<>();
        urlParameters.add(new BasicNameValuePair("grant_type", "client_credentials"));
        urlParameters.add(new BasicNameValuePair("scope", scope));
        post.setEntity(new UrlEncodedFormEntity(urlParameters));
        LOGGER.info("startClientCredentialsFlow -  POST request send, token Uri: {} \n" +
                        "Header: Authorization Basic (encoded ClientID and ClientSecret): {} \n" +
                        "Header: Content-Type: application/x-www-form-urlencoded \n" +
                        "Parameters: Grand-Type: client_credentials \n" +
                        "Parameters: Scope: {}"
                , tokenEndpoint, idSecretEncoded, scope);


        HttpResponse response = client.execute(post);
        LOGGER.info("callbackHandler -  Response received. Token :\n" +
                        "Access token: {} \n" +
                        "Token type: {} \n" +
                        "Expires in: {} \n" +
                        "Scope: {} \n"
                , tokenResponse.getAccess_token(), tokenResponse.getToken_type(), tokenResponse.getExpires_in(), tokenResponse.getScope());
        if (response.getStatusLine().getStatusCode() != 200) {
            return "error/error-" + response.getStatusLine().getStatusCode();
        }
        InputStreamReader streamReader = new InputStreamReader(response.getEntity().getContent());
        tokenResponse = gson.fromJson(streamReader, AccessTokenResponse.class);
        return "redirect:/vehicles";
    }


    //Utility method for displaying Json
    public void displayJSON(HttpResponse response) throws IOException {
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = rd.readLine()) != null) {
            System.out.println(line);
            result.append(line);
        }
    }


}
