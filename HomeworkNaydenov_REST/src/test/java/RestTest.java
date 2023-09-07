
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;

public class RestTest{

    String BASE_URI = "http://localhost:8080";
    String API_ENDPOINT = "/api/food";
    String POST_REQUEST_BODY = "{\"name\": \"Батат\", \"type\": \"VEGETABLE\", \"exotic\": true}";
    String SQL_REQUEST_CHECK_ADD = "SELECT * FROM FOOD";
    String sessionId;
    JdbcTemplate jdbcTemplate = new DBconfig().getConfigureDatabase();

    @BeforeEach
    public void setUp() {
        Response response = given().baseUri(BASE_URI)
                .when()
                .get("/api/food");

        sessionId = response.getCookie("JSESSIONID");
    }
    @Test
    @DisplayName("Отправляем POST заппрос на добавление товара, после через GET проверяем, что он добавлен")
    public void test1() {
        Specifications.installSpec(Specifications.requestSpecification(BASE_URI),
                Specifications.responseSpecification(200));

       given()
                .header("Cookie", "JSESSIONID=" + sessionId)
                .body(POST_REQUEST_BODY)
                .when()
                .post(API_ENDPOINT)
                .then()
                .log().all();


       given()
                .header("Cookie", "JSESSIONID=" + sessionId)
                .when()
                .get(API_ENDPOINT)
                .then()
                .log().all()
                .assertThat()
                .body("name", hasItem("Батат"));
    }

    @Test
    @DisplayName("Проверяем, что добавленый товар появился в БД")
    public void displayFoodTable() {
        String selectQuery = SQL_REQUEST_CHECK_ADD;
        jdbcTemplate.query(selectQuery, (rs) -> {
            while (rs.next()) {
                int foodId = rs.getInt("FOOD_ID");
                String foodName = rs.getString("FOOD_NAME");
                String foodType = rs.getString("FOOD_TYPE");
                int foodExotic = rs.getInt("FOOD_EXOTIC");
                System.out.println("ID: " + foodId +
                        " NAME: " + foodName +
                        " TYPE: " + foodType +
                        " isEXOTIC: " + foodExotic);
            }
        });


    }
}
