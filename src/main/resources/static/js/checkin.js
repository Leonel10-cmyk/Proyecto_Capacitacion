let html5QrCode;
let isProcessing = false;

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('auth_token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    iniciarCamara();
});

function iniciarCamara() {
    html5QrCode = new Html5Qrcode("reader");
    const config = { fps: 10, qrbox: { width: 220, height: 220 } };

    html5QrCode.start(
        { facingMode: "environment" },
        config,
        onScanSuccess
    ).catch(err => {
        console.warn("No se pudo iniciar cámara trasera, probando cualquier cámara...", err);
        html5QrCode.start({ facingMode: "user" }, config, onScanSuccess)
            .catch(e => {
                document.getElementById('reader').innerHTML = `
                    <div class="p-6 text-gray-400 text-xs">
                        <i class="fa-solid fa-video-slash text-2xl mb-2 text-amber-400"></i>
                        <p>No se pudo acceder a la cámara. Puedes utilizar la búsqueda manual por DNI abajo.</p>
                    </div>
                `;
            });
    });
}

function onScanSuccess(decodedText) {
    if (isProcessing) return;
    isProcessing = true;

    // Extraer DNI del contenido escaneado (soporta formato "DNI: 12345678" o directo "12345678" o "QR-12345678-...")
    let dni = decodedText;
    const match = decodedText.match(/\b\d{8}\b/);
    if (match) {
        dni = match[0];
    }

    enviarCheckin(dni);
}

function marcarManual() {
    const dni = document.getElementById('manualDni').value.trim();
    if (!dni) {
        alert('Ingresa un número de DNI');
        return;
    }
    enviarCheckin(dni);
}

async function enviarCheckin(dni) {
    const token = localStorage.getItem('auth_token');
    const dia = parseInt(document.getElementById('daySelect').value, 10);
    const resultBox = document.getElementById('resultBox');
    const statusIcon = document.getElementById('statusIcon');
    const personName = document.getElementById('personName');
    const resultMessage = document.getElementById('resultMessage');
    const resetBtn = document.getElementById('resetBtn');

    try {
        const res = await fetch('/api/admin/checkin/marcar', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ dni: dni, dia: dia })
        });

        const data = await res.json();

        resultBox.classList.remove('hidden');
        resetBtn.classList.remove('hidden');

        if (res.ok && data.success) {
            const p = data.data;
            resultBox.className = 'p-4 rounded-2xl mb-4 bg-green-500/20 text-green-300 border border-green-500/30';
            statusIcon.className = 'w-12 h-12 mx-auto rounded-full flex items-center justify-center mb-2 bg-green-500/20 text-green-400';
            statusIcon.innerHTML = '<i class="fa-solid fa-check text-xl"></i>';
            personName.textContent = `${p.nombre} ${p.apellidos}`;
            resultMessage.textContent = `¡Asistencia del Día ${dia} registrada con éxito! (Estado de pago: ${p.estadoPago})`;
        } else {
            resultBox.className = 'p-4 rounded-2xl mb-4 bg-red-500/20 text-red-300 border border-red-500/30';
            statusIcon.className = 'w-12 h-12 mx-auto rounded-full flex items-center justify-center mb-2 bg-red-500/20 text-red-400';
            statusIcon.innerHTML = '<i class="fa-solid fa-xmark text-xl"></i>';
            personName.textContent = `DNI: ${dni}`;
            resultMessage.textContent = data.message || 'Error al validar ticket';
        }
    } catch (e) {
        resultBox.classList.remove('hidden');
        resetBtn.classList.remove('hidden');
        resultBox.className = 'p-4 rounded-2xl mb-4 bg-red-500/20 text-red-300 border border-red-500/30';
        resultMessage.textContent = 'Error de red o servidor: ' + e.message;
    }
}

function reiniciarEscaneo() {
    isProcessing = false;
    document.getElementById('resultBox').classList.add('hidden');
    document.getElementById('resetBtn').classList.add('hidden');
    document.getElementById('manualDni').value = '';
}

function logout() {
    localStorage.removeItem('auth_token');
    window.location.href = 'login.html';
}
