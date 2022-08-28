import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import model.Courier;
import model.CourierClient;
import model.CourierCredentials;
import model.CourierGenerator;
import org.junit.*;

import static model.StepProvider.step;
import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CourierAuthorizationTest {

    private static Courier courier;
    private static CourierClient courierClient;
    private static Integer id;

    @BeforeClass
    public static void setUp() {
        courier = CourierGenerator.getCourier();
        courierClient = new CourierClient();
        courierClient.create(courier);
    }

    @AfterClass
    public static void tearDown() {
        courierClient.delete(id);
    }

    @After
    public void resetCourier() {
        courier = CourierGenerator.getCourier();
    }

    @Test
    @DisplayName("Успешная авторизация")
    @Description("Проверка возможности входа с правильными логином и паролем.")
    public void loginWithValidCredentialsSucceed() {

        step("Отправить запрос на логин");
        Response response = courierClient.login(CourierCredentials.from(courier));

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not OK", SC_OK, statusCode);

        step("Проверить наличие id в ответе");
        id = response.then().extract().path("id");
        assertNotNull(id);
    }

    @Test
    @DisplayName("Запрос без поля логин")
    @Description("Попытка авторизоваться без указания логина.")
    public void loginWithoutLoginFieldReturnFault() {

        courier.setLogin(null);

        step("Отправить запрос на логин");
        Response response = courierClient.login(CourierCredentials.from(courier));

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not BAD_REQUEST", SC_BAD_REQUEST, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Недостаточно данных для входа", message);
    }

    @Test
    @DisplayName("Запрос без поля пароль")
    @Description("Попытка авторизоваться без указания пароля.")
    public void loginWithoutPasswordFieldReturnFault() {

        courier.setPassword(null);

        step("Отправить запрос на логин");
        Response response = courierClient.login(CourierCredentials.from(courier));

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 400 BAD_REQUEST", SC_BAD_REQUEST, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Недостаточно данных для входа", message);
    }

    @Test
    @DisplayName("Запрос с неверным логином")
    @Description("Попытка авторизации с неверным логином учетной записи.")
    public void loginWithWrongLoginReturnFault() {

        courier.setLogin("badTractor");

        step("Отправить запрос на логин");
        Response response = courierClient.login(CourierCredentials.from(courier));

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 404 NOT_FOUND", SC_NOT_FOUND, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Учетная запись не найдена", message);
    }

    @Test
    @DisplayName("Запрос с неверным паролем")
    @Description("Попытка авторизации с неверным паролем учетной записи.")
    public void loginWithWrongPasswordReturnFault() {

        courier.setPassword("1111");

        step("Отправить запрос на логин");
        Response response = courierClient.login(CourierCredentials.from(courier));

        step("Проверить статус ответа");
        int statusCode = response.then().extract().statusCode();
        assertEquals("Status code is not 404 NOT_FOUND", SC_NOT_FOUND, statusCode);

        step("Проверить текст ошибки в ответе");
        String message = response.then().extract().path("message");
        assertEquals("Message doesn't match", "Учетная запись не найдена", message);
    }
}
