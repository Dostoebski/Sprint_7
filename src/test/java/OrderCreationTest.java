import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.notNullValue;

@RunWith(Parameterized.class)
public class OrderCreationTest {

    private static int track;
    private final List<String> color;

    public OrderCreationTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Создание заказа с полем цвет: {0}")
    public static Object[] getData() {
        return new Object[]{List.of("\"BLACK\""),
                List.of("\"GREY\""),
                List.of("\"BLACK\"", "\"GREY\""),
                List.of()};
    }

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @After
    public void tearDown() {
        cancelOrder(track);
    }

    @Test
    @Description("Проверка возможности создания заказа с разными значениями поля color.")
    public void ordersCreatesOrder() {
        String body = "{\n" +
                "    \"firstName\": \"Naruto\",\n" +
                "    \"lastName\": \"Uzumaki\",\n" +
                "    \"address\": \"Konoha, 142 apt.\",\n" +
                "    \"metroStation\": 4,\n" +
                "    \"phone\": \"+7 800 355 35 35\",\n" +
                "    \"rentTime\": 5,\n" +
                "    \"deliveryDate\": \"2022-08-22\",\n" +
                "    \"comment\": \"Saske, come back to Konoha\",\n" +
                "    \"color\":" + color +
                "\n}";

        Response response = sendPostOrders(body);

        track = assertThatOrderCreated(response);

        assertThatOrderExist(track);
    }

    @Step("Отправить запрос на создание заказа.")
    private Response sendPostOrders(String body) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .post("/api/v1/orders");
    }

    @Step("Проверить, что заказ создался.")
    private int assertThatOrderCreated(Response response) {
        return response.then()
                .assertThat()
                .statusCode(201)
                .and()
                .extract().path("track");
    }

    @Step("Проверить, что заказ находится по track.")
    private void assertThatOrderExist(int track) {

        given()
                .queryParam("t", track)
        .when()
                .get("/api/v1/orders/track")
        .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("order", notNullValue());
    }

    @Step("Отменить заказ.")
    private static void cancelOrder(int track) {

        String body = "{ \"track\":" + track + "}";

        given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .put("/api/v1/orders");
    }
}
