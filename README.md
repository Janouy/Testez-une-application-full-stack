# Yoga App

## Table of Contents
1. [Description](#yoga-app)
2. [Technologies](#the-project-was-generated-with)
3. [Install My SQL Data Base](#install-my-sql-data-base)
4. [Install project](#install-project)
5. [Start Java project](#start-java-project)
6. [Start Angular project](#start-angular-project)
7. [Run tests](#run-tests)
   1. [Unit tests](#unit-tests)
   2. [Integration tests](#integration-tests)
   3. [End-to-end tests](#end-to-end-tests)


## Yoga App

Yoga App is designed to organization the yoga sessions.
It allows members to register for yoga classes and manage their participation, while providing instructors to create, schedule, and manage yoga sessions.

## The project was generated with:

[Angular CLI](https://github.com/angular/angular-cli) version 14.1.0

[JAVA](https://www.oracle.com/fr/java/technologies/javase/jdk11-archive-downloads.html) version 11

[NODE JS](https://nodejs.org/fr) version 16

[MySQL](https://www.mysql.com/fr/)


## Install My SQL Data Base:

To set up the database locally, install MySQL, create a data base named 'test' and run it on the default port 3306.
No manual table creation is required — the Java project will automatically generate all necessary tables when it starts for the first time.


## Install project
Clone the repository:

git clone https://github.com/Janouy/Testez-une-application-full-stack.git

## Start Java project

### Compile the project
> mvn clean install

### Run the application
> mvn spring-boot:run

## Start Angular project

### Install dependencies:

> npm install

### Launch Front-end:

> npm run start;

## Run tests

### Unit tests and integration tests in Angular

#### Launching test:

> npm run test

#### Generate coverage report:

> npm run jest:coverage

#### Report is available here:

> front/coverage/jest/lcov-report/index.html

### End-to-end tests 

While project is running

> npm run e2e

Open your favorite browser and run all.cy.ts file

#### Generate coverage report:

After running e2e tests

> npm run e2e:coverage

#### Report is available here:

> front/coverage/lcov-report/index.html

### Unit tests and integration tests in Java

#### For launch and generate the jacoco code coverage:
> mvn clean test

#### Report is available here:

> back/target/site/jacoco/index.html
