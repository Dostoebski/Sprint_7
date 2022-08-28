package model;

import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class RestClient {

    private final String BASE_URL = "http://qa-scooter.praktikum-services.ru";

    public RequestSpecification getBaseSpec() {
        return given()
                .baseUri(BASE_URL)
                .header("Content-type", "application/json");
    }
}
