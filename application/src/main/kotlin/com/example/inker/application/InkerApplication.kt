package com.example.inker.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InkerApplication

fun main(args: Array<String>) {
    runApplication<InkerApplication>(*args)
} 