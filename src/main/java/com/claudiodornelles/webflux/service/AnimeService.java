package com.claudiodornelles.webflux.service;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.mapper.ResponseMapper;
import com.claudiodornelles.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private static final String RESOURCE = "Anime";
    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(UUID id) {
        return animeRepository.findById(id)
                .switchIfEmpty(ResponseMapper.statusNotFound(RESOURCE));
    }

    public Mono<Anime> save(Anime anime) {
        return animeRepository.save(anime);
    }

    public Mono<Void> update(Anime anime) {
        return findById(anime.getId())
                .map(entityFound -> anime)
                .flatMap(animeRepository::save)
                .then();
    }

    public Mono<Void> delete(UUID id) {
        return findById(id)
                .flatMap(animeRepository::delete)
                .then();
    }
}
