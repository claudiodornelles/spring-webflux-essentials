package com.claudiodornelles.webflux.util;

import com.claudiodornelles.webflux.domain.Anime;

import java.util.UUID;

public class AnimeCreator {

    private static final UUID ANIME_ID_1 = UUID.fromString("cb349efc-7411-45e0-941e-4514adb14811");

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("Tensei Shitara Slime Datta Ken")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(ANIME_ID_1)
                .name("Tensei Shitara Slime Datta Ken")
                .build();
    }

    public static Anime createValidUpdatedAnime() {
        return Anime.builder()
                .id(ANIME_ID_1)
                .name("Tensei Shitara Slime Datta Ken 2")
                .build();
    }
}
