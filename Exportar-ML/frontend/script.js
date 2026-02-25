document.addEventListener('DOMContentLoaded', () => {
    let modelData = null;

    // Cargar el modelo desde el JSON generado por Java
    fetch('modelo_data.json')
        .then(response => response.json())
        .then(data => {
            modelData = data;
            console.log("Modelo cargado:", modelData);
            document.getElementById('status').innerText = "Modelo cargado y listo.";
            document.getElementById('predictBtn').disabled = false;
        })
        .catch(error => {
            console.error("Error cargando modelo:", error);
            document.getElementById('status').innerText = "Error cargando modelo. Asegúrate de ejecutar el backend Java primero.";
        });

    document.getElementById('predictBtn').addEventListener('click', () => {
        if (!modelData) return;

        const inputs = obtenerInputs();
        if (!inputs) return;

        const prediction = predecirStress(inputs, modelData);
        mostrarResultado(prediction);
    });

    function obtenerInputs() {
        // Obtener valores del DOM
        const age = parseFloat(document.getElementById('age').value);
        const gender = document.getElementById('gender').value;
        const occupation = document.getElementById('occupation').value;
        const device = document.getElementById('device').value;

        const items = [
            'dailyPhone', 'socialMedia', 'productivity', 'sleep',
            'apps', 'caffeine', 'weekendScreen'
        ];

        const numValues = {};
        for (let id of items) {
            let val = parseFloat(document.getElementById(id).value);
            if (isNaN(val)) {
                alert(`Por favor ingresa un número válido para ${id}`);
                return null;
            }
            numValues[id] = val;
        }

        if (isNaN(age)) {
            alert("Edad inválida");
            return null;
        }

        return { age, gender, occupation, device, ...numValues };
    }

    function predecirStress(inputs, model) {
        // 1. Normalizar Vector de Entrada
        const vec = [];
        const r = model.ranges;
        const m = model; // mappings

        // Normalización Min-Max igual que en Java
        // vec[0] Age
        vec.push(norm(inputs.age, r.minAge, r.maxAge));

        // Categóricos
        const maxGender = Math.max(Object.keys(m.genderMap).length - 1, 1);
        const maxOcc = Math.max(Object.keys(m.occupationMap).length - 1, 1);
        const maxDevice = Math.max(Object.keys(m.deviceMap).length - 1, 1);

        vec.push((m.genderMap[inputs.gender] || 0) / maxGender);
        vec.push((m.occupationMap[inputs.occupation] || 0) / maxOcc);
        vec.push((m.deviceMap[inputs.device] || 0) / maxDevice);

        // Resto nums
        vec.push(norm(inputs.dailyPhone, r.minPhone, r.maxPhone));
        vec.push(norm(inputs.socialMedia, r.minSocial, r.maxSocial));
        vec.push(norm(inputs.productivity, r.minProd, r.maxProd));
        vec.push(norm(inputs.sleep, r.minSleep, r.maxSleep));
        vec.push(norm(inputs.apps, r.minApp, r.maxApp));
        vec.push(norm(inputs.caffeine, r.minCaff, r.maxCaff));
        vec.push(norm(inputs.weekendScreen, r.minScreen, r.maxScreen));

        // 2. KNN (k=15)
        const k = 15;
        const distancias = [];

        // Calcular distancia con todos los puntos de entrenamiento
        for (let i = 0; i < model.trainSet.length; i++) {
            const trainVec = model.trainSet[i];
            const dist = euclideanDist(vec, trainVec);
            // La clase es el último elemento del vector de entrenamiento
            const clase = trainVec[trainVec.length - 1];
            distancias.push({ dist, clase });
        }

        // Ordenar
        distancias.sort((a, b) => a.dist - b.dist);

        // Votar
        const votos = {};
        for (let i = 0; i < k; i++) {
            const c = distancias[i].clase;
            votos[c] = (votos[c] || 0) + 1;
        }

        // Ganador
        let maxVotos = -1;
        let ganador = -1;
        for (let c in votos) {
            if (votos[c] > maxVotos) {
                maxVotos = votos[c];
                ganador = c;
            }
        }

        return ganador; // 0, 1, 2
    }

    function norm(val, min, max) {
        if (max === min) return 0;
        return (val - min) / (max - min);
    }

    function euclideanDist(v1, v2) {
        let sum = 0;
        // v1 tiene length 11. v2 tiene 12 (inc clase). Iteramos sobre v1.length
        for (let i = 0; i < v1.length; i++) {
            let d = v1[i] - v2[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    function mostrarResultado(clase) {
        const resultDiv = document.getElementById('result');
        const labels = ["Bajo (Relax)", "Medio (Alerta)", "Alto (Peligro/Burnout)"];
        const colors = ["#4CAF50", "#FFC107", "#F44336"];

        resultDiv.innerHTML = `<h3>Nivel de Estrés Predicho:</h3>
                               <h2 style="color: ${colors[clase]}">${labels[clase]}</h2>`;
        resultDiv.style.display = 'block';
    }
});
