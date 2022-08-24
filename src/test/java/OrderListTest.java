import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.*;
import org.junit.Before;
import org.junit.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.StepProvider.step;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.*;

public class OrderListTest {

    private Order order;
    private static Courier courier;
    private OrderClient orderClient;
    private static CourierClient courierClient;

    @Before
    public void setUp() {
        order = OrderGenerator.getOrder();
        orderClient = new OrderClient();
        courier = CourierGenerator.getCourier();
        courierClient = new CourierClient();
    }

    @Test
    @DisplayName("Получение списка всех заказов")
    @Description("Проверка успешного получения списка всех заказов на запрос без параметров")
    public void getOrdersWithoutParametersReturnOrders() {

        step("Отправка запроса на получение списка");
        Response response = orderClient.getOrders();

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        List<Object> orders = response.then().extract().path("orders");
        assertNotNull(orders);

        step("Проверить номер страницы в ответе");
        int page = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", 0, page);

        step("Проверить ограничение количества заказов в ответе");
        int limit = response.then().extract().path("pageInfo.limit");
        assertEquals("Default limit must be 30", 30, limit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);
    }

    @Test
    @DisplayName("Получение списка заказов курьера")
    @Description("Проверка успешного получения списка по courierId.")
    public void getOrdersByValidCourierIdReturnOrders() {
        Integer id = courierClient.create(courier).then().extract().path("id");
        Integer track = orderClient.create(order).then().extract().path("track");
        courierClient.accept(id, track);

        step("Отправка запроса на получение заказов курьера");
        Response response = orderClient.getOrdersByCourierId(id);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        List<Object> orders = response.then().extract().path("orders");
        assertNotNull(orders);

        step("Проверить номер страницы в ответе");
        int page = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", 0, page);

        step("Проверить ограничение количества заказов в ответе");
        int limit = response.then().extract().path("pageInfo.limit");
        assertEquals("Default limit must be 30", 30, limit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);

        orderClient.cancel(track);
        courierClient.delete(id);
    }

    @Test
    @DisplayName("Получение списка заказов несуществующего курьера")
    @Description("Попытка получить список заказов по несуществующему courierId.")
    public void getOrdersByInvalidCourierIdReturnFault() {

        Integer id = 0;

        step("Отправка запроса на получение заказов курьера");
        Response response = orderClient.getOrdersByCourierId(id);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 404 NOT_FOUND", SC_NOT_FOUND, statusCode);
    }

    @Test
    @DisplayName("Получение списка по станциям метро")
    @Description("Проверка успешного получения списка заказов с параметром nearestStation.")
    public void getOrdersByNearestStationReturnFilteredOrders() {
        Map<String, List<String>> nearestStation = getNearestStationMap();

        step("Отправка запроса на получение списка");
        Response response = orderClient.getOrdersByNearestStation(nearestStation);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        List<Object> orders = response.then().extract().path("orders");
        assertNotNull(orders);

        step("Проверить номер станции у заказа");
        String metroStation = response.then().extract().path("orders[0].metroStation");
        assertEquals("Metro Station doesn't match", "110", metroStation);

        step("Проверить номер страницы в ответе");
        int page = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", 0, page);

        step("Проверить ограничение количества заказов в ответе");
        int limit = response.then().extract().path("pageInfo.limit");
        assertEquals("Default limit must be 30", 30, limit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);
        assertEquals("Must be 1 station",1, availableStations.size());

        step("Проверить номер станции у доступных станций");
        String availableStation = response.then().extract().path("availableStations[0].number");
        assertEquals("Available Station doesn't match", "110", availableStation);
    }

    @Test
    @DisplayName("Получение списка с заданным количеством")
    @Description("Проверка успешного получения списка заказов с параметром limit.")
    public void getOrdersWithValidLimitReturnLimitedQuantityOfOrders() {
        int limit = 10;

        step("Отправка запроса на получение списка");
        Response response = orderClient.getOrdersWithLimit(limit);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        List<Object> orders = response.then().extract().path("orders");
        assertNotNull(orders);

        step("Проверить номер страницы в ответе");
        int page = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", 0, page);

        step("Проверить ограничение количества заказов в ответе");
        int actualLimit = response.then().extract().path("pageInfo.limit");
        assertEquals("Limit doesn't match", limit, actualLimit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);
    }

    @Test
    @DisplayName("Получение списка с количеством больше максимума")
    @Description("Попытка получения списка заказов с параметром limit > 30.")
    public void getOrdersByInvalidLimitReturnFault() {
        int limit = 35;

        step("Отправка запроса на получение списка");
        Response response = orderClient.getOrdersWithLimit(limit);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        int orders = response.then().extract().path("orders.size()");
        assertEquals("Orders quantity must be 30 max", 30, orders);

        step("Проверить номер страницы в ответе");
        int page = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", 0, page);

        step("Проверить ограничение количества заказов в ответе");
        int actualLimit = response.then().extract().path("pageInfo.limit");
        assertEquals("Max limit must be 30", 30, actualLimit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);
    }

    @Test
    @DisplayName("Получение списка конкретной страницы")
    @Description("Проверка получения списка заказов с параметром page.")
    public void getOrdersByPageReturnExactPageOfOrders() {
        int page = 30;
        step("Отправка запроса на получение списка");
        Response response = orderClient.getOrdersByPage(page);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие заказов в ответе");
        List<Object> orders = response.then().extract().path("orders");
        assertNotNull(orders);

        step("Проверить номер страницы в ответе");
        int actualPage = response.then().extract().path("pageInfo.page");
        assertEquals("Default page must be 0", page, actualPage);

        step("Проверить ограничение количества заказов в ответе");
        int limit = response.then().extract().path("pageInfo.limit");
        assertEquals("Default limit must be 30", 30, limit);

        step("Проверить наличие доступных станций метро в ответе");
        List<Object> availableStations = response.then().extract().path("availableStations");
        assertNotNull(availableStations);
    }

    private Map<String, List<String>> getNearestStationMap() {
        Map<String, List<String>> nearestStation = new HashMap<>();
        nearestStation.put("nearestStation", List.of("[\"110\"]"));

        return nearestStation;
    }

}
