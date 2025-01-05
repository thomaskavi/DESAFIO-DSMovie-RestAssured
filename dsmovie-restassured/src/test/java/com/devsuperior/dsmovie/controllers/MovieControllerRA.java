package com.devsuperior.dsmovie.controllers;

import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.devsuperior.dsmovie.tests.TokenUtil;

import io.restassured.http.ContentType;

import static io.restassured.RestAssured.*;
import static io.restassured.matcher.RestAssuredMatchers.*;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;

public class MovieControllerRA {

	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken, invalidToken;
	private Long existingMovieId, nonExistingMovieId;
	private String movieName;
	private Map<String, Object> postMovieInstance;

	@BeforeEach
	public void setUp() throws Exception {
		baseURI = "http://localhost:8080";

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";

		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		movieName = "vingadores";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
		invalidToken = adminToken + "xpto"; // INVALID TOKEN

		postMovieInstance = new HashMap<>();
		postMovieInstance.put("title", "Test Movie");
		postMovieInstance.put("score", 0.0);
		postMovieInstance.put("count", 0);
		postMovieInstance.put("image",
				"https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg");
	}

	@Test
	public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {

		given()
				.get("/movies?page=0")
				.then()
				.statusCode(200)
				.body("content.id", hasItems(1, 2, 7))
				.body("content.title", hasItems("The Witcher", "Venom: Tempo de Carnificina", "Titanic"));
	}

	@Test
	public void findAllShouldReturnPagedMoviesWhenMovieTitleParamIsNotEmpty() {

		given()
				.get("/movies?title={movieName}", movieName)
				.then()
				.statusCode(200)
				.body("content.id[0]", is(13))
				.body("content.title[0]", equalTo("Vingadores: Ultimato"));
	}

	@Test
	public void findByIdShouldReturnMovieWhenIdExists() {
		existingMovieId = 1L;

		given()
				.get("/movies/{id}", existingMovieId)
				.then()
				.statusCode(200)
				.body("id", is(1))
				.body("title", equalTo("The Witcher"))
				.body("image",
						equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
	}

	@Test
	public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {
		nonExistingMovieId = 100L;

		given()
				.get("/movies/{id}", nonExistingMovieId)
				.then()
				.statusCode(404)
				.body("error",
						equalTo("Recurso não encontrado"));
	}

	@Test
	public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {

		postMovieInstance.put("title", "   ");
		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + adminToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(422)
				.body("errors.fieldName", hasItem("title"))
				.body("errors.message", hasItems("Tamanho deve ser entre 5 e 80 caracteres", "Campo requerido"));
	}

	@Test
	public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {

		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(403);
	}

	@Test
	public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {

		JSONObject newMovie = new JSONObject(postMovieInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + invalidToken)
				.body(newMovie)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.post("/movies")
				.then()
				.statusCode(401);
	}
}
