package com.claudiodornelles.webflux.service;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.exception.NotFoundException;
import com.claudiodornelles.webflux.exception.ServiceValidationException;
import com.claudiodornelles.webflux.repository.AnimeRepository;
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
class AnimeServiceTest {

    @InjectMocks
    private AnimeService service;

    @Mock
    private AnimeRepository repositoryMock;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    static void beforeAll() {
        BlockHound.install();
    }

    @Test
    void blockHoundWorks() {
        try {
            var task = new FutureTask<>(
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

        StepVerifier.create(service.findAll())
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findAll();
    }

    @Test
    void shouldFindById() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.just(anime));

        StepVerifier.create(service.findById(UUID.randomUUID()))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
    }

    @Test
    void shouldFailFindByIdWhenAnimeDoesNotExist() {
        var randomId = UUID.randomUUID();

        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.findById(randomId))
                .expectSubscription()
                .expectErrorMatches(error -> error instanceof NotFoundException
                && error.getMessage().equals("could not find anime with id " + randomId))
                .verify();
    }

    @Test
    void shouldThrowExceptionWhenTryingToFindByANullId() {
        var exception = Assertions.assertThrows(ServiceValidationException.class, () -> service.findById(null));
        Assertions.assertEquals("id should not be null", exception.getMessage());
    }

    @Test
    void shouldSaveAnime() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        Mockito.when(repositoryMock.save(animeToBeSaved))
                .thenReturn(Mono.just(animeToBeSaved));

        StepVerifier.create(service.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(animeToBeSaved)
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .save(animeToBeSaved);
    }

    @Test
    void shouldSaveAllAnime() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        Mockito.when(repositoryMock.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
                .thenReturn(Flux.just(animeToBeSaved, animeToBeSaved));

        StepVerifier.create(service.saveAll(List.of(animeToBeSaved, animeToBeSaved)))
                .expectSubscription()
                .expectNext(animeToBeSaved, animeToBeSaved)
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .saveAll(List.of(animeToBeSaved, animeToBeSaved));
    }

    @Test
    void shouldFailSaveAllWhenAnimeContainsNullOrEmptyName() {
        var animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        var animesToBeSaved = List.of(animeToBeSaved, animeToBeSaved.withName(""));

        var exception = Assertions.assertThrows(ServiceValidationException.class, () -> service.saveAll(animesToBeSaved));

        Assertions.assertEquals("name cannot be empty", exception.getMessage());

        Mockito.verify(repositoryMock, Mockito.never())
                .saveAll(Mockito.anyList());
    }

    @Test
    void shouldDeleteAnime() {
        Mockito.when(repositoryMock.findById(AnimeCreator.ANIME_ID_1))
                .thenReturn(Mono.just(AnimeCreator.createValidAnime()));
        Mockito.when(repositoryMock.delete(Mockito.any(Anime.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.delete(AnimeCreator.ANIME_ID_1))
                .expectSubscription()
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(AnimeCreator.ANIME_ID_1);
        Mockito.verify(repositoryMock, Mockito.times(1))
                .delete(Mockito.any(Anime.class));
    }

    @Test
    void shouldFailDeleteWhenAnimeDoesNotExist() {
        Mockito.when(repositoryMock.findById(AnimeCreator.ANIME_ID_1))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.delete(AnimeCreator.ANIME_ID_1))
                .expectSubscription()
                .expectErrorMatches(error -> error instanceof NotFoundException
                && error.getMessage().equals("could not find anime with id " + AnimeCreator.ANIME_ID_1))
                .verify();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(AnimeCreator.ANIME_ID_1);
        Mockito.verify(repositoryMock, Mockito.never())
                .delete(Mockito.any(Anime.class));
    }

    @Test
    void shouldUpdateAnime() {
        var validUpdatedAnime = AnimeCreator.createValidUpdatedAnime();

        Mockito.when(repositoryMock.findById(AnimeCreator.ANIME_ID_1))
                .thenReturn(Mono.just(validUpdatedAnime));
        Mockito.when(repositoryMock.save(validUpdatedAnime))
                .thenReturn(Mono.just(validUpdatedAnime));

        StepVerifier.create(service.update(validUpdatedAnime))
                .expectSubscription()
                .verifyComplete();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(AnimeCreator.ANIME_ID_1);
        Mockito.verify(repositoryMock, Mockito.times(1))
                .save(validUpdatedAnime);
    }

    @Test
    void shouldFailUpdateWhenAnimeDoesNotExist() {
        Mockito.when(repositoryMock.findById(Mockito.any(UUID.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.update(AnimeCreator.createValidUpdatedAnime()))
                .expectSubscription()
                .expectErrorMatches(error -> error instanceof NotFoundException
                && error.getMessage().equals("could not find anime with id " + AnimeCreator.ANIME_ID_1))
                .verify();

        Mockito.verify(repositoryMock, Mockito.times(1))
                .findById(Mockito.any(UUID.class));
        Mockito.verify(repositoryMock, Mockito.never())
                .save(Mockito.any(Anime.class));
    }
}