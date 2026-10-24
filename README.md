# 🥗 VitalBite — RecipeBox Application

**VitalBite** is a full-stack, health-centric web application designed to simplify daily nutrition and home recipe organization. Built with **Spring Boot 3**, **Spring Security**, and **Thymeleaf**, VitalBite provides tailored 7-day meal routines based on individual Body Mass Index (BMI) categories, allergen management, and interactive recipe discovery.

---

## ✨ Key Features

- **📊 Instant BMI & Calorie Metrics**
  - Instant BMI calculation supporting both **Metric** (kg/cm) and **Imperial** (lb/ft/in) units.
  - Guest/Anonymous visitors can freely test the BMI calculator without logging in.
  - Registered users can save BMI records and track health metrics over time.

- **📅 Automated 7-Day Meal Generation**
  - Generates personalized, balanced 7-day meal schedules (Breakfast, Lunch, Dinner) matched to specific BMI target categories (*Underweight, Normal, Overweight, Obese*).
  - Users can save, review, and toggle active weekly plans.

- **🔍 Interactive Recipe Exploration**
  - Filter recipes by cooking time, dietary parameters, and meal categories.
  - Detailed recipe views including preparation steps, cooking durations, and calorie guides.

- **❤️ Save & Favorites **
  - Build a personalized cookbook collection by saving favorite recipes for quick access.

- **⚠️ Allergen & Dietary Safety Tracking**
  - Register personal dietary restrictions and allergens to receive smart warnings and custom meal suggestions.

- **🤖 Telegram Assistant Integration**
  - Integrated Telegram Bot  to receive daily meal plans, random recipe picks, and on-the-go BMI calculations.

---

## 🛠️ Technology Stack

- **Backend Framework:** Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security 6
- **Frontend Engine:** Thymeleaf, HTML5, CSS3, JavaScript (ES6+), Bootstrap Icons
- **Database Management:** MySQL 8.0
- **Build Tool:** Apache Maven
- **Template Security:** Thymeleaf Spring Security 6 Dialect
