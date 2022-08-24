import io.qameta.allure.Description;
import io.restassured.response.Response;
import model.Order;
import model.OrderClient;
import model.OrderGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.util.List;

import static model.StepProvider.step;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class OrderCreationTest {

    private Integer track;
    private Order order;
    private OrderClient orderClient;
    private List<String> color;

    public OrderCreationTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters(name = "Создание заказа с полем цвет: {0}")
    public static Object[] getData() {
        return new Object[]{List.of("BLACK"),
                List.of("GREY"),
                List.of("BLACK", "GREY"),
                List.of()};
    }

    @Before
    public void setUp() {
        order = OrderGenerator.getOrder();
        orderClient = new OrderClient();
    }

    @After
    public void tearDown() {
        orderClient.cancel(track);
    }

    @Test
    @Description("Проверка возможности создания заказа с разными значениями поля color.")
    public void ordersCreatesOrder() {
        order.setColor(color);

        step("Отправить запрос на создание заказа");
        Response response = orderClient.create(order);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 201 CREATED", SC_CREATED, statusCode);

        step("Проверить наличие track в ответе");
        track = response.then().extract().path("track");
        assertNotNull(track);

        step("Проверить что заказ создался");
        response = orderClient.track(track);
        statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 200", SC_OK, statusCode);
    }
}
