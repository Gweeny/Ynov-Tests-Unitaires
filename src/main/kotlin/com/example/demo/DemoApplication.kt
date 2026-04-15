package com.example.demo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.example.demo", "livres"])
class DemoApplication

fun main(args: Array<String>) {
	runApplication<DemoApplication>(*args)
}