package com.xaaris.webfluxcontextdemo

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
internal class TestServiceTest(@Autowired val testService: TestService) {


    @Test
    fun doSomething_test() {
        testService.doSomething().blockLast()
    }
}