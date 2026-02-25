# Sistema de Mantenimiento Predictivo (Python + Java + ONNX)

Este proyecto implementa una arquitectura de Machine Learning en dos fases separando el entorno de ciencia de datos del entorno de producción. Utiliza **Python** para el entrenamiento y exportación del modelo, y **Java (Swing)** para la interfaz gráfica y la ejecución de inferencias mediante **ONNX Runtime**.

##  Arquitectura del Sistema
1. **Capa de Entrenamiento (Python):** Procesa el dataset `ai4i2020.csv`, entrena un pipeline (StandardScaler + RandomForest) y exporta todo a un archivo binario `.onnx`.
2. **Capa de Inferencia (Java):** Una aplicación de escritorio estática que carga el entorno de ONNX, toma datos ingresados por el usuario y ejecuta la predicción sin necesidad de dependencias de Python instaladas.

##  Prerrequisitos
Para ejecutar este proyecto de forma manual desde la terminal, necesitas:
* **Python 3.x** con las librerías: `pandas`, `scikit-learn`, `skl2onnx`, `onnx`.
* **JDK (Java Development Kit)** instalado y configurado en tus variables de entorno.
* **Dataset:** El archivo `ai4i2020.csv` ubicado en la raíz del proyecto.
* **Librería ONNX:** El archivo `onnxruntime-1.24.2.jar` descargado manualmente y colocado en la carpeta `libs/`.

##  Estructura de Carpetas Esperada
```text
Modelo - ML/
│
├── libs/
│   └── onnxruntime-1.24.2.jar
├── src/
│   └── main/
│       └── java/
│           ├── MantenimientoPredictivoApp.java
│           └── MantenimientoPredictivoGUI.java
│
├── ai4i2020.csv
├── pipeline.py
└── README.md

python pipeline.py

javac -cp "libs/*" src/main/java/*.java

java -cp "libs/*;src/main/java" MantenimientoPredictivoGUI