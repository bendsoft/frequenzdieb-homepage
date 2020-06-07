package ch.frequenzdieb.common

import reactor.core.publisher.Mono

fun <T1, T2> Mono<T1>.zipToPairWhen(async: (param: T1) -> Mono<T2>): Mono<Pair<T1, T2>> =
    zipWhen(async) {
        entity, checkResult -> Pair(entity, checkResult)
    }
