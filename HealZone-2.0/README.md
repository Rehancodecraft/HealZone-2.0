# HealZone-2.0
![Project Banner](https://user-images.githubusercontent.com/99194647/219858377-08c7b248-7dfd-4b73-8e7a-39a0d4c4e6a7.png) <!-- Example banner, can be replaced -->

**A JavaFX-based healthcare management system designed to streamline interactions between patients and doctors.**

---

> **Last Updated:** July 11, 2025  
> **Author:** Rehan Shafiq ([Rehancodecraft](https://github.com/Rehancodecraft))  
> **Contact:** rehan.codecraft@gmail.com

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Usage Guide](#usage-guide)
- [Project Structure](#project-structure)
- [Contributing](#contributing)
- [Contact](#contact)
- [Acknowledgements](#acknowledgements)

---

## Overview

HealZone-2.0 is a modern healthcare management system built with JavaFX, focused on improving the interaction between patients and doctors. It provides comprehensive appointment scheduling, secure authentication with OTP via email, prescription management, and user profile management for both patients and doctors.

---

## Features

### 🟢 User Authentication
- Secure login for patients and doctors
- One-Time Password (OTP) verification via email

### 🟢 Appointment Scheduling
- Patients can schedule appointments with doctors
- Doctors can view today's appointments, full month calendar, upcoming, and historical appointments

### 🟢 Prescription Management
- Doctors can prescribe medicines to patients through the system
- Patients can view their prescriptions

### 🟢 User Profile Management
- Update personal information for both patients and doctors
- Profile picture and contact details

### 🟢 Appointment Views
- Doctors and patients can view:
  - Today's appointments
  - Upcoming appointments
  - Full month schedule
  - Appointment history

---

## Tech Stack

- **Programming Language:** Java (80.6%)
- **UI Framework:** JavaFX
- **Styles:** CSS (19.4%)
- **Build Tool:** Maven (`pom.xml`)
- **IDE Recommended:** IntelliJ IDEA

---

## Installation

### Prerequisites

- **Java JDK** (version 11 or above recommended)
- **IntelliJ IDEA** (or any Java IDE supporting JavaFX)
- **Maven** (for dependency management)

### Steps

1. **Clone the Repository**
   ```sh
   git clone https://github.com/Rehancodecraft/HealZone-2.0.git
   ```

2. **Open in IntelliJ IDEA**
  - File → Open → Select the cloned `HealZone-2.0` directory.

3. **Configure Database**
  - Create a database named `HealZone` in your MySQL or preferred RDBMS.
  - Set the database password as specified in the `Databaseconfig` file in the repository, or use your own password.

4. **Setup Libraries**
  - All required libraries are available in the `library` directory within the project.
  - Ensure these JARs are included in your project/module dependencies.

5. **Install Dependencies**
   ```sh
   mvn clean install
   ```

6. **Run the Application**
  - Use your IDE's run configuration for JavaFX, or run via Maven:
    ```sh
    mvn javafx:run
    ```

---

## Usage Guide

- **Patients**
  - Register/login using email (OTP authentication)
  - Schedule appointments, view upcoming/history
  - Access prescriptions prescribed by doctors
  - Update profile information

- **Doctors**
  - Login securely
  - View today's appointments, full month, upcoming, and history
  - Prescribe medicines to patients
  - Update own profile

---

## Project Structure

```
HealZone-2.0/
├── src/
│   ├── main/
│   │   ├── java/           # Java source files
│   │   ├── resources/      # FXML, CSS, images
│   ├── test/               # Test cases
├── library/                # Third-party JARs
├── pom.xml                 # Maven build configuration
├── Databaseconfig          # Database config file (credentials)
```

---

## Contributing

We welcome contributions!

- Fork the repository
- Create a new branch (`feature/your-feature-name`)
- Commit your changes and push to your branch
- Open a Pull Request describing your changes

> **Note:** Please ensure your code is well-documented and adheres to project conventions.

---

## Contact

- **Author:** Rehan Shafiq
- **GitHub:** [Rehancodecraft](https://github.com/Rehancodecraft)
- **Email:** rehan.codecraft@gmail.com

---

## Acknowledgements

- Thanks to all contributors and users of HealZone-2.0!
- Inspired by the need for robust, accessible digital healthcare solutions.

---

*This repository does not currently have a specific license. For questions, suggestions, or collaboration, feel free to reach out.*
