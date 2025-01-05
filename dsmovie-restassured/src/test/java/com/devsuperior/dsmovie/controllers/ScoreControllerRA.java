package com.devsuperior.dsmovie.controllers;

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

public class ScoreControllerRA {

	private String clientUsername, clientPassword, adminUsername, adminPassword;
	private String clientToken, adminToken;

	private Map<String, Object> putScoreInstance;

	@BeforeEach
	public void setUp() throws Exception {
		baseURI = "http://localhost:8080";

		clientUsername = "alex@gmail.com";
		clientPassword = "123456";

		adminUsername = "maria@gmail.com";
		adminPassword = "123456";

		clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
		adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

		putScoreInstance = new HashMap<>();
		putScoreInstance.put("movieId", 1L);
		putScoreInstance.put("score", 5);
	}

	@Test
	public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {

		putScoreInstance.put("movieId", 100L);
		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newScore)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.put("/scores")
				.then()
				.statusCode(404)
				.body("error", equalTo("Recurso não encontrado"));
	}

	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {

		putScoreInstance.put("movieId", null);
		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newScore)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.put("/scores")
				.then()
				.statusCode(422)
				.body("errors.fieldName[0]", equalTo("movieId"))
				.body("errors.message[0]", equalTo("Campo requerido"));
	}

	@Test
	public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {

		putScoreInstance.put("score", -1);
		JSONObject newScore = new JSONObject(putScoreInstance);

		given()
				.header("Content-type", "application/json")
				.header("Authorization", "Bearer " + clientToken)
				.body(newScore)
				.contentType(ContentType.JSON)
				.accept(ContentType.JSON)
				.when()
				.put("/scores")
				.then()
				.statusCode(422)
				.body("errors.fieldName[0]", equalTo("score"))
				.body("errors.message[0]", equalTo("Valor mínimo 0"));
	}
}
