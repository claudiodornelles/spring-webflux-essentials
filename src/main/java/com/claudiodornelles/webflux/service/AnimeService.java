package com.claudiodornelles.webflux.service;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.mapper.ResponseMapper;
import com.claudiodornelles.webflux.repository.AnimeRepository;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
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

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return animeRepository.saveAll(animes)
                .doOnNext(this::throwResponseStatusExceptionWhenEmptyName);
    }

    private void throwResponseStatusExceptionWhenEmptyName(Anime anime) {
        if (StringUtil.isNullOrEmpty(anime.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Name");
        }

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
