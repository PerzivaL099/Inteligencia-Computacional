package mx.edu.cetys;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Map;

public class MantenimientoPredictivoGui extends JFrame {

    // Campos de texto para la entrada dinámica
    private JTextField txtTempAire;
    private JTextField txtTempProceso;
    private JTextField txtRpm;
    private JTextField txtTorque;
    private JTextField txtDesgaste;

    // Ruta del modelo
    private final String MODEL_PATH = "modelo_mantenimiento.onnx";

    public MantenimientoPredictivoGui() {
        // Configuración básica de la ventana
        setTitle("Predictor de Mantenimiento (ONNX)");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 10, 10));

        // Inicializar componentes
        add(new JLabel("Temp Aire (K):"));
        txtTempAire = new JTextField("298.1");
        add(txtTempAire);

        add(new JLabel("Temp Proceso (K):"));
        txtTempProceso = new JTextField("308.6");
        add(txtTempProceso);

        add(new JLabel("Velocidad (RPM):"));
        txtRpm = new JTextField("1551.0");
        add(txtRpm);

        add(new JLabel("Torque (Nm):"));
        txtTorque = new JTextField("42.8");
        add(txtTorque);

        add(new JLabel("Desgaste Herramienta (min):"));
        txtDesgaste = new JTextField("0");
        add(txtDesgaste);

        JButton btnPredecir = new JButton("Ejecutar Inferencia");
        add(new JLabel("")); // Espacio vacío para alinear el botón
        add(btnPredecir);

        // Acción del botón
        btnPredecir.addActionListener(e -> realizarPrediccion());
    }

    private void realizarPrediccion() {
        try {
            // 1. Validaciones de entrada y extracción de datos
            float tempAire = Float.parseFloat(txtTempAire.getText());
            float tempProceso = Float.parseFloat(txtTempProceso.getText());
            float rpm = Float.parseFloat(txtRpm.getText());
            float torque = Float.parseFloat(txtTorque.getText());
            float desgaste = Float.parseFloat(txtDesgaste.getText());

            float[][] sensorData = new float[][] {
                    { tempAire, tempProceso, rpm, torque, desgaste }
            };

            // 2. Ejecutar el modelo ONNX (Lógica aislada)
            int resultado = ejecutarModeloOnnx(sensorData);

            // 3. Mostrar resultado al usuario
            if (resultado == 1) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ ALERTA: Predicción de FALLA en la máquina.",
                        "Resultado de Inferencia",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "✅ OK: La máquina funciona correctamente.",
                        "Resultado de Inferencia",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: Por favor ingresa solo valores numéricos válidos.",
                    "Error de Entrada",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error ejecutando el modelo: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private int ejecutarModeloOnnx(float[][] inputData) throws OrtException {

        try (OrtEnvironment env = OrtEnvironment.getEnvironment();
                OrtSession.SessionOptions options = new OrtSession.SessionOptions();
                OrtSession session = env.createSession(MODEL_PATH, options);
                OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {

            String inputName = session.getInputNames().iterator().next();
            Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);

            try (OrtSession.Result results = session.run(inputs)) {
                long[] labels = (long[]) results.get(0).getValue();
                return (int) labels[0];
            }
        }
    }

    public static void main(String[] args) {
        // Asegura que la interfaz gráfica se construya en el hilo correcto (Event
        // Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            new MantenimientoPredictivoGui().setVisible(true);
        });
    }
}
