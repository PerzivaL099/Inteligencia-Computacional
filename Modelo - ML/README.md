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
* **Maven:** Maven instalado y configurado correctamente en tu sistema.


##  Estructura de Carpetas Esperada
```text
Modelo - ML/
│
├── src/
│   └── main/
│       └── java/
│           └── mx/
│               └── edu/
│                  └── cetys/
│                       ├── MantenimientoPredictivoCli.java
│                       └── MantenimientoPredictivoGui.java
│
├── ai4i2020.csv
├── pipeline.py
├── pom.xml
└── README.md
```

## Instrucciones de uso

Entrena el modelo con:

```bash
python pipeline.py
```

Para probar rápidamente, puedes utilizar el CLI de ejemplo (siempre realiza la misma predicción) con el siguiente comando:

```bash
mvn -Pcli
```

Para ejecutar el GUI interactivo, ejecuta el siguiente comando:

```bash
mvn -Pgui
```

