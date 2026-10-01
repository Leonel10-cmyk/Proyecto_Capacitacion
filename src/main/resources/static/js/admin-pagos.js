document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('auth_token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const yapeForm = document.getElementById('yapeForm');
    const yapeFile = document.getElementById('yapeFile');
    const yapeFileName = document.getElementById('yapeFileName');

    yapeFile.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
            yapeFileName.textContent = `Archivo seleccionado: ${e.target.files[0].name}`;
            yapeFileName.classList.add('text-purple-300');
        } else {
            yapeFileName.textContent = 'Haz clic aquí para seleccionar el archivo CSV de Yape';
            yapeFileName.classList.remove('text-purple-300');
        }
    });

    yapeForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const alertBox = document.getElementById('yapeAlert');
        const btn = document.getElementById('btnProcesarYape');

        if (!yapeFile.files || !yapeFile.files[0]) {
            alert('Por favor selecciona un archivo CSV de Yape');
            return;
        }

        const formData = new FormData();
        formData.append('file', yapeFile.files[0]);

        btn.disabled = true;
        btn.textContent = 'Procesando en SQL Server...';
        alertBox.classList.add('hidden');

        try {
            const res = await fetch('/api/admin/pagos/conciliacion-yape', {
                method: 'POST',
                headers: { 'Authorization': 'Bearer ' + token },
                body: formData
            });

            const data = await res.json();
            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Error en la conciliación');
            }

            const info = data.data;
            alertBox.className = 'p-4 rounded-xl text-xs font-medium bg-green-500/20 text-green-300 border border-green-500/30';
            alertBox.innerHTML = `
                <div class="font-bold text-sm mb-1">✅ Conciliación Exitosa</div>
                <div>Códigos analizados: <b>${info.totalCodigosEncontrados}</b></div>
                <div>Pagos validados y actualizados en SQL Server: <b>${info.pagosValidados}</b></div>
            `;
            alertBox.classList.remove('hidden');

        } catch (err) {
            alertBox.className = 'p-4 rounded-xl text-xs font-medium bg-red-500/20 text-red-300 border border-red-500/30';
            alertBox.textContent = '❌ ' + err.message;
            alertBox.classList.remove('hidden');
        } finally {
            btn.disabled = false;
            btn.textContent = 'Procesar Validación Yape';
        }
    });
});

async function buscarPagos() {
    const token = localStorage.getItem('auth_token');
    const query = document.getElementById('pagosSearchInput').value.trim();
    const tbody = document.getElementById('pagosResultBody');

    if (!query) {
        alert('Ingresa un término de búsqueda');
        return;
    }

    tbody.innerHTML = `
        <tr><td colspan="7" class="text-center py-6 text-gray-400">
            <i class="fa-solid fa-spinner animate-spin mr-2"></i> Consultando SQL Server...
        </td></tr>
    `;

    try {
        const res = await fetch(`/api/admin/participantes?query=${encodeURIComponent(query)}`, {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        const data = await res.json();
        const lista = data.data || [];

        if (lista.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-gray-400">No se encontraron resultados.</td></tr>`;
            return;
        }

        let html = '';
        lista.forEach(p => {
            html += `
                <tr class="hover:bg-white/[0.02]">
                    <td class="py-3 px-4">
                        <div class="font-semibold text-white">${p.nombre} ${p.apellidos}</div>
                        <div class="text-[11px] font-mono text-indigo-300">DNI: ${p.dni}</div>
                    </td>
                    <td class="py-3 px-4">${p.modalidad}</td>
                    <td class="py-3 px-4 font-semibold text-gray-200">S/ ${(p.costoTotal || 0).toFixed(2)}</td>
                    <td class="py-3 px-4 font-bold text-green-400">S/ ${(p.totalPagado || 0).toFixed(2)}</td>
                    <td class="py-3 px-4 font-bold text-amber-400">S/ ${(p.deuda || 0).toFixed(2)}</td>
                    <td class="py-3 px-4"><span class="px-2 py-0.5 rounded text-[10px] font-semibold bg-white/10">${p.estadoPago}</span></td>
                    <td class="py-3 px-4 text-center">
                        <a href="admin-tabla.html" class="bg-indigo-600 hover:bg-indigo-500 text-white px-3 py-1 rounded-lg text-xs font-medium inline-block">
                            Ver en Hoja de Datos
                        </a>
                    </td>
                </tr>
            `;
        });

        tbody.innerHTML = html;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-6 text-red-400">${err.message}</td></tr>`;
    }
}

function logout() {
    localStorage.removeItem('auth_token');
    window.location.href = 'login.html';
}
