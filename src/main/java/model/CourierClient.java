package model;

import io.restassured.response.Response;

import static io.restassured.RestAssured.*;

public class CourierClient extends RestClient {

    private final String LOGIN_PATH = "/api/v1/courier/login",
                         COURIER_PATH = "/api/v1/courier";

    public Response login(CourierCredentials credentials) {
        return given()
                .spec(getBaseSpec())
                .body(credentials)
                .post(LOGIN_PATH);
    }

    public Response create(Courier courier) {
        return given()
                .spec(getBaseSpec())
                .body(courier)
                .post(COURIER_PATH);
    }

    public void delete(Integer id) {
        given()
                .spec(getBaseSpec())
                .delete(COURIER_PATH + "/" + id);
    }

    public void accept(Integer id, Integer track) {
        given()
                .spec(getBaseSpec())
                .queryParam("courierId", id)
                .put(COURIER_PATH + "/" + track);
    }


}
