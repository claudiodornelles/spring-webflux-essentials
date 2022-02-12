package com.claudiodornelles.webflux.integration;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.exception.CustomAttributes;
import com.claudiodornelles.webflux.repository.AnimeRepository;
import com.claudiodornelles.webflux.service.AnimeService;
import com.claudiodornelles.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebFluxTest
@Import({AnimeService.class, CustomAttributes.class})
class AnimeControllerIntegrationTest {

    @MockBean
    private AnimeRepository repositoryMock;

    @Autowired
    private WebTestClient testClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    static void beforeAll() {
        BlockHound.install(
                builder -> builder.allowBlockingCallsInside("java.util.UUID", "randomUUID")
        );
    }

    @Test
    void blockHoundWorks() {
        try {
            FutureTask<?> task = new FutureTask<>(
                    () -> {
                        Thread.sleep(0);
                        return "";
                    }
            );
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    void shouldFindAll() {
        Mockito.when(repositoryMock.findAll())
                .thenReturn(Flux.just(anime));

        testClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody()
                .jsonPath("$.[0].id").value(id -> assertEquals(String.valueOf(anime.getId()), id))
                .jsonPath("$.[0].name").value(name -> assertEquals(anime.getName(), name));

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findAll();
    }

    @Test
    void shouldFindAll2() {
        Mockito.when(repositoryMock.findAll())
                .thenReturn(Flux.just(anime));

        testClient.get()
                .uri("/animes")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findAll();
    }

    @Test
    void shouldFindById() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.just(anime));

        testClient.get()
                .uri("/animes/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
    }

    @Test
    void shouldFailFindByIdWhenAnimeDoesNotExist() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        testClient.get()
                .uri("/animes/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus()
                .isNotFound()
                .expectBody()
                .jsonPath("$.status").value(status -> assertEquals(404, status))
                .jsonPath("$.message").value(message -> assertEquals("404 NOT_FOUND \"Anime not found\"", message));

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
    }

    @Test
    void shouldSaveAnime() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        Mockito.when(repositoryMock.save(Mockito.any(Anime.class)))
                .thenReturn(Mono.just(animeToBeSaved));

        testClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class).value(anime -> {
                    assertNotNull(anime.getId());
                    assertEquals(animeToBeSaved.getName(), anime.getName());
                });

        Mockito.verify(repositoryMock, Mockito.times(1))
                .save(Mockito.any(Anime.class));
    }

    @Test
    void shouldFailSaveAnimeWithoutName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeWithEmptyName();

        testClient.post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);

    }

    @Test
    void shouldDeleteAnime() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.just(AnimeCreator.createValidAnime()));
        Mockito.when(repositoryMock.delete(Mockito.any(Anime.class)))
                .thenReturn(Mono.empty());

        testClient.delete()
                .uri("/animes/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
        Mockito.verify(repositoryMock, Mockito.times(1))
                .delete(Mockito.any(Anime.class));
    }

    @Test
    void shouldFailDeleteWhenAnimeDoesNotExist() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        testClient.delete()
                .uri("/animes/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").value(status -> assertEquals(404, status))
                .jsonPath("$.message").value(message -> assertEquals("404 NOT_FOUND \"Anime not found\"", message));

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
        Mockito.verify(repositoryMock, Mockito.never())
                .delete(Mockito.any(Anime.class));
    }

    @Test
    void shouldUpdateAnime() {
        Anime validUpdatedAnime = AnimeCreator.createValidUpdatedAnime();

        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.just(validUpdatedAnime));
        Mockito.when(repositoryMock.save(validUpdatedAnime))
                .thenReturn(Mono.just(validUpdatedAnime));

        testClient.put()
                .uri("/animes/{id}", AnimeCreator.ANIME_ID_1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(validUpdatedAnime))
                .exchange()
                .expectStatus().isNoContent();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
        Mockito.verify(repositoryMock, Mockito.times(1))
                .save(validUpdatedAnime);
    }

    @Test
    void shouldFailUpdateWhenAnimeDoesNotExist() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        testClient.put()
                .uri("/animes/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(anime))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").value(status -> assertEquals(404, status))
                .jsonPath("$.message").value(message -> assertEquals("404 NOT_FOUND \"Anime not found\"", message));

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
        Mockito.verify(repositoryMock, Mockito.never())
                .save(Mockito.any(Anime.class));
    }
}
