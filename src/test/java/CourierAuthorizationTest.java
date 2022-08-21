import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierAuthorizationTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
        createCourier();
    }

    @AfterClass
    public static void tearDown() {
        deleteCourier();
    }

    @Test
    @DisplayName("Успешная авторизация")
    @Description("Проверка возможности входа с правильными логином и паролем.")
    public void loginWithValidCredentialsSucceed() {

        String body = "{ \"login\": \"tractor\", \"password\": \"1234\" }";

        Response response = sendPostCourierLogin(body);

        assertThatCourierAuthorized(response);
    }

    @Test
    @DisplayName("Запрос без поля логин")
    @Description("Попытка авторизоваться без указания логина.")
    public void loginWithoutLoginFieldReturnFault() {

        String body = "{\"password\": \"1234\"}";

        Response response = sendPostCourierLogin(body);

        assertThatProvidedDataIsNotEnough(response);
    }

    @Test
    @DisplayName("Запрос без поля пароль")
    @Description("Попытка авторизоваться без указания пароля.")
    public void loginWithoutPasswordFieldReturnFault() {

        String body = "{\"login\": \"tractor\"}";

        Response response = sendPostCourierLogin(body);

        assertThatProvidedDataIsNotEnough(response);
    }

    @Test
    @DisplayName("Запрос с неверным логином")
    @Description("Попытка авторизации с неверным логином учетной записи.")
    public void loginWithWrongLoginReturnFault() {

        String body = "{ \"login\": \"badTractor\", \"password\": \"1234\" }";

        Response response = sendPostCourierLogin(body);

        assertThatCourierNotFound(response);
    }

    @Test
    @DisplayName("Запрос с неверным паролем")
    @Description("Попытка авторизации с неверным паролем учетной записи.")
    public void loginWithWrongPasswordReturnFault() {

        String body = "{ \"login\": \"tractor\", \"password\": \"1111\" }";

        Response response = sendPostCourierLogin(body);

        assertThatCourierNotFound(response);
    }

    @Step("Создать курьера.")
    private static void createCourier() {
        String body = "{\"login\": \"tractor\", \"password\": \"1234\", \"firstName\": \"Pyotr\"}";

         given()
                 .header("Content-type", "application/json")
                 .and()
                 .body(body)
                 .post("/api/v1/courier");
    }

    @Step("Отправить запрос на авторизацию.")
    private static Response sendPostCourierLogin(String body) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .post("/api/v1/courier/login");
    }

    @Step("Проверить, что курьер авторизовался.")
    private void assertThatCourierAuthorized(Response response) {
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("id", notNullValue());
    }

    @Step("Проверить, что данных недостаточно для входа.")
    private void assertThatProvidedDataIsNotEnough(Response response) {
        response.then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Step("Проверить, что учетная запись не найдена.")
    private void assertThatCourierNotFound(Response response) {
        response.then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Step("Удалить курьера.")
    private static void deleteCourier() {

        String body = "{ \"login\": \"tractor\", \"password\": \"1234\" }";

        Response response = sendPostCourierLogin(body);

        int id = response.then()
                .extract().path("id");

        if (id > 0) {
            given().delete("/api/v1/courier/" + id);
        }
    }
}
