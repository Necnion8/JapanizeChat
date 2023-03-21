package com.github.ucchyocean.lc3.japanize.provider;

import io.github.apple502j.kanaify.Kanaifier;

import java.util.concurrent.CompletableFuture;

public interface Provider {
    CompletableFuture<String> fetch(Kanaifier kanaifier, String message);
    default String parse(String value) {
        return value;
    }
    default boolean isUsable() {
        return true;
    }
    default String getName() {
        return this.getClass().getName();
    }
}
