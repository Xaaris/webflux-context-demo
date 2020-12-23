package com.xaaris.webfluxcontextdemo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class TestService {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun doSomething(): Flux<Int> {
        return Flux.just(0, 1, 2)
            .doOnNext { log.info("Value: $it") }
            .contextWrite { it.put("session-id", "xyz") }
    }
}


