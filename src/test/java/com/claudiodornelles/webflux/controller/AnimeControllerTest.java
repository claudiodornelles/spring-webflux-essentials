package com.claudiodornelles.webflux.controller;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.service.AnimeService;
import com.claudiodornelles.webflux.util.AnimeCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
class AnimeControllerTest {

    @InjectMocks
    private AnimeController controller;

    @Mock
    private AnimeService serviceMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    static void beforeAll() {
        BlockHound.install();
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
        Mockito.when(serviceMock.findAll())
                .thenReturn(Flux.just(anime));

        StepVerifier.create(controller.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .findAll();
    }

    @Test
    void shouldFindById() {
        Mockito.when(serviceMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.just(anime));

        StepVerifier.create(controller.findById(UUID.randomUUID()))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
    }

    @Test
    void shouldSaveAnime() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        Mockito.when(serviceMock.save(animeToBeSaved))
                .thenReturn(Mono.just(animeToBeSaved));

        StepVerifier.create(controller.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(animeToBeSaved)
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .save(animeToBeSaved);
    }

    @Test
    void shouldSaveBatchAnime() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        Mockito.when(serviceMock.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
                .thenReturn(Flux.just(animeToBeSaved, animeToBeSaved));

        StepVerifier.create(controller.saveBatch(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(animeToBeSaved, animeToBeSaved)
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .saveAll(List.of(animeToBeSaved, animeToBeSaved));
    }

    @Test
    void shouldDeleteAnime() {
        Mockito.when(serviceMock.delete(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(AnimeCreator.ANIME_ID_1))
                .expectSubscription()
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .delete(Mockito.any(UUID.class));
    }

    @Test
    void shouldUpdateAnime() {
        Anime validUpdatedAnime = AnimeCreator.createValidUpdatedAnime();

        Mockito.when(serviceMock.update(validUpdatedAnime))
                .thenReturn(Mono.empty());

        StepVerifier.create(controller.update(AnimeCreator.ANIME_ID_1, validUpdatedAnime))
                .expectSubscription()
                .verifyComplete();

        Mockito.verify(serviceMock, Mockito.times(1))
                .update(validUpdatedAnime);
    }

}