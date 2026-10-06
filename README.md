# Data Validator Engine

[![Java Version](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A high-performance, reflection-free, and type-safe data validation engine for modern Java applications. Designed for massive datasets and low-latency systems.

---

## 🚀 Why This Project?

Most Java validation libraries (like Hibernate Validator) rely heavily on Reflection and Annotations. While convenient, they introduce performance overhead and are difficult to use with dynamic data like Maps or large Streams.

**Data Validator Engine** changes the game by using **Functional Programming (Lambdas)**:
- **Zero Reflection:** Validation is as fast as native code.
- **Type-Safe:** Catch errors at compile-time, not runtime.
- **Big Data Ready:** Supports lazy validation via Java Streams (process millions of records with minimal RAM).
- **Extensible:** Plug-in your own rules, context, and error handlers.

---

## ✨ Key Features

- ✅ **No Annotations:** Use Lambda expressions (`User::getName`) for maximum speed.
- ✅ **Primitive Specialization:** Specialized validators for `int`, `long`, and `double` to avoid Autoboxing.
- ✅ **Stream Support:** True lazy loading for massive datasets.
- ✅ **Dynamic Data:** Out-of-the-box support for `Map<String, Object>` (perfect for CSV/Excel/JSON imports).
- ✅ **Context-Aware:** Pass database connections or global configs into your validation rules.
- ✅ **Customizable:** Decoupled architecture (Rules, Fields, Handlers).

---

## 📦 Installation

Add this to your project's `pom.xml`:

```xml
<dependency>
    <groupId>io.github.tanphat1095</groupId>
    <artifactId>data-validator</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

## 🛠 Usage

### 1. Basic POJO Validation

First, implement the `Validatable` interface in your class:

```java
public class User implements Validatable {
    private String name;
    private int age;
    private List<ValidationError> errors;

    // Getters, Setters, etc.
    @Override public void setValidationResult(List<ValidationError> res) { this.errors = res; }
    @Override public List<ValidationError> getValidationResult() { return this.errors; }
}
```

Then, configure and run the validator:

```java
// 1. Define Rules
Validator<User> nameValidator = new ObjectValidator<>("name", 
    List.of(CommonRules.required(), CommonRules.maxStringLength(20)), 
    User::getName);

Validator<User> ageValidator = new IntValidator<>("age", 
    List.of((val, ctx, field) -> val >= 18 ? null : ValidationError.fail(field, "Underage")), 
    User::getAge);

// 2. Initialize Engine
DataValidator<User> engine = new DataValidator<>(List.of(nameValidator, ageValidator), null);

// 3. Execute
User user = new User("John", 15);
engine.validate(user);

if (!user.getValidationResult().isEmpty()) {
    System.out.println("Errors: " + user.getValidationResult());
}
```

### 2. Big Data Validation (Stream)

Process 10M+ records without memory issues:

```java
Stream<User> userStream = database.streamAllUsers();

// Validation happens lazily as the stream is consumed!
Stream<User> validatedStream = engine.validate(userStream);

validatedStream.forEach(user -> {
    if (user.getValidationResult().isEmpty()) {
        saveToExport(user);
    }
});
```

### 3. Dynamic Map Validation (CSV/Excel style)

```java
Map<String, Object> row = new HashMap<>();
row.put("username", "phatlt");

Validator<Map<String, Object>> mapVal = new ObjectValidator<>("username", 
    List.of(CommonRules.required()), 
    m -> (String) m.get("username"));

DataValidator<Map<String, Object>> engine = new DataValidator<>(List.of(mapVal), null);
engine.validate(row);
```

### 4. Internationalization (I18n) & Custom Messages

You can customize error messages and support multiple languages using `MessageProvider`:

```java
// 1. Create a custom provider (e.g., using ResourceBundle)
MessageProvider i18nProvider = (template, args) -> {
    // Logic to fetch message from messages_vn.properties or messages_en.properties
    return MyI18nTool.get(template, args); 
};

// 2. Attach to Context
ValidationContext<String, Object> context = new DefaultValidationContext();
context.setMessageProvider(i18nProvider);

// 3. Rules will now use your provider for formatting
Validator<User> nameValidator = new ObjectValidator<>("name", 
    List.of(CommonRules.required().withMessage("name.required_key")), 
    User::getName);

// 4. Pass the context to the engine
DataValidator<User> engine = new DataValidator<>(List.of(nameValidator), context);
engine.validate(user);
```

---

## 🛠 Customization

### Custom Rule
```java
RuleValidator<String> emailRule = (value, context, fieldName) -> 
    value.contains("@") ? null : ValidationError.fail(fieldName, "Invalid email format");
```

### Custom Handler (e.g., Logging Errors)
```java
public class LogHandler implements ValidationResultHandler {
    @Override
    public <T> void handle(T t, List<ValidationError> errors) {
        if (errors != null) System.err.println("Object " + t + " has " + errors.size() + " errors");
    }

    @Override
    public boolean support(Class<?> clazz) { return true; }
}
```

---

## ⚖️ License

Project is licensed under the **Apache License 2.0**.

---
*Created with ❤️ by **Le Tan Phat***
