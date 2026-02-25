package backend;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Modelo {

    // =========================================================
    // ESTADO DEL MODELO (necesario para predecir nuevos casos)
    // =========================================================

    // Mappings de variables categóricas (se construyen durante normalización)
    private static Map<String, Integer> genderMap     = new HashMap<>();
    private static Map<String, Integer> occupationMap = new HashMap<>();
    private static Map<String, Integer> deviceMap     = new HashMap<>();

    // Rangos min/max de variables numéricas (se guardan para normalizar nuevos datos)
    private static double minAge, maxAge;
    private static double minPhone, maxPhone;
    private static double minSocial, maxSocial;
    private static double minProd, maxProd;
    private static double minSleep, maxSleep;
    private static double minApp, maxApp;
    private static double minCaff, maxCaff;
    private static double minScreen, maxScreen;

    // El conjunto de entrenamiento que se usa para predecir
    private static List<double[]> trainSetGlobal = new ArrayList<>();

    // Etiquetas legibles para las 3 clases
    private static final String[] CLASE_NOMBRE = {"Bajo (Relax)", "Medio (Alerta)", "Alto (Peligro/Burnout)"};

    // =========================================================
    // MAIN - Punto de entrada
    // =========================================================

    public static void main(String[] args) {
        String rutaArchivo = "backend/Stress level classification.csv";

        // 1. Cargar datos
        Datos datosManager = new Datos();
        datosManager.cargarDatos(rutaArchivo);
        List<Datos.Registro> registros = datosManager.getRegistros();

        if (registros.isEmpty()) {
            return;
        }

        // 2. Normalizar y dividir datos (Silencioso)
        List<double[]> datosNormalizados = normalizarDatos(registros);

        // 3. Barajar y dividir 80/20
        Collections.shuffle(datosNormalizados);

        int total     = datosNormalizados.size();
        int trainSize = (int) (total * 0.8);

        trainSetGlobal = new ArrayList<>(datosNormalizados.subList(0, trainSize));
        List<double[]> testSet = new ArrayList<>(datosNormalizados.subList(trainSize, total));

        // 4. Evaluar modelo KNN
        int k = 15;
        evaluarModelo(trainSetGlobal, testSet, k);
        
        // 5. Guardar modelo serializado ("stressprediction.onnx")
        guardarModelo("stressprediction.onnx");
        
        // 6. Exportar JSON para GitHub Pages
        exportarAJSON("frontend/modelo_data.json");
    }

    // =========================================================
    // EVALUACIÓN DEL MODELO
    // =========================================================

    public static void evaluarModelo(List<double[]> trainSet, List<double[]> testSet, int k) {
        System.out.println("Evaluación Modelo KNN (k=" + k + ")");

        int maxTest  = Math.min(testSet.size(), 1000); // Limitar test para velocidad en demo
        int aciertos = 0;

        for (int i = 0; i < maxTest; i++) {
            double[] testInstance = testSet.get(i);
            int realClass      = (int) testInstance[testInstance.length - 1]; // Último elemento es la clase
            
            int predictedClass = predictKNN(trainSet, testInstance, k);

            if (predictedClass == realClass) {
                aciertos++;
            }
        }

        double accuracy  = (double) aciertos / maxTest * 100.0;

        // --- Resultados globales ---
        System.out.println("Resultados Finales");
        System.out.printf("Precisión (Accuracy): %.2f%%%n", accuracy);
    }

    // =========================================================
    // ALGORITMO KNN
    // =========================================================

    public static int predictKNN(List<double[]> trainSet, double[] testInstance, int k) {
        List<Par> distancias = new ArrayList<>();

        for (double[] trainInstance : trainSet) {
            double dist = calcularDistanciaEuclidea(testInstance, trainInstance);
            distancias.add(new Par(dist, (int) trainInstance[trainInstance.length - 1]));
        }

        // Ordenar de menor a mayor distancia
        distancias.sort((p1, p2) -> Double.compare(p1.distancia, p2.distancia));

        // Votar entre los k vecinos más cercanos
        Map<Integer, Integer> votos = new HashMap<>();
        for (int i = 0; i < k && i < distancias.size(); i++) {
            int clase = distancias.get(i).clase;
            votos.put(clase, votos.getOrDefault(clase, 0) + 1);
        }

        // Retornar la clase con más votos
        return votos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(-1);
    }
    
    private static double calcularDistanciaEuclidea(double[] inst1, double[] inst2) {
        double sum = 0.0;
        // Excluir la clase target (último elemento)
        for (int i = 0; i < inst1.length - 1; i++) { 
            double diff = inst1[i] - inst2[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    static class Par {
        double distancia;
        int    clase;
        Par(double d, int c) { this.distancia = d; this.clase = c; }
    }

    // =========================================================
    // NORMALIZACIÓN
    // =========================================================

    public static List<double[]> normalizarDatos(List<Datos.Registro> registros) {
        List<double[]> normalizados = new ArrayList<>();
        if (registros.isEmpty()) return normalizados;

        // Inicializar rangos
        minAge = minPhone = minSocial = minProd = minSleep = minApp = minCaff = minScreen = Double.MAX_VALUE;
        maxAge = maxPhone = maxSocial = maxProd = maxSleep = maxApp = maxCaff = maxScreen = Double.MIN_VALUE;

        // Reiniciar mapas
        genderMap.clear();
        occupationMap.clear();
        deviceMap.clear();

        // 1. Calcular rangos y construir mappings
        for (Datos.Registro r : registros) {
            minAge    = Math.min(minAge,    r.age);                    maxAge    = Math.max(maxAge,    r.age);
            minPhone  = Math.min(minPhone,  r.dailyPhoneHours);        maxPhone  = Math.max(maxPhone,  r.dailyPhoneHours);
            minSocial = Math.min(minSocial, r.socialMediaHours);       maxSocial = Math.max(maxSocial, r.socialMediaHours);
            minProd   = Math.min(minProd,   r.workProductivityScore);  maxProd   = Math.max(maxProd,   r.workProductivityScore);
            minSleep  = Math.min(minSleep,  r.sleepHours);             maxSleep  = Math.max(maxSleep,  r.sleepHours);
            minApp    = Math.min(minApp,    r.appUsageCount);          maxApp    = Math.max(maxApp,    r.appUsageCount);
            minCaff   = Math.min(minCaff,   r.caffeineIntakeCups);     maxCaff   = Math.max(maxCaff,   r.caffeineIntakeCups);
            minScreen = Math.min(minScreen, r.weekendScreenTimeHours); maxScreen = Math.max(maxScreen, r.weekendScreenTimeHours);

            genderMap.putIfAbsent(r.gender,      genderMap.size());
            occupationMap.putIfAbsent(r.occupation, occupationMap.size());
            deviceMap.putIfAbsent(r.deviceType,  deviceMap.size());
        }

        int maxGender = Math.max(genderMap.size() - 1, 1);
        int maxOcc    = Math.max(occupationMap.size() - 1, 1);
        int maxDevice = Math.max(deviceMap.size() - 1, 1);

        // 2. Construir vectores
        for (Datos.Registro r : registros) {
            double[] vector = new double[12]; 

            vector[0]  = normalizar(r.age,                   minAge,    maxAge);
            vector[1]  = (double) genderMap.get(r.gender)          / maxGender;
            vector[2]  = (double) occupationMap.get(r.occupation)   / maxOcc;
            vector[3]  = (double) deviceMap.get(r.deviceType)       / maxDevice;
            vector[4]  = normalizar(r.dailyPhoneHours,       minPhone,  maxPhone);
            vector[5]  = normalizar(r.socialMediaHours,      minSocial, maxSocial);
            vector[6]  = normalizar(r.workProductivityScore, minProd,   maxProd);
            vector[7]  = normalizar(r.sleepHours,            minSleep,  maxSleep);
            vector[8]  = normalizar(r.appUsageCount,         minApp,    maxApp);
            vector[9]  = normalizar(r.caffeineIntakeCups,    minCaff,   maxCaff);
            vector[10] = normalizar(r.weekendScreenTimeHours,minScreen, maxScreen);

            // Target (1-10) -> 3 clases
            int label;
            if      (r.stressLevel <= 3) label = 0; // Bajo
            else if (r.stressLevel <= 6) label = 1; // Medio
            else                         label = 2; // Alto
            vector[11] = label;

            normalizados.add(vector);
        }

        return normalizados;
    }

    private static double normalizar(double val, double min, double max) {
        if (max == min) return 0.0;
        return (val - min) / (max - min);
    }

    // =========================================================
    // EXPORTACIÓN WEB (JSON) - Para GitHub Pages
    // =========================================================

    // =========================================================
    // SERIALIZACIÓN (Simulando "onnx")
    // =========================================================

    public static void guardarModelo(String ruta) {
        ModeloSerializado estado = new ModeloSerializado();
        estado.trainSet = trainSetGlobal;
        estado.genderMap = genderMap;
        estado.occupationMap = occupationMap;
        estado.deviceMap = deviceMap;
        
        estado.minAge = minAge; estado.maxAge = maxAge;
        estado.minPhone = minPhone; estado.maxPhone = maxPhone;
        estado.minSocial = minSocial; estado.maxSocial = maxSocial;
        estado.minProd = minProd; estado.maxProd = maxProd;
        estado.minSleep = minSleep; estado.maxSleep = maxSleep;
        estado.minApp = minApp; estado.maxApp = maxApp;
        estado.minCaff = minCaff; estado.maxCaff = maxCaff;
        estado.minScreen = minScreen; estado.maxScreen = maxScreen;

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(estado);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void exportarAJSON(String ruta) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        
        // 1. Rangos
        json.append("  \"ranges\": {\n");
        json.append(String.format("    \"minAge\": %.2f, \"maxAge\": %.2f,\n", minAge, maxAge));
        json.append(String.format("    \"minPhone\": %.2f, \"maxPhone\": %.2f,\n", minPhone, maxPhone));
        json.append(String.format("    \"minSocial\": %.2f, \"maxSocial\": %.2f,\n", minSocial, maxSocial));
        json.append(String.format("    \"minProd\": %.2f, \"maxProd\": %.2f,\n", minProd, maxProd));
        json.append(String.format("    \"minSleep\": %.2f, \"maxSleep\": %.2f,\n", minSleep, maxSleep));
        json.append(String.format("    \"minApp\": %.2f, \"maxApp\": %.2f,\n", minApp, maxApp));
        json.append(String.format("    \"minCaff\": %.2f, \"maxCaff\": %.2f,\n", minCaff, maxCaff));
        json.append(String.format("    \"minScreen\": %.2f, \"maxScreen\": %.2f\n", minScreen, maxScreen));
        json.append("  },\n");

        // 2. Mappings (Invertir mapa para exportar Nombre -> Valor no es necesario, el JS necesita Nombre -> Valor)
        // Pero aquí ya tenemos Nombre -> Valor
        json.append("  \"genderMap\": ").append(mapToJson(genderMap)).append(",\n");
        json.append("  \"occupationMap\": ").append(mapToJson(occupationMap)).append(",\n");
        json.append("  \"deviceMap\": ").append(mapToJson(deviceMap)).append(",\n");

        // 3. TrainSet (Array de arrays)
        json.append("  \"trainSet\": [\n");
        for (int i = 0; i < trainSetGlobal.size(); i++) {
            double[] vec = trainSetGlobal.get(i);
            json.append("    [");
            for (int j = 0; j < vec.length; j++) {
                json.append(String.format(java.util.Locale.US, "%.4f", vec[j])); // Forzar punto decimal
                if (j < vec.length - 1) json.append(", ");
            }
            json.append("]");
            if (i < trainSetGlobal.size() - 1) json.append(",\n");
        }
        json.append("\n  ]\n");
        json.append("}");

        try (FileOutputStream fos = new FileOutputStream(ruta)) {
            fos.write(json.toString().getBytes());
            // System.out.println("Modelo JSON exportado a: " + ruta);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String mapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int count = 0;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (++count < map.size()) sb.append(", ");
        }
        sb.append("}");
        return sb.toString();
    }

    
    public static void cargarModelo(String ruta) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ruta))) {
            ModeloSerializado estado = (ModeloSerializado) ois.readObject();
            
            trainSetGlobal = estado.trainSet;
            genderMap = estado.genderMap;
            occupationMap = estado.occupationMap;
            deviceMap = estado.deviceMap;
            
            minAge = estado.minAge; maxAge = estado.maxAge;
            minPhone = estado.minPhone; maxPhone = estado.maxPhone;
            minSocial = estado.minSocial; maxSocial = estado.maxSocial;
            minProd = estado.minProd; maxProd = estado.maxProd;
            minSleep = estado.minSleep; maxSleep = estado.maxSleep;
            minApp = estado.minApp; maxApp = estado.maxApp;
            minCaff = estado.minCaff; maxCaff = estado.maxCaff;
            minScreen = estado.minScreen; maxScreen = estado.maxScreen;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Clase auxiliar interna para serializar
    static class ModeloSerializado implements Serializable {
        private static final long serialVersionUID = 1L;
        List<double[]> trainSet;
        Map<String, Integer> genderMap;
        Map<String, Integer> occupationMap;
        Map<String, Integer> deviceMap;
        double minAge, maxAge;
        double minPhone, maxPhone;
        double minSocial, maxSocial;
        double minProd, maxProd;
        double minSleep, maxSleep;
        double minApp, maxApp;
        double minCaff, maxCaff;
        double minScreen, maxScreen;
    }
    
    // Método público para predecir (usado por interfaz externa)
    public static String predecirNuevoUsuario(int age, String gender, String occupation,
                                               String deviceType, double dailyPhoneHours,
                                               double socialMediaHours, double workProductivityScore,
                                               double sleepHours, int appUsageCount,
                                               double caffeineIntakeCups, double weekendScreenTimeHours,
                                               int k) {
        if (trainSetGlobal == null || trainSetGlobal.isEmpty()) {
            return "Error: Modelo no cargado.";
        }

        double[] vector = new double[12];
        int maxGender = Math.max(genderMap.size() - 1, 1);
        int maxOcc    = Math.max(occupationMap.size() - 1, 1);
        int maxDevice = Math.max(deviceMap.size() - 1, 1);

        vector[0]  = normalizar(age,                   minAge,    maxAge);
        vector[1]  = (double) genderMap.getOrDefault(gender, 0) / maxGender;
        vector[2]  = (double) occupationMap.getOrDefault(occupation, 0) / maxOcc;
        vector[3]  = (double) deviceMap.getOrDefault(deviceType, 0) / maxDevice;
        vector[4]  = normalizar(dailyPhoneHours,       minPhone,  maxPhone);
        vector[5]  = normalizar(socialMediaHours,      minSocial, maxSocial);
        vector[6]  = normalizar(workProductivityScore, minProd,   maxProd);
        vector[7]  = normalizar(sleepHours,            minSleep,  maxSleep);
        vector[8]  = normalizar(appUsageCount,         minApp,    maxApp);
        vector[9]  = normalizar(caffeineIntakeCups,    minCaff,   maxCaff);
        vector[10] = normalizar(weekendScreenTimeHours,minScreen, maxScreen);
        vector[11] = -1; 

        int clasePredicha = predictKNN(trainSetGlobal, vector, k);
        return CLASE_NOMBRE[clasePredicha];
    }
}