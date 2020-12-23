package com.xaaris.webfluxcontextdemo

import org.reactivestreams.Subscription
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import reactor.core.CoreSubscriber
import reactor.core.publisher.Flux
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators
import reactor.util.context.Context
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class TestService {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    fun doSomething(): Flux<String> {
        return Flux.just(0, 1, 2)
            .map { it.toString() }
            .doOnNext { log.info("test") }
            .contextWrite { it.put("session-id", "session xyz") }
    }
}

@Configuration
class MdcContextLifterConfiguration {

    companion object {
        val MDC_CONTEXT_REACTOR_KEY: String = "blubb"
    }

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
    }

    @PreDestroy
    fun cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY)
    }

}

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 */
class MdcContextLifter<T>(private val coreSubscriber: CoreSubscriber<T>) : CoreSubscriber<T> {

    override fun onNext(t: T) {
        coreSubscriber.currentContext().copyToMdc()
        coreSubscriber.onNext(t)
    }

    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun onError(throwable: Throwable?) {
        coreSubscriber.onError(throwable)
    }

    override fun currentContext(): Context {
        return coreSubscriber.currentContext()
    }
}

/**
 * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
 * State of the MDC after calling this method should be same as Reactor [Context] state.
 * One thread-local access only.
 */
private fun Context.copyToMdc() {
    if (!this.isEmpty) {
        val map: Map<String, String> = this.stream()
            .collect(Collectors.toMap({ e -> e.key.toString() }, { e -> e.value.toString() }))

        MDC.setContextMap(map)
    } else {
        MDC.clear()
    }
}