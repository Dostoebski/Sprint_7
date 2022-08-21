import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class OrderListTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @Test
    @DisplayName("Получение списка всех заказов")
    @Description("Проверка успешного получения списка всех заказов на запрос без параметров")
    public void getOrdersWithoutParametersReturnOrders() {
        Response response = getOrders();
        assertThatOrdersListReturned(response);
    }

    @Test
    @DisplayName("Получение списка заказов курьера")
    @Description("Проверка успешного получения списка по courierId.")
    public void getOrdersByValidCourierIdReturnOrders() {
        String id = createCourier().then().extract().path("id");
        String track = createOrder().then().extract().path("track");
        acceptOrder(id, track);

        Response response = getOrdersByCourierId(id);
        assertThatOrdersListByCourierIdReturned(response);

        cancelOrder(track);
        deleteCourier(id);
    }

    @Test
    @DisplayName("Получение списка заказов несуществующего курьера")
    @Description("Попытка получить список заказов по несуществующему courierId.")
    public void getOrdersByInvalidCourierIdReturnFault() {
        String id = "0";
        Response response = getOrdersByCourierId(id);
        assertThatOrdersByInvalidCourierIdNotFound(response);
    }

    @Test
    @DisplayName("Получение списка по станциям метро")
    @Description("Проверка успешного получения списка заказов с параметром nearestStation.")
    public void getOrdersByNearestStationReturnFilteredOrders() {
        Map<String, List<String>> nearestStation = new HashMap<>();
        nearestStation.put("nearestStation", List.of("[\"110\"]"));

        Response response = getOrdersByNearestStation(nearestStation);
        assertThatOrdersListByNearestStationReturned(response);
    }

    @Test
    @DisplayName("Получение списка с заданным количеством")
    @Description("Проверка успешного получения списка заказов с параметром limit.")
    public void getOrdersWithValidLimitReturnLimitedQuantityOfOrders() {
        int limit = 10;
        Response response = getOrdersWithLimit(limit);
        assertThatOrdersListWithLimitReturned(response, limit);
    }

    @Test
    @DisplayName("Получение списка с количеством больше максимума")
    @Description("Попытка получения списка заказов с параметром limit > 30.")
    public void getOrdersByInvalidLimitReturnFault() {
        int limit = 35;
        Response response = getOrdersWithLimit(limit);
        assertThatOrdersWithInvalidLimitReturnedListWithOnlyMaxQuantity(response);
    }

    @Test
    @DisplayName("Получение списка конкретной страницы")
    @Description("Проверка получения списка заказов с параметром page.")
    public void getOrdersByPageReturnExactPageOfOrders() {
        int page = 30;
        Response response = getOrdersByPage(page);
        assertThatOrdersByPageReturnedList(response, page);
    }

    @Step("Отправить запрос на получение списка заказов.")
    private Response getOrders() {
        return given().get("/api/v1/orders");
    }

    @Step("Отправить запрос на получение списка заказов курьера.")
    private Response getOrdersByCourierId(String courierId) {
        return given()
                .queryParam("courierId", courierId)
                .get("/api/v1/orders");
    }

    @Step("Отправить запрос на получение списка с фильтром по Метро.")
    private Response getOrdersByNearestStation(Map<String, List<String>> nearestStation) {
        return given()
                .queryParams(nearestStation)
                .get("/api/v1/orders");
    }

    @Step("Отправить запрос на получение списка заказов c ограничением по количеству.")
    private Response getOrdersWithLimit(int limit) {
        return given()
                .queryParam("limit", limit)
                .get("/api/v1/orders");
    }

    @Step("Отправить запрос на получение конкретной страницы заказов")
    private Response getOrdersByPage(int page) {
        return given()
                .queryParam("page", page)
                .get("/api/v1/orders");
    }

    @Step("Проверить, что вернулся списк всех заказов.")
    private void assertThatOrdersListReturned(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", notNullValue())
                .and()
                .body("pageInfo.page", equalTo(0))
                .and()
                .body("pageInfo.limit", equalTo(30))
                .and()
                .body("availableStations", notNullValue());
    }

    @Step("Проверить, что вернулся список с заказами курьера.")
    private void assertThatOrdersListByCourierIdReturned(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", notNullValue())
                .and()
                .body("pageInfo.page", equalTo(0))
                .and()
                .body("pageInfo.limit", equalTo(30))
                .and()
                .body("availableStations", notNullValue());
    }

    @Step("Проверить, что вернулась ошибка \"Курьер с идентификатором 0 не найден\".")
    private void assertThatOrdersByInvalidCourierIdNotFound(Response response) {
        response.then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Курьер с идентификатором 0 не найден"));
    }

    @Step("Проверить, что вернулся отфильтрованный по станции метро список.")
    private void assertThatOrdersListByNearestStationReturned(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", notNullValue())
                .and()
                .body("orders[0].metroStation", equalTo("110"))
                .and()
                .body("pageInfo.page", equalTo(0))
                .and()
                .body("pageInfo.limit", equalTo(30))
                .and()
                .body("availableStations.size()", equalTo(1))
                .and()
                .body("availableStations[0].number", equalTo("110"));
    }

    @Step("Проверить, что вернулся ограниченный список.")
    private void assertThatOrdersListWithLimitReturned(Response response, int limit) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", notNullValue())
                .and()
                .body("pageInfo.page", equalTo(0))
                .and()
                .body("pageInfo.limit", equalTo(limit))
                .and()
                .body("availableStations", notNullValue());
    }

    @Step("Проверить, что вернулся список с только 30 заказами.")
    private void assertThatOrdersWithInvalidLimitReturnedListWithOnlyMaxQuantity(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders.size()", equalTo(30))
                .and()
                .body("pageInfo.page", equalTo(0))
                .and()
                .body("pageInfo.limit", equalTo(30))
                .and()
                .body("availableStations", notNullValue());
    }

    @Step("Проверить, что вернулся ограниченный список.")
    private void assertThatOrdersByPageReturnedList(Response response, int page) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", notNullValue())
                .and()
                .body("pageInfo.page", equalTo(page))
                .and()
                .body("pageInfo.limit", equalTo(30))
                .and()
                .body("availableStations", notNullValue());
    }

    @Step("Создать курьера.")
    private Response createCourier() {
        String body = "{\"login\": \"fastTractor\", \"password\": \"1234\", \"firstName\": \"Pyotr\"}";

        return given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .post("/api/v1/courier");
    }

    @Step("Создать заказ.")
    private Response createOrder() {
        String body = "{\n" +
                "    \"firstName\": \"Naruto\",\n" +
                "    \"lastName\": \"Uzumaki\",\n" +
                "    \"address\": \"Konoha, 143 apt.\",\n" +
                "    \"metroStation\": 4,\n" +
                "    \"phone\": \"+7 800 355 35 35\",\n" +
                "    \"rentTime\": 5,\n" +
                "    \"deliveryDate\": \"2022-08-22\",\n" +
                "    \"comment\": \"Saske, come back to Konoha\",\n" +
                "    \"color\": [\n" +
                "        \"BLACK\"\n" +
                "    ]\n" +
                "}";

        return given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .post("/api/v1/courier");
    }

    @Step("Принять заказ.")
    private void acceptOrder(String id, String track) {
        given()
                .queryParam("courierId", id)
                .put("/api/v1/courier/" + track);
    }

    @Step("Отменить заказ.")
    private void cancelOrder(String track) {
        String body = "{ \"track\":" + track + "}";

        given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .put("/api/v1/orders");
    }

    @Step("Удалить курьера.")
    private static void deleteCourier(String id) {
        given().delete("/api/v1/courier/" + id);
    }

}
