# 📝 Automated Text Processing System

A high-performance Java application designed to automate large-scale text processing using **regular expressions**, **file handling**, and **JavaFX** for an intuitive user interface. Developed as part of a lab project for **DataFlow Solutions**, this tool aims to replace inefficient manual text-processing workflows with automated, scalable solutions.

---

## 🚀 Features

### ✅ Core Functionalities
- **Regex Search, Match, Replace**  
  Dynamic regex support for locating and modifying text patterns.
- **Batch File Processing**  
  Process multiple files in a single run to automate text cleanup.
- **Text Analytics**  
  Word frequency analysis and pattern-based summaries.
- **Stream-Based Processing**  
  Efficient handling of large text data using Java Streams API.
- **Data Management**  
  Manage processed text entries with collection operations.
- **File Handling**  
  Read/write large files using `BufferedReader`/`BufferedWriter`.

### 🎨 User Interface (JavaFX)
- Input text and regex patterns
- Highlight matches and replacements
- Manage and process files through a GUI
- View results, analytics, and processing history

---

## 🧱 Architecture: MVC Design Pattern


- **Model:** Business logic and data structures
- **View:** JavaFX interface components
- **Controller:** Logic to handle UI interaction and connect model/view

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── atps/
│   │           └── automatedtextprocessingsystem/
│   │               ├── model/
│   │               │   ├── TextProcessingModel.java
│   │               │   ├── RegexModel.java
│   │               │   ├── FileModel.java
│   │               │   └── DataModel.java
|   |               |   └── TextProcessingAppModel.java
│   │               ├── view/
│   │               │   ├── MainView.java
│   │               │   ├── TextEditorView.java
│   │               │   ├── RegexView.java
│   │               │   ├── ResultsView.java
│   │               │   └── FileOperationsView.java
│   │               ├── controller/
│   │               │   ├── MainController.java
│   │               │   ├── TextProcessingController.java
│   │               │   ├── FileController.java
│   │               │   └── RegexController.java
|   |               ├── util/
│   │               │   ├── TextProcessingUtils.java
│   │               │   ├── TextProcessingException.java
│   │               │   └── ConfigManager.java
│   │               └── TextProcessorApplication.java
│   └── resources/
│       ├── css/
│       ├── fxml/
│       └── images/
└── test/
    └── java/
        └── com/
            └── atps/
            └── automatedtextprocessingsystem/
                ├── model/
                ├── controller/
                └── integration/
```

---

## 🧪 Technologies Used

- Java 17+
- JavaFX
- Regex (`java.util.regex`)
- Java Streams
- Collections Framework
- Logging (`java.util.logging`)
- Maven (recommended for build automation)

## 📹 Video

```
Link
```

## 🧰 How to Run

> Requires Java 17+ and JavaFX SDK

1. Clone the repository:
   ```bash
   git clone https://github.com/Ganza-Kevin-Murinda/Automated-Text-Processing-System.git
   cd Automated-Text-Processing