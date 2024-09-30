# PointPoker

## Overview

This project employs the Model-View-Intent (MVI) architectural pattern alongside Jetpack Compose to build a streamlined and reactive Android application. This README guides you through the setup process, offers insights into MVI and Jetpack Compose, directs you to the relevant repository.

## Feature
1. create room and join to realtime PointPoker

## Installation

To install the necessary dependencies for this project, follow these steps:

1. Android Studio Setup (https://developer.android.com/studio.)
2. Clone the Repository
3. Open in Android Studio
4. Build and Run

## Design Pattern Documentation
wanna be clean architecture

### MVI
This project implements the Model-View-Intent (MVI) architectural pattern, which enforces a unidirectional data flow and clear separation of concerns. MVI comprises:

Model: Represents the application's state and data.
View: Renders the UI based on the Model's state.
Intent: Captures user interactions and triggers state updates.

### Jetpack Compose
Jetpack Compose is used for building the UI declaratively, enabling efficient UI development and updates.
resources:https://developer.android.com/jetpack/compose

### Repository Pattern
The Repository Pattern is a design pattern that provides an abstraction layer between your application's business logic and the underlying data access layer. It acts as a mediator, allowing your application to interact with data sources (databases, APIs, etc.) in a consistent and decoupled manner.

## Design
The UI adopts a minimalist approach, adapting to the features available within the app. The primary focus is on providing an optimal user experience (UX). Future iterations may involve further enhancements and refinements based on this initial design.
[App Poke Point Figma](https://www.figma.com/design/jUamBKnUnMNZ98bQLH0ieR/App-poke-point)

## Contributing
We welcome contributions to enhance this project! Please fork the repository and submit a pull request for any improvements or features you would like to add.

## TODO Next ...
can find tag // TODO in code
### up to playstore (Instane App  already)
### create Global data
### Add DI Lib
### optimize lifccycle