import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Courier;
import model.CourierClient;
import model.CourierCredentials;
import model.CourierGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static model.StepProvider.step;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.*;

public class CourierCreationTest {

    private static Courier courier;
    private static CourierClient courierClient;
    private static Integer id;

    @Before
    public void setUp() {
        courier = CourierGenerator.getCourier();
        courierClient = new CourierClient();
    }

    @After
    public void tearDown() {
        if (id != null) {
            courierClient.delete(id);
        }
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Проверка позитивного сценария создания курьера.")
    public void createCourierWithValidFieldsCreatesCourier() {

        step("Отправить запрос на создание курьера");
        Response response = courierClient.create(courier);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 201 CREATED", SC_CREATED, statusCode);

        step("Проверить тело ответа");
        Boolean isOk = response.then().extract().path("ok");
        assertTrue(isOk);

        step("Убедиться, что курьер создался");
        id = courierClient.login(CourierCredentials.from(courier)).then().extract().path("id");
        assertNotNull(id);
    }

    @Test
    @DisplayName("Логин уже используется")
    @Description("Попытка создания курьера с логином, который уже есть в системе.")
    public void createCourierWithAlreadyExistedLoginReturnFault() {

        courierClient.create(courier);
        id = courierClient.login(CourierCredentials.from(courier)).then().extract().path("id");

        step("Отправить запрос на создание курьера");
        Response response = courierClient.create(courier);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 409 CONFLICT", SC_CONFLICT, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Этот логин уже используется", message);
    }

    @Test
    @DisplayName("Запрос без поля логина")
    @Description("Попытка создания курьера без поля login.")
    public void createCourierWithoutLoginReturnBadRequest() {

        courier.setLogin(null);

        step("Отправить запрос на создание курьера");
        Response response = courierClient.create(courier);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        if (statusCode == SC_CREATED) {
            id = courierClient.login(CourierCredentials.from(courier)).then().extract().path("id");
        }
        assertEquals("Status code is not 400 BAD_REQUEST", SC_BAD_REQUEST, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Недостаточно данных для создания учетной записи", message);
    }

    @Test
    @DisplayName("Запрос без поля пароль")
    @Description("Попытка создания курьера без поля password.")
    public void createCourierWithoutPasswordReturnBadRequest() {

        courier.setPassword(null);

        step("Отправить запрос на создание курьера");
        Response response = courierClient.create(courier);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        if (statusCode == SC_CREATED) {
            id = courierClient.login(CourierCredentials.from(courier)).then().extract().path("id");
        }
        assertEquals("Status code is not 400 BAD_REQUEST", SC_BAD_REQUEST, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Недостаточно данных для создания учетной записи", message);
    }

    @Test
    @DisplayName("Запрос без поля Имя")
    @Description("Попытка создания курьера без поля firstname.")
    public void createCourierWithoutFirstNameReturnBadRequest() {

        courier.setFirstName(null);

        step("Отправить запрос на создание курьера");
        Response response = courierClient.create(courier);

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        if (statusCode == SC_CREATED) {
            id = courierClient.login(CourierCredentials.from(courier)).then().extract().path("id");
        }
        assertEquals("Status code is not 400 BAD_REQUEST", SC_BAD_REQUEST, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Недостаточно данных для создания учетной записи", message);
    }
}
