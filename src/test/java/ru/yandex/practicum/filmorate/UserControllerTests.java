package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserControllerTests {
    private static ConfigurableApplicationContext context;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final HttpResponse.BodyHandler<String> responseHandler = HttpResponse.BodyHandlers.ofString();

    @BeforeEach
    void setupContext() {
        context = SpringApplication.run(FilmorateApplication.class);
    }

    @AfterEach
    void exitContext() {
        context.close();
    }

    private HttpRequest requestGETUsers() {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/users"))
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private HttpRequest requestPOSTUsers(String json) {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        return HttpRequest.newBuilder()
                .POST(body)
                .uri(URI.create("http://localhost:8080/users"))
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private HttpRequest requestPUTUsers(String json) {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        return HttpRequest.newBuilder()
                .PUT(body)
                .uri(URI.create("http://localhost:8080/users"))
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Test
    void shouldReturnEmptyUserListTest() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(requestGETUsers(), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnOneUserListTest() throws IOException, InterruptedException {
        shouldCreateUserTest();
        HttpResponse<String> response = httpClient.send(requestGETUsers(), responseHandler);
        assertEquals(200, response.statusCode());
        String jsonExpected = "[{" +
                "\"id\":1," +
                "\"email\":\"mail@mail.ru\"," +
                "\"login\":\"dolore\"," +
                "\"name\":\"Nick Name\"," +
                "\"birthday\":\"1946-08-20\"" +
                "}]";
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldGet400CodeWithNoPOSTRequestBody() throws IOException, InterruptedException {
        String jsonSent = "";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldGet400CodeWithNoPUTRequestBody() throws IOException, InterruptedException {
        String jsonSent = "";
        HttpResponse<String> response = httpClient.send(requestPUTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldCreateUserTest() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        String jsonExpected = "{" +
                "\"id\":1," +
                "\"email\":\"mail@mail.ru\"," +
                "\"login\":\"dolore\"," +
                "\"name\":\"Nick Name\"," +
                "\"birthday\":\"1946-08-20\"" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldUpdateUserTest() throws IOException, InterruptedException {
        shouldCreateUserTest();
        String jsonSent = "{\n" +
                "\"id\":1," +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mailYAROSLAV@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        String jsonExpected = "{" +
                "\"id\":1," +
                "\"email\":\"mailYAROSLAV@mail.ru\"," +
                "\"login\":\"dolore\"," +
                "\"name\":\"Nick Name\"," +
                "\"birthday\":\"1946-08-20\"" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPUTUsers(jsonSent), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldGet500WhenUpdatingWrongIdUserTest() throws IOException, InterruptedException {
        shouldCreateUserTest();
        String jsonSent = "{\n" +
                "\"id\":3," +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mailYAROSLAV@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPUTUsers(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }

    @Test
    void shouldGet400CodeWithNoEmailInUserCreationPOST() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldGet400CodeWithNoLoginInUserCreationPOST() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void nameShouldBeFilledWithLoginWhenNotSpecified() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        String jsonExpected = "{" +
                "\"id\":1," +
                "\"email\":\"mail@mail.ru\"," +
                "\"login\":\"dolore\"," +
                "\"name\":\"dolore\"," +
                "\"birthday\":\"1946-08-20\"" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldGet400CodeWhenBirthdayDateIsInFuture() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"2946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenEmailHasBlanks() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \"mail@m ail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenEmailIsInInvalidFormat() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"email\": \".ru@mail\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTUsers(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }
}
