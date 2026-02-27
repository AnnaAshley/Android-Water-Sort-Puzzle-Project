# Android Water Sort Puzzle

An Android implementation of the Water Sort Puzzle Game built using Java and Android Studio.
This project demonstrates object-oriented design, custom game logic implementation, and interactive mobile UI development.

The application allows users to interactively sort colored liquids between test tubes while enforcing puzzle rules and tracking game progress.

---

## Features
- Interactive touch-based gameplay
- Automatic puzzle solver capable of computing valid solutions
- Custom puzzle logic implemented in Java
- Rule-based move validation and win condition detection
- Multiple test tubes with layered color representation
- Reset functionality to restart puzzles
- Android UI built using XML layouts and Android lifecycle components

---

## Technical Highlights

**Languages & Frameworks**
- Java (core game logic)
- Android SDK
- XML (UI layouts)
- Gradle Kotlin DSL (`build.gradle.kts`)

**Software Engineering Concepts Demonstated**
- Object-oriented programming (encapsulation of game state and tube logic)
- State management and rule validation
- Event-driben programming
- Seperation of UI and game logic
- Mobile application architecture fundamentals

**Development tools**
- Android Studio
- Gradle build system
- Git and Github for version control

---

## How It Works

The game maintains an internal representation of test tubes and their color layers. When a user selects two tubes, the system:

1. Validates whether the move follows puzzle rules
2. Transfers valid color layers between tubes
3. Updates the UI to reflect the new state
4. Checks whether the win condition has been reached

This logic is implemented using Java classes that manage tube state, game rules, and user interaction.

**Puzzle Solver**

The application includes an automatic solving feature that computes valid move sequences to complete the puzzle.

The Solver:
- Represents the puzzle as a structured game state
- Evaluates valid moves according to puzzle rules
- Iteratively searches for a sequence that leads to a solved state
- Executes the solution within the app interface

This demonstrates algorithm design, state modeling, and programmatic problem solving in Java.

---

## Running the Project

1. Clone the repository
2. Open the project in Android Studio
3. Allow Gradle to sync dependencies
4. Run on an Android emulator or physical device

## Purpose

This project was developed as part of an Android and ubiquitous computing course and serves as a demonstration of Android development fundamentals, Java-based application logic, and mobile UI implementation.

It also reflects my broader interest in backend and application development using Java.

## Author

Anna Ashley
Software Developer
https://github.com/AnnaAshley
http://www.linkedin.com/in/anna-ashley
