package ch.frequenzdieb.common

import reactor.core.publisher.Mono

fun <T1, T2> Mono<T1>.zipToPairWhen(async: (T1) -> Mono<T2>): Mono<Pair<T1, T2>> =
    zipWhen(async) {
        entity, checkResult -> Pair(entity, checkResult)
    }

fun <T1, T2, T3> Mono<Pair<T1, T2>>.zipToTripleWhen(async: (Pair<T1, T2>) -> Mono<T3>): Mono<Triple<T1, T2, T3>> =
    zipWhen(async) { (t1, t2), t3 -> Triple(t1, t2, t3) }
