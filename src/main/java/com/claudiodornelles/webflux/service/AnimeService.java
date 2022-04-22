package com.claudiodornelles.webflux.service;

import com.claudiodornelles.webflux.domain.Anime;
import com.claudiodornelles.webflux.exception.NotFoundException;
import com.claudiodornelles.webflux.exception.ServiceValidationException;
import com.claudiodornelles.webflux.repository.AnimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(UUID id) {
        if (id == null) {
            throw new ServiceValidationException("id should not be null");
        }
        return animeRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("could not find anime with id " + id)));
    }

    @Transactional
    public Mono<Anime> save(Anime anime) {
        return Mono.just(validateBeanAttributes(anime))
                .flatMap(animeRepository::save);
    }

    @Transactional
    public Flux<Anime> saveAll(List<Anime> animes) {
        return animeRepository.saveAll(
                animes.stream()
                        .map(this::validateBeanAttributes)
                        .collect(Collectors.toList())
        );
    }

    public Mono<Void> update(Anime anime) {
        if (anime.getId() == null) {
            throw new ServiceValidationException("id should not be null");
        }
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

    private <T> T validateBeanAttributes(T bean) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Set<ConstraintViolation<T>> violations = validator.validate(bean);
            if (!violations.isEmpty()) {
                throw new ServiceValidationException(violations.iterator().next().getMessage());
            }
        }
        return bean;
    }
}
