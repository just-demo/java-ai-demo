# Spring Boot

Spring Boot is an opinionated framework built on top of the Spring Framework that simplifies
building stand-alone, production-grade applications with minimal configuration.

## Key features

* Auto-configuration based on classpath contents and properties
* Starter dependencies that bundle commonly used libraries
* Embedded servers (Tomcat, Jetty, Undertow) - no external application server needed
* Actuator for production-ready monitoring (health, metrics, info)
* Externalized configuration via application.properties / application.yml

## Dependency injection

Spring Boot relies on the Spring IoC container to manage beans and wire dependencies using
annotations such as @Component, @Service, @Repository, and constructor injection (the
recommended approach over field injection).
