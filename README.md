# 🍽️ MySmartRecipeBook

**MySmartRecipeBook** is a web application developed for the **Large Scale and Multi-Structured Databases** course project.

The platform allows users to discover, save and manage recipes through a multi-database architecture based on **MongoDB**, **Neo4j** and **Redis**.  
The application is designed around three main actors: **Foodies**, **Chefs** and **Administrators**.

Foodies can browse recipes, save their favourite ones, manage their **Smart Fridge** and **Shopping List**, and receive recipe suggestions based on the ingredients they currently have.  
Chefs can create and manage recipes, while administrators are responsible for approving chefs and recipes and accessing analytical functionalities.

---

## 🚀 Main Features

- 🔐 **Authentication and Authorization**  
  User registration and login are managed through JWT-based authentication.

- 👤 **Foodie Area**  
  Foodies can manage their profile, browse recipes, save favourite recipes and use Smart Fridge functionalities.

- 👨‍🍳 **Chef Area**  
  Chefs can create new recipes, manage their submitted recipes and view their approved recipes.

- 🛠️ **Admin Area**  
  Administrators can approve or discard pending chefs and recipes.

- 📖 **Recipe Catalog**  
  Users can browse recipes by category, chef or title.

- ❤️ **Favourite Recipes**  
  Foodies can save and remove recipes from their favourites.

- 🧊 **Smart Fridge**  
  Users can store the ingredients they currently have and receive recipe suggestions.

- 🛒 **Shopping List**  
  Users can manage ingredients they need to buy.

- 📊 **Analytics**  
  Administrators can access statistics such as monthly registrations, category trends and chef rankings.

- 🗄️ **Multi-Database Architecture**  
  The application combines MongoDB, Neo4j and Redis, each one used for a specific purpose.

---
## 🧱 Technologies

MongoDB: Stores structured document data (users, chefs, foodies, recipes, pending approvals).
Neo4j: Manages graph data involving chefs, recipes and ingredients.
Redis: Stores fast-access data such as Smart Fridge ingredients, Shopping Lists and cached recipe suggestions.
Python: Custom scripts for scraping, cleaning and dataset generation.
Java (Spring Boot): RESTful backend exposing services for the application.

## 📊 Dataset Overview

Large-scale recipe dataset processed and adapted for the application.
Collected through web scraping from recipe websites.
Enhanced with generated users and chefs to simulate a realistic application environment.
Covers: Recipes, Ingredients, Chefs, Foodies.

## 🚀 Setup Instructions

Clone the repository.

Set up MongoDB, Neo4j and Redis instances:

Ensure MongoDB is running locally or in your environment.

Ensure Neo4j is running and accessible for graph-based recommendation services.

Ensure Redis is running and accessible for Smart Fridge, Shopping List and cache management.

Run backend services (/java-backend).

(Optional) Launch dataset generation scripts (/data-scripts).

## 📄 Documentation

Full instructions and API documentation are available in the repository.

## ✨ Credits

Developed by

Megan Maremmani
Eleonora Sgorbini
Chiara Masiero
