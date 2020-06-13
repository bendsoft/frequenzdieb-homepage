package ch.frequenzdieb.common

import reactor.core.publisher.Mono

fun <T1, T2> Mono<T1>.zipToPairWhen(async: (param: T1) -> Mono<T2>): Mono<Pair<T1, T2>> =
    zipWhen(async) {
        entity, checkResult -> Pair(entity, checkResult)
    }

fun <T1, T2> Mono<T1>.zipToPair(async: (T1) -> Mono<T2>): Mono<Pair<T1, T2>> =
    zipWhen(async) { t1, t2 -> Pair(t1, t2) }

fun <T1, T2, T3> Mono<Pair<T1, T2>>.zipToTriple(async: (Pair<T1, T2>) -> Mono<T3>): Mono<Triple<T1, T2, T3>> =
    zipWhen(async) { (t1, t2), t3 -> Triple(t1, t2, t3) }
