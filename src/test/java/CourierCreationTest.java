import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

public class CourierCreationTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "http://qa-scooter.praktikum-services.ru";
    }

    @After
    public void tearDown() {
        deleteCourier();
    }

    @Test
    @DisplayName("Успешное создание курьера")
    @Description("Проверка позитивного сценария создания курьера.")
    public void postCourierWithValidFieldsCreatesCourier() {

        String body = "{\"login\": \"redTractor\", \"password\": \"1234\", \"firstName\": \"Pyotr\"}";

        Response response = sendPostCourier(body);

        assetThatCourierResponseValid(response);

        body = "{ \"login\": \"redTractor\", \"password\": \"1234\" }";

        assertThatCourierIsCreated(body);
    }

    @Test
    @DisplayName("Логин уже используется")
    @Description("Попытка создания курьера с логином, который уже есть в системе.")
    public void postCourierWithAlreadyExistedLoginReturnFault() {

        String body = "{\"login\": \"redTractor\", \"password\": \"1234\", \"firstName\": \"Pyotr\"}";

        sendPostCourier(body);

        Response response = sendPostCourier(body);

        assertThatLoginAlreadyInUse(response);
    }

    @Test
    @DisplayName("Запрос без поля логина")
    @Description("Попытка создания курьера без поля login.")
    public void postCourierWithoutLoginReturnBadRequest() {

        String body = "{\"password\": \"1234\", \"firstName\": \"Pyotr\"}";

        Response response = sendPostCourier(body);

        assertThatProvidedDataIsNotEnough(response);

    }

    @Test
    @DisplayName("Запрос без поля пароль")
    @Description("Попытка создания курьера без поля password.")
    public void postCourierWithoutPasswordReturnBadRequest() {

        String body = "{\"login\": \"redTractor\", \"firstName\": \"Pyotr\"}";

        Response response = sendPostCourier(body);

        assertThatProvidedDataIsNotEnough(response);

    }

    @Test
    @DisplayName("Запрос без поля Имя")
    @Description("Попытка создания курьера без поля firstname.")
    public void postCourierWithoutFirstNameReturnBadRequest() {

        String body = "{\"login\": \"redTractor\", \"password\": \"1234\"}";

        Response response = sendPostCourier(body);

        assertThatProvidedDataIsNotEnough(response);
    }

    @Step("Отправить запрос на создание курьера.")
    private Response sendPostCourier(String body) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
                .post("/api/v1/courier");
    }

    @Step("Проверить ответ.")
    private void assetThatCourierResponseValid(Response response) {
        response.then()
                .assertThat()
                .statusCode(201)
                .and()
                .body("ok", equalTo(true));
    }

    @Step("Проверить, что курьер создался.")
    private void assertThatCourierIsCreated(String body) {
        given()
                .header("Content-type", "application/json")
                .and()
                .body(body)
        .when()
                .post("/api/v1/courier/login")
        .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("id", notNullValue());
    }

    @Step("Проверить, что такой логин уже используется.")
    private void assertThatLoginAlreadyInUse(Response response) {
        response.then()
                .assertThat()
                .statusCode(409)
                .and()
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Step("Проверить, что данных в запросе недостаточно.")
    private void assertThatProvidedDataIsNotEnough(Response response) {
        response.then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @Step("Удалить курьера, если создавался.")
    private void deleteCourier() {

        int id;

        try {
            String body = "{ \"login\": \"redTractor\", \"password\": \"1234\" }";

            id = given()
                    .header("Content-type", "application/json")
                    .and()
                    .body(body)
                 .when()
                    .post("/api/v1/courier/login")
                 .then()
                    .extract().path("id");
        } catch (NullPointerException e) {
            id = 0;
        }

        if (id > 0) {
            given().delete("/api/v1/courier/" + id);
        }
    }
}
