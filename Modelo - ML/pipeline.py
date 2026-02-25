import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline
from sklearn.metrics import classification_report
from skl2onnx import convert_sklearn
from skl2onnx.common.data_types import FloatTensorType

# 1. Carga de datos reales desde el archivo CSV
print("Cargando dataset...")
df = pd.read_csv('ai4i2020.csv')

# Definimos las 5 características (features) que nuestro modelo de Java espera
columnas_features = [
    'Air temperature [K]', 
    'Process temperature [K]', 
    'Rotational speed [rpm]', 
    'Torque [Nm]', 
    'Tool wear [min]'
]

# Extraemos las variables de entrada (X) y la etiqueta a predecir (y)
X = df[columnas_features].values
y = df['Machine failure'].values

# Dividimos en conjunto de entrenamiento y prueba
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# 2. Definición del Pipeline Arquitectónico
pipeline = Pipeline([
    ('scaler', StandardScaler()),
    ('classifier', RandomForestClassifier(n_estimators=100, random_state=42, class_weight='balanced')) # Añadí class_weight para datos desbalanceados
])

# 3. Entrenamiento y Evaluación
print("Entrenando modelo...")
pipeline.fit(X_train, y_train)
y_pred = pipeline.predict(X_test)
print("Reporte de Clasificación:\n", classification_report(y_test, y_pred))

# 4. Exportación a ONNX
initial_type = [('float_input', FloatTensorType([None, 5]))]
onnx_model = convert_sklearn(pipeline, initial_types=initial_type)

with open("modelo_mantenimiento.onnx", "wb") as f:
    f.write(onnx_model.SerializeToString())

print("Modelo exportado exitosamente a 'modelo_mantenimiento.onnx'")