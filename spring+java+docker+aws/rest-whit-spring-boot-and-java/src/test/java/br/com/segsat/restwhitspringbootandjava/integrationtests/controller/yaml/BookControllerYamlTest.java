package br.com.segsat.restwhitspringbootandjava.integrationtests.controller.yaml;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import br.com.segsat.restwhitspringbootandjava.configs.TestConfig;
import br.com.segsat.restwhitspringbootandjava.integrationtests.controller.yaml.mapper.YamlMapper;
import br.com.segsat.restwhitspringbootandjava.integrationtests.testcontainers.AbstractIntegrationTest;
import br.com.segsat.restwhitspringbootandjava.integrationtests.vo.pagedModels.PagedModelBook;
import br.com.segsat.restwhitspringbootandjava.integrationtests.vo.v1.AccountCredentialsVO;
import br.com.segsat.restwhitspringbootandjava.integrationtests.vo.v1.BookVO;
import br.com.segsat.restwhitspringbootandjava.integrationtests.vo.v1.TokenVO;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(OrderAnnotation.class)
public class BookControllerYamlTest extends AbstractIntegrationTest {

    private static RequestSpecification specification;

    private static YamlMapper objectMapper;

    private static BookVO book;

    @BeforeAll
    public static void setup() {
        objectMapper = new YamlMapper();
        book = new BookVO();
    }
    
    @Test
    @Order(1)
    public void authorization() {
        AccountCredentialsVO user = new AccountCredentialsVO();
        user.setUsername("leandro");
        user.setPassword("admin123");

        var token =
                given()
                    .config(
                        RestAssuredConfig
                            .config()
                            .encoderConfig(EncoderConfig.encoderConfig()
                                    .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
                    .basePath("/auth/signin")
                    .port(TestConfig.SERVER_PORT)
                    .contentType(TestConfig.CONTENT_TYPE_YML)
    				.accept(TestConfig.CONTENT_TYPE_YML)
                    .body(user, objectMapper)
                    .when()
                        .post()
                    .then()
                        .statusCode(200)
                    .extract()
                    .body()
                        .as(TokenVO.class, objectMapper)
                    .getAccessToken();

            specification =
                new RequestSpecBuilder()
                    .addHeader(TestConfig.HEADER_PARAM_AUTHORIZATION, "Bearer " + token)
                    .addHeader(TestConfig.HEADER_PARAM_ORIGIN, TestConfig.ORIGIN_FROTA)
                    .setBasePath("/api/book/v1")
                    .setPort(TestConfig.SERVER_PORT)
                    .addFilter(new RequestLoggingFilter(LogDetail.ALL))
                    .addFilter(new ResponseLoggingFilter(LogDetail.ALL))
                    .build();
    }
      
    @Test
    @Order(2)
    public void testCreate() throws JsonMappingException, JsonProcessingException {
        
        mockBook();

        book = given()
                    .config(
                        RestAssuredConfig
                            .config()
                            .encoderConfig(EncoderConfig.encoderConfig()
                                    .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
                    .spec(specification)
                .contentType(TestConfig.CONTENT_TYPE_YML)
				.accept(TestConfig.CONTENT_TYPE_YML)
                    .body(book, objectMapper)
                    .when()
                    .post()
                .then()
                    .statusCode(200)
                        .extract()
                        .body()
                            .as(BookVO.class, objectMapper);
        
        assertNotNull(book.getId());
        assertNotNull(book.getTitle());
        assertNotNull(book.getAuthor());
        assertNotNull(book.getPrice());
        assertTrue(book.getId() > 0);
        assertEquals("Docker Deep Dive", book.getTitle());
        assertEquals("Nigel Poulton", book.getAuthor());
        assertEquals(55.99, book.getPrice());
    }
      
    @Test
    @Order(3)
    public void testUpdate() throws JsonMappingException, JsonProcessingException {
        
        book.setTitle("Docker Deep Dive - Updated");

        BookVO bookUpdated = given()
                    .config(
                        RestAssuredConfig
                            .config()
                            .encoderConfig(EncoderConfig.encoderConfig()
                                    .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
                    .spec(specification)
                .contentType(TestConfig.CONTENT_TYPE_YML)
				.accept(TestConfig.CONTENT_TYPE_YML)
                .pathParam("id", book.getId())
                    .body(book, objectMapper)
                    .when()
                    .put("{id}")
                .then()
                    .statusCode(200)
                        .extract()
                        .body()
                        .as(BookVO.class, objectMapper);
        
        assertNotNull(bookUpdated.getId());
        assertNotNull(bookUpdated.getTitle());
        assertNotNull(bookUpdated.getAuthor());
        assertNotNull(bookUpdated.getPrice());
        assertEquals(bookUpdated.getId(), book.getId());
        assertEquals("Docker Deep Dive - Updated", bookUpdated.getTitle());
        assertEquals("Nigel Poulton", bookUpdated.getAuthor());
        assertEquals(55.99, bookUpdated.getPrice());
    }

    @Test
    @Order(4)
    public void testFindById() throws JsonMappingException, JsonProcessingException {
        var foundBook = given()
                    .config(
                        RestAssuredConfig
                            .config()
                            .encoderConfig(EncoderConfig.encoderConfig()
                                    .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
                    .spec(specification)
                .contentType(TestConfig.CONTENT_TYPE_YML)
				.accept(TestConfig.CONTENT_TYPE_YML)
                    .pathParam("id", book.getId())
                    .when()
                    .get("{id}")
                .then()
                    .statusCode(200)
                        .extract()
                        .body()
                        .as(BookVO.class, objectMapper);
        
        assertNotNull(foundBook.getId());
        assertNotNull(foundBook.getTitle());
        assertNotNull(foundBook.getAuthor());
        assertNotNull(foundBook.getPrice());
        assertEquals(foundBook.getId(), book.getId());
        assertEquals("Docker Deep Dive - Updated", foundBook.getTitle());
        assertEquals("Nigel Poulton", foundBook.getAuthor());
        assertEquals(55.99, foundBook.getPrice());
    }
    
    @Test
    @Order(5)
    public void testDelete() {
        given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
            .spec(specification)
                .contentType(TestConfig.CONTENT_TYPE_YML)
				.accept(TestConfig.CONTENT_TYPE_YML)
                    .pathParam("id", book.getId())
                    .when()
                    .delete("{id}")
                .then()
                    .statusCode(204);
    }
    
    @Test
    @Order(6)
    public void testFindAll() throws JsonMappingException, JsonProcessingException {

        var wrapper = given()
                    .config(
                        RestAssuredConfig
                            .config()
                            .encoderConfig(EncoderConfig.encoderConfig()
                                    .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
                    .spec(specification)
                .contentType(TestConfig.CONTENT_TYPE_YML)
				.accept(TestConfig.CONTENT_TYPE_YML)
                .queryParams("page", 0, "size", 10, "direction", "asc")
				.when()
					.get("/getAll")
                .then()
                    .statusCode(200)
                        .extract()
                        .body()
                        .as(PagedModelBook.class, objectMapper); 
                        // .as(BookVO[].class, objectMapper); 

        BookVO foundBookOne = wrapper.getContent().get(0);
        
        assertNotNull(foundBookOne.getId());
        assertNotNull(foundBookOne.getTitle());
        assertNotNull(foundBookOne.getAuthor());
        assertNotNull(foundBookOne.getPrice());
        assertTrue(foundBookOne.getId() > 0);
        assertEquals("Working effectively with legacy code", foundBookOne.getTitle());
        assertEquals("Michael C. Feathers", foundBookOne.getAuthor());
        assertEquals(49.00, foundBookOne.getPrice());
        
        BookVO foundBookFive = wrapper.getContent().get(4);
        
        assertNotNull(foundBookFive.getId());
        assertNotNull(foundBookFive.getTitle());
        assertNotNull(foundBookFive.getAuthor());
        assertNotNull(foundBookFive.getPrice());
        assertTrue(foundBookFive.getId() > 0);
        assertEquals("Code complete", foundBookFive.getTitle());
        assertEquals("Steve McConnell", foundBookFive.getAuthor());
        assertEquals(58.0, foundBookFive.getPrice());
    }

    @Test
    @Order(7)
    public void testHateoas() throws JsonMappingException, JsonProcessingException {

        var content = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfig.CONTENT_TYPE_YML, ContentType.TEXT)))
            .spec(specification)
            .contentType(TestConfig.CONTENT_TYPE_YML)
            .accept(TestConfig.CONTENT_TYPE_YML)
            .queryParams("page", 1, "size", 5, "direction", "asc")
            .when()
                .get("/getAll")
            .then()
                .statusCode(200)
                    .extract()
                    .body()
                .asString();
        
        assertTrue(content.contains("- rel: \"self\"\n    href: \"http://localhost:8888/api/book/v1/10\""));
        assertTrue(content.contains("- rel: \"self\"\n    href: \"http://localhost:8888/api/book/v1/6\""));

        assertTrue(content.contains("- rel: \"first\"\n  href: \"http://localhost:8888/api/book/v1/getAll?direction=asc&page=0&size=5&sort=id,asc\""));
        assertTrue(content.contains("- rel: \"prev\"\n  href: \"http://localhost:8888/api/book/v1/getAll?direction=asc&page=0&size=5&sort=id,asc\""));
        assertTrue(content.contains("- rel: \"self\"\n  href: \"http://localhost:8888/api/book/v1/getAll?page=1&size=5&direction=asc\""));
        assertTrue(content.contains("- rel: \"next\"\n  href: \"http://localhost:8888/api/book/v1/getAll?direction=asc&page=2&size=5&sort=id,asc\""));
        assertTrue(content.contains("- rel: \"last\"\n  href: \"http://localhost:8888/api/book/v1/getAll?direction=asc&page=2&size=5&sort=id,asc\""));

        assertTrue(content.contains("page:\n  size: 5\n  totalElements: 15\n  totalPages: 3\n  number: 1"));

    }
     
    private void mockBook() {
        book.setTitle("Docker Deep Dive");
        book.setAuthor("Nigel Poulton");
        book.setPrice(Double.valueOf(55.99));
        book.setLaunchDate(new Date());
    }
}