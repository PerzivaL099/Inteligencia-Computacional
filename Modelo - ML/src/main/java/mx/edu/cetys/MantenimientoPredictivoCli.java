package mx.edu.cetys;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.util.Collections;
import java.util.Map;

public class MantenimientoPredictivoCli {

    public static void main(String[] args) {
        // Ruta al archivo exportado desde Python
        String modelPath = "modelo_mantenimiento.onnx";

        try {
            // 1. Inicializar el entorno de ONNX Runtime
            OrtEnvironment env = OrtEnvironment.getEnvironment();

            // 2. Cargar el modelo en una sesión
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            OrtSession session = env.createSession(modelPath, options);

            System.out.println("Modelo ONNX cargado correctamente.");

            // 3. Preparar los datos de entrada ("Hardcodeados")
            // Los valores: [Temp Aire, Temp Proceso, RPM, Torque, Desgaste]
            // Nota: ONNX espera un arreglo 2D [batch_size][num_features]
            float[][] sensorData = new float[][] {
                    { 298.1f, 308.6f, 1551.0f, 42.8f, 0.0f } // Ejemplo de lectura de un sensor
            };

            // 4. Crear el Tensor de ONNX
            OnnxTensor inputTensor = OnnxTensor.createTensor(env, sensorData);

            // El nombre de la entrada debe coincidir con el 'initial_type' de Python
            String inputName = session.getInputNames().iterator().next();
            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

            // 5. Ejecutar la inferencia
            try (OrtSession.Result results = session.run(inputs)) {

                // Extraer la predicción (Suele regresar un arreglo de arreglos con las
                // etiquetas)
                long[] labels = (long[]) results.get(0).getValue();

                System.out.println("--- Resultados de Inferencia ---");
                if (labels[0] == 1) {
                    System.out.println(" Predicción de FALLA en la máquina.");
                } else {
                    System.out.println(" La máquina funciona correctamente.");
                }
            }

            // Liberar recursos
            inputTensor.close();
            session.close();
            env.close();

        } catch (OrtException e) {
            System.err.println("Error ejecutando ONNX Runtime: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
