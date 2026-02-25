# Introducción
En esta actividad entrenarás y exportarás un modelo de ML de modo que este pueda ser utilizado en una aplicación.



Esta aplicación puede tomar cualquier forma, desde una aplicación que imprima en la terminal a un sitio web; la única restricción es que debe hacer uso del modelo generado para ejecutar inferencia.


# Para puntos extra, puedes:

Utilizar ONNX para ejecutar inferencia en un entorno diferente a Python (se sugiere Java, C# o node.js)
Crear una interfaz gráfica para permitir entrada dinámica, incluyendo las validaciones de entrada pertinentes


# Instrucciones
Selecciona un conjunto de datos relacionado a algún tema o problemática de tu interés
Se recomienda utilizar Kaggle para la búsqueda del conjunto de datos
El conjunto de datos deberá poder utilizarse para resolver algún problema de clasificación o de regresión.
En un notebook de Jupyter:
Carga el conjunto de datos y realiza EDA
Define un pipeline de preprocesamiento
Entrena un modelo y evalúa su desempeño en términos de la métrica que consideres más apropiada
Exporta el modelo (incluyendo el pipeline de preprocesamiento) a un archivo
En un proyecto nuevo:
Carga el modelo desde el archivo al que fue exportado
Realiza una predicción (puedes "hardcodear" los datos de entrada)
# Modo de entrega
Enlaces a:


Notebook de entrenamiento (GitHub)
Modelo serializado (Google Drive)
Aplicación de inferencia(GitHub)