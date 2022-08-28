package model;

import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;

public class OrderClient extends RestClient {

    private final String ORDERS_PATH = "/api/v1/orders",
                         TRACK_PATH = "/api/v1/orders/track";

    public Response create(Order order) {
        return given()
                .spec(getBaseSpec())
                .body(order)
                .post(ORDERS_PATH);
    }

    public Response track(Integer track) {
        return given()
                .spec(getBaseSpec())
                .queryParam("t", track)
                .get(TRACK_PATH);
    }

    public void cancel(Integer track) {
        Map<String, Integer> body = new HashMap<>();
        body.put("track", track);

        given()
                .spec(getBaseSpec())
                .body(body)
                .put(ORDERS_PATH);
    }

    public Response getOrders() {
        return given()
                .spec(getBaseSpec())
                .get(ORDERS_PATH);
    }

    public Response getOrdersByCourierId(Integer id) {
        return given()
                .spec(getBaseSpec())
                .queryParam("courierId", id)
                .get(ORDERS_PATH);
    }

    public Response getOrdersByNearestStation(Map<String, List<String>> nearestStation) {
        return given()
                .spec(getBaseSpec())
                .queryParams(nearestStation)
                .get(ORDERS_PATH);
    }

    public Response getOrdersWithLimit(Integer limit) {
        return given()
                .spec(getBaseSpec())
                .queryParam("limit", limit)
                .get(ORDERS_PATH);
    }

    public Response getOrdersByPage(int page) {
        return given()
                .spec(getBaseSpec())
                .queryParam("page", page)
                .get(ORDERS_PATH);
    }
}
