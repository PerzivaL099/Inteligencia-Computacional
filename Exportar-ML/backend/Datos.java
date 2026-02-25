package backend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Datos {
    private List<Registro> registros = new ArrayList<>();

    public static class Registro {
        // userId eliminado porque no es relevante para el modelo
        public int age;
        public String gender;
        public String occupation;
        public String deviceType;
        public double dailyPhoneHours;
        public double socialMediaHours;
        public double workProductivityScore;
        public double sleepHours;
        public int stressLevel;
        public int appUsageCount;
        public double caffeineIntakeCups;
        public double weekendScreenTimeHours;

        public Registro(int age, String gender, String occupation, String deviceType,
                        double dailyPhoneHours, double socialMediaHours, double workProductivityScore,
                        double sleepHours, int stressLevel, int appUsageCount, double caffeineIntakeCups,
                        double weekendScreenTimeHours) {
            this.age = age;
            this.gender = gender;
            this.occupation = occupation;
            this.deviceType = deviceType;
            this.dailyPhoneHours = dailyPhoneHours;
            this.socialMediaHours = socialMediaHours;
            this.workProductivityScore = workProductivityScore;
            this.sleepHours = sleepHours;
            this.stressLevel = stressLevel;
            this.appUsageCount = appUsageCount;
            this.caffeineIntakeCups = caffeineIntakeCups;
            this.weekendScreenTimeHours = weekendScreenTimeHours;
        }

        @Override
        public String toString() {
            return "Registro{" +
                    "Stress=" + stressLevel +
                    ", Age=" + age +
                    '}';
        }
    }

    public void cargarDatos(String rutaArchivo) {
        String linea = "";
        String separador = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            // Leer encabezado
            br.readLine();

            while ((linea = br.readLine()) != null) {
                // Saltar líneas vacías
                if (linea.trim().isEmpty()) continue;
                
                String[] datos = linea.split(separador);
                
                // Asegurar que tenemos suficientes columnas
                if (datos.length < 13) {
                     System.err.println("Línea incompleta o mal formateada (columnas=" + datos.length + "): " + linea);
                     continue;
                }

                try {
                    // Ignoramos datos[0] que es el User_ID
                    Registro registro = new Registro(
                        Integer.parseInt(datos[1]),
                        datos[2],
                        datos[3],
                        datos[4],
                        Double.parseDouble(datos[5]),
                        Double.parseDouble(datos[6]),
                        Double.parseDouble(datos[7]),
                        Double.parseDouble(datos[8]),
                        Integer.parseInt(datos[9]),
                        Integer.parseInt(datos[10]),
                        Double.parseDouble(datos[11]),
                        Double.parseDouble(datos[12])
                    );
                    registros.add(registro);
                } catch (NumberFormatException e) {
                    System.err.println("Error al parsear línea: " + linea + " - " + e.getMessage());
                }
            }
            System.out.println("Datos cargados exitosamente: " + registros.size() + " registros.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Registro> getRegistros() {
        return registros;
    }
}
