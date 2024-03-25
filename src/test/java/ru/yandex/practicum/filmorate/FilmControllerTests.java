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
public class FilmControllerTests {

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

    private HttpRequest requestGETFilms() {
        return HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:8080/films"))
                .header("Accept", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private HttpRequest requestPOSTFilms(String json) {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        return HttpRequest.newBuilder()
                .POST(body)
                .uri(URI.create("http://localhost:8080/films"))
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    private HttpRequest requestPUTFilms(String json) {
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        return HttpRequest.newBuilder()
                .PUT(body)
                .uri(URI.create("http://localhost:8080/films"))
                .header("Accept", "*/*")
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
    }

    @Test
    void shouldReturnEmptyFilmListTest() throws IOException, InterruptedException {
        HttpResponse<String> response = httpClient.send(requestGETFilms(), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnOneFilmListTest() throws IOException, InterruptedException {
        shouldCreateFilmTest();
        HttpResponse<String> response = httpClient.send(requestGETFilms(), responseHandler);
        assertEquals(200, response.statusCode());
        String jsonExpected = "[{" +
                "\"id\":1," +
                "\"name\":\"nisi eiusmod\"," +
                "\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\"," +
                "\"duration\":100," +
                "\"likes\":[]" +
                "}]";
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldGet400CodeWithNoPOSTRequestBody() throws IOException, InterruptedException {
        String jsonSent = "";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }

    @Test
    void shouldGet400CodeWithNoPUTRequestBody() throws IOException, InterruptedException {
        String jsonSent = "";
        HttpResponse<String> response = httpClient.send(requestPUTFilms(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }

    @Test
    void shouldCreateFilmTest() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";
        String jsonExpected = "{" +
                "\"id\":1," +
                "\"name\":\"nisi eiusmod\"," +
                "\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\"," +
                "\"duration\":100," +
                "\"likes\":[]" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldUpdateFilmTest() throws IOException, InterruptedException {
        shouldCreateFilmTest();
        String jsonSent = "{" +
                "\"id\":1," +
                "\"name\":\"nisi eiusmod\"," +
                "\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\"," +
                "\"duration\":200" +
                "}";
        String jsonExpected = "{" +
                "\"id\":1," +
                "\"name\":\"nisi eiusmod\"," +
                "\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\"," +
                "\"duration\":200," +
                "\"likes\":[]" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPUTFilms(jsonSent), responseHandler);
        assertEquals(200, response.statusCode());
        assertEquals(jsonExpected, response.body());
    }

    @Test
    void shouldGet500WhenUpdatingInvalidIdFilmTest() throws IOException, InterruptedException {
        shouldCreateFilmTest();
        String jsonSent = "{" +
                "\"id\":2," +
                "\"name\":\"nisi eiusmod\"," +
                "\"description\":\"adipisicing\"," +
                "\"releaseDate\":\"1967-03-25\"," +
                "\"duration\":200" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPUTFilms(jsonSent), responseHandler);
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenNameIsEmpty() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenDescriptionMoreThen200() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff" +
                "fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffficing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 300\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenReleaseDateEarlierThen28121895() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1807-03-25\",\n" +
                "  \"duration\": 100\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldGet400CodeWhenFilmDurationIsNegative() throws IOException, InterruptedException {
        String jsonSent = "{\n" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1947-03-25\",\n" +
                "  \"duration\": -100\n" +
                "}";
        HttpResponse<String> response = httpClient.send(requestPOSTFilms(jsonSent), responseHandler);
        assertEquals(500, response.statusCode());
    }
}
