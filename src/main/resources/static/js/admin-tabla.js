let listaGlobal = [];

document.addEventListener('DOMContentLoaded', () => {
    // 1. Verificar autenticación
    const token = localStorage.getItem('auth_token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    const userName = localStorage.getItem('auth_name') || localStorage.getItem('auth_user') || 'Administrador';
    const userDisplay = document.getElementById('userDisplayName');
    if (userDisplay) userDisplay.textContent = userName;

    // 2. Escuchadores de eventos para filtros, búsqueda y fechas
    const searchInput = document.getElementById('searchInput');
    const filterModalidad = document.getElementById('filterModalidad');
    const filterEstadoPago = document.getElementById('filterEstadoPago');
    const filterEstadoRegistro = document.getElementById('filterEstadoRegistro');
    const filterFechaDesde = document.getElementById('filterFechaDesde');
    const filterFechaHasta = document.getElementById('filterFechaHasta');

    searchInput.addEventListener('input', () => debounce(aplicarFiltrosLocales, 200)());
    filterModalidad.addEventListener('change', aplicarFiltrosLocales);
    filterEstadoPago.addEventListener('change', aplicarFiltrosLocales);
    filterEstadoRegistro.addEventListener('change', aplicarFiltrosLocales);
    filterFechaDesde.addEventListener('change', aplicarFiltrosLocales);
    filterFechaHasta.addEventListener('change', aplicarFiltrosLocales);

    // Formulario de Cuota
    const cuotaForm = document.getElementById('cuotaForm');
    cuotaForm.addEventListener('submit', guardarNuevaCuota);

    // Cargar datos iniciales desde SQL Server
    cargarParticipantes();
});

let debounceTimer;
function debounce(func, delay) {
    return function() {
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(func, delay);
    };
}

// Configurar filtro rápido de fecha
function setFiltroFecha(tipo) {
    const desdeInput = document.getElementById('filterFechaDesde');
    const hastaInput = document.getElementById('filterFechaHasta');
    const hoy = new Date();

    if (tipo === 'hoy') {
        const fechaStr = hoy.toISOString().slice(0, 10);
        desdeInput.value = fechaStr;
        hastaInput.value = fechaStr;
    } else if (tipo === '7dias') {
        const hace7 = new Date();
        hace7.setDate(hoy.getDate() - 7);
        desdeInput.value = hace7.toISOString().slice(0, 10);
        hastaInput.value = hoy.toISOString().slice(0, 10);
    } else {
        desdeInput.value = '';
        hastaInput.value = '';
    }

    aplicarFiltrosLocales();
}

async function cargarParticipantes() {
    const token = localStorage.getItem('auth_token');
    const tablaBody = document.getElementById('tablaBody');
    tablaBody.innerHTML = `
        <tr>
            <td colspan="13" class="text-center py-12 text-slate-400">
                <i class="fa-solid fa-spinner animate-spin text-2xl mb-2 text-indigo-600"></i>
                <p class="font-bold">Cargando registros desde SQL Server...</p>
            </td>
        </tr>
    `;

    try {
        const res = await fetch('/api/admin/participantes', {
            headers: { 'Authorization': 'Bearer ' + token }
        });

        if (res.status === 401 || res.status === 403) {
            logout();
            return;
        }

        const data = await res.json();
        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Error al obtener participantes');
        }

        listaGlobal = data.data || [];
        actualizarEstadisticas(listaGlobal);
        aplicarFiltrosLocales();

    } catch (err) {
        showTableAlert(err.message, true);
        tablaBody.innerHTML = `
            <tr>
                <td colspan="13" class="text-center py-8 text-rose-500 font-bold">
                    <i class="fa-solid fa-triangle-exclamation text-2xl mb-2"></i>
                    <p>${err.message}</p>
                </td>
            </tr>
        `;
    }
}

function formatearFechaRegistro(fechaRaw) {
    if (!fechaRaw) return { fecha: 'Sin fecha', hora: '' };

    try {
        // Soporta formatos "YYYY-MM-DD HH:mm:ss" o "YYYY-MM-DDTHH:mm:ss"
        const clean = fechaRaw.replace(' ', 'T');
        const d = new Date(clean);
        if (isNaN(d.getTime())) {
            return { fecha: fechaRaw.slice(0, 10), hora: fechaRaw.slice(11, 16) };
        }

        const dia = String(d.getDate()).padStart(2, '0');
        const mes = String(d.getMonth() + 1).padStart(2, '0');
        const anio = d.getFullYear();
        const hora = String(d.getHours()).padStart(2, '0');
        const min = String(d.getMinutes()).padStart(2, '0');

        return {
            fecha: `${dia}/${mes}/${anio}`,
            hora: `${hora}:${min}`,
            iso: `${anio}-${mes}-${dia}`
        };
    } catch (e) {
        return { fecha: fechaRaw, hora: '' };
    }
}

function aplicarFiltrosLocales() {
    const q = (document.getElementById('searchInput').value || '').trim().toLowerCase();
    const mod = document.getElementById('filterModalidad').value;
    const pago = document.getElementById('filterEstadoPago').value;
    const reg = document.getElementById('filterEstadoRegistro').value;
    const fechaDesde = document.getElementById('filterFechaDesde').value; // YYYY-MM-DD
    const fechaHasta = document.getElementById('filterFechaHasta').value; // YYYY-MM-DD
    const sortFecha = document.getElementById('sortFecha').value; // asc o desc

    let filtrados = listaGlobal.filter(p => {
        const matchesQuery = !q ||
            (p.nombre && p.nombre.toLowerCase().includes(q)) ||
            (p.apellidos && p.apellidos.toLowerCase().includes(q)) ||
            (p.dni && p.dni.includes(q)) ||
            (p.iglesia && p.iglesia.toLowerCase().includes(q)) ||
            (p.correo && p.correo.toLowerCase().includes(q));

        const matchesMod = !mod || p.modalidad === mod;
        const matchesPago = !pago || (p.estadoPago && p.estadoPago.includes(pago));
        const matchesReg = !reg || p.estadoRegistro === reg;

        // Filtro por Fechas
        let matchesFecha = true;
        if (p.fechaRegistro) {
            const fechaInfo = formatearFechaRegistro(p.fechaRegistro);
            const fechaRegistroIso = fechaInfo.iso || p.fechaRegistro.slice(0, 10);
            if (fechaDesde && fechaRegistroIso < fechaDesde) matchesFecha = false;
            if (fechaHasta && fechaRegistroIso > fechaHasta) matchesFecha = false;
        }

        return matchesQuery && matchesMod && matchesPago && matchesReg && matchesFecha;
    });

    // Ordenar por fecha
    filtrados.sort((a, b) => {
        const dateA = a.fechaRegistro ? new Date(a.fechaRegistro.replace(' ', 'T')).getTime() : 0;
        const dateB = b.fechaRegistro ? new Date(b.fechaRegistro.replace(' ', 'T')).getTime() : 0;
        return sortFecha === 'asc' ? dateA - dateB : dateB - dateA;
    });

    renderTabla(filtrados);
}

function renderTabla(lista) {
    const tablaBody = document.getElementById('tablaBody');
    const rowCount = document.getElementById('rowCount');
    rowCount.textContent = `${lista.length} participante(s) visualizados`;

    if (lista.length === 0) {
        tablaBody.innerHTML = `
            <tr>
                <td colspan="13" class="text-center py-12 text-slate-400">
                    <i class="fa-regular fa-folder-open text-3xl mb-2 text-slate-400"></i>
                    <p class="font-bold text-slate-600">No se encontraron registros con los filtros o fechas seleccionadas.</p>
                </td>
            </tr>
        `;
        return;
    }

    let html = '';
    lista.forEach((p) => {
        const fInfo = formatearFechaRegistro(p.fechaRegistro);

        // Estilos de badges alegres
        let estadoPagoBadge = 'bg-slate-100 text-slate-700 border-slate-200';
        if (p.estadoPago && p.estadoPago.includes('Completo')) {
            estadoPagoBadge = 'bg-emerald-100 text-emerald-800 border-emerald-300 font-extrabold';
        } else if (p.estadoPago && p.estadoPago.includes('Falta')) {
            estadoPagoBadge = 'bg-amber-100 text-amber-800 border-amber-300 font-extrabold';
        }

        let estadoRegColor = 'text-amber-600 bg-amber-50 border-amber-200';
        if (p.estadoRegistro === 'Validado') estadoRegColor = 'text-emerald-700 bg-emerald-50 border-emerald-200 font-bold';
        if (p.estadoRegistro === 'Rechazado') estadoRegColor = 'text-rose-700 bg-rose-50 border-rose-200 font-bold';

        // Comprobantes
        let vouchersHtml = '<span class="text-slate-400 text-[10px]">Sin voucher</span>';
        if (p.pagos && p.pagos.length > 0) {
            const conUrl = p.pagos.filter(pg => pg.comprobanteUrl);
            if (conUrl.length > 0) {
                vouchersHtml = conUrl.map(pg => `
                    <button onclick="verVoucher('${pg.comprobanteUrl}', 'Voucher Cuota ${pg.numeroCuota} - ${p.nombre} ${p.apellidos}')" 
                        class="text-[10px] bg-indigo-50 hover:bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-lg border border-indigo-200 font-bold inline-flex items-center gap-1 mb-1 mr-1 shadow-sm transition-colors">
                        <i class="fa-regular fa-image text-indigo-500"></i> C${pg.numeroCuota}
                    </button>
                `).join('');
            }
        }

        html += `
            <tr class="hover:bg-indigo-50/40 transition-colors">
                <td class="py-3.5 px-4 font-mono font-bold text-slate-500">#${p.id}</td>
                
                <!-- COLUMNA FECHA DE REGISTRO CLARA Y DETALLADA -->
                <td class="py-3.5 px-4">
                    <div class="flex items-center gap-1.5 text-slate-800 font-bold text-xs">
                        <i class="fa-regular fa-calendar text-indigo-500 text-[11px]"></i>
                        <span>${fInfo.fecha}</span>
                    </div>
                    <div class="text-[11px] text-slate-500 font-mono ml-4">
                        ${fInfo.hora || ''}
                    </div>
                </td>

                <td class="py-3.5 px-4">
                    <div class="font-extrabold text-slate-900">${p.nombre} ${p.apellidos}</div>
                    <div class="text-[11px] text-slate-500 font-medium">${p.correo || 'Sin correo'}</div>
                </td>
                <td class="py-3.5 px-4">
                    <div class="font-mono font-bold text-indigo-700">${p.dni}</div>
                    <div class="text-[11px] text-slate-500 font-medium">${p.telefono || '-'}</div>
                </td>
                <td class="py-3.5 px-4">
                    <div class="font-semibold text-slate-800">${p.iglesia || 'No especificada'}</div>
                    <div class="text-[11px] text-slate-500">${p.cargo || '-'}</div>
                </td>
                <td class="py-3.5 px-4">
                    <span class="inline-block bg-purple-100 text-purple-800 border border-purple-200 px-2.5 py-0.5 rounded-full text-[11px] font-bold">
                        ${p.modalidad}
                    </span>
                </td>
                <td class="py-3.5 px-4 font-bold text-slate-700">S/ ${(p.costoTotal || 0).toFixed(2)}</td>
                <td class="py-3.5 px-4 font-black text-emerald-600">S/ ${(p.totalPagado || 0).toFixed(2)}</td>
                <td class="py-3.5 px-4 font-black text-amber-500">S/ ${(p.deuda || 0).toFixed(2)}</td>
                
                <td class="py-3.5 px-4">
                    <span class="inline-block px-2.5 py-1 rounded-full text-[10px] border shadow-xs ${estadoPagoBadge}">
                        ${p.estadoPago || 'Pendiente'}
                    </span>
                </td>

                <td class="py-3.5 px-4">
                    <select onchange="cambiarEstadoRegistro(${p.id}, this.value)" 
                        class="rounded-xl px-2 py-1 text-[11px] border font-bold cursor-pointer outline-none shadow-xs ${estadoRegColor}">
                        <option value="Pendiente" ${p.estadoRegistro === 'Pendiente' ? 'selected' : ''}>Pendiente</option>
                        <option value="Validado" ${p.estadoRegistro === 'Validado' ? 'selected' : ''}>Validado</option>
                        <option value="Rechazado" ${p.estadoRegistro === 'Rechazado' ? 'selected' : ''}>Rechazado</option>
                    </select>
                </td>

                <td class="py-3.5 px-4">${vouchersHtml}</td>

                <td class="py-3.5 px-4 text-center">
                    <button onclick="abrirModalCuota(${p.id}, '${p.nombre} ${p.apellidos}', ${p.deuda || 0})"
                        class="bg-indigo-600 hover:bg-indigo-700 text-white font-bold px-3 py-1.5 rounded-xl text-[11px] transition-all shadow-sm flex items-center gap-1 mx-auto active:scale-95"
                        title="Registrar pago de cuota">
                        <i class="fa-solid fa-plus text-[10px]"></i> <span>Cuota</span>
                    </button>
                </td>
            </tr>
        `;
    });

    tablaBody.innerHTML = html;
}

function actualizarEstadisticas(lista) {
    document.getElementById('statInscritos').textContent = lista.length;

    let recaudado = 0;
    let deuda = 0;
    let d1 = 0;
    let d2 = 0;

    lista.forEach(p => {
        recaudado += parseFloat(p.totalPagado || 0);
        deuda += parseFloat(p.deuda || 0);
        if (p.asistenciaDia1) d1++;
        if (p.asistenciaDia2) d2++;
    });

    document.getElementById('statRecaudado').textContent = `S/ ${recaudado.toFixed(2)}`;
    document.getElementById('statDeuda').textContent = `S/ ${deuda.toFixed(2)}`;
    document.getElementById('statAsistencia').textContent = `${d1} / ${d2}`;
}

async function cambiarEstadoRegistro(id, nuevoEstado) {
    const token = localStorage.getItem('auth_token');
    try {
        const res = await fetch(`/api/admin/participantes/${id}/estado`, {
            method: 'PATCH',
            headers: {
                'Authorization': 'Bearer ' + token,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ estado: nuevoEstado })
        });

        const data = await res.json();
        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Error al actualizar estado');
        }

        const p = listaGlobal.find(item => item.id === id);
        if (p) p.estadoRegistro = nuevoEstado;

        showTableAlert(`✅ Estado de #${id} actualizado a '${nuevoEstado}' en SQL Server.`, false);
    } catch (err) {
        showTableAlert(err.message, true);
        cargarParticipantes();
    }
}

// Modal Cuotas
function abrirModalCuota(participanteId, nombreCompleto, deuda) {
    document.getElementById('cuotaParticipanteId').value = participanteId;
    document.getElementById('modalParticipanteNombre').textContent = `Maestro(a): ${nombreCompleto} (Deuda actual: S/ ${deuda.toFixed(2)})`;
    document.getElementById('cuotaMonto').value = deuda > 0 ? deuda.toFixed(2) : '';
    document.getElementById('cuotaForm').reset();
    document.getElementById('cuotaParticipanteId').value = participanteId;
    document.getElementById('cuotaModal').classList.remove('hidden');
}

function cerrarModalCuota() {
    document.getElementById('cuotaModal').classList.add('hidden');
}

async function guardarNuevaCuota(e) {
    e.preventDefault();
    const token = localStorage.getItem('auth_token');
    const pId = document.getElementById('cuotaParticipanteId').value;
    const monto = document.getElementById('cuotaMonto').value;
    const medio = document.getElementById('cuotaMedioPago').value;
    const op = document.getElementById('cuotaOp').value;
    const compInput = document.getElementById('cuotaComprobante');

    const formData = new FormData();
    formData.append('participanteId', pId);
    formData.append('monto', monto);
    formData.append('medioPago', medio);
    if (op) formData.append('codigoOperacion', op);
    if (compInput.files && compInput.files[0]) {
        formData.append('comprobante', compInput.files[0]);
    }

    const btn = document.getElementById('btnGuardarCuota');
    btn.disabled = true;
    btn.textContent = 'Guardando en SQL Server...';

    try {
        const res = await fetch('/api/admin/pagos/cuota', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + token },
            body: formData
        });

        const data = await res.json();
        if (!res.ok || !data.success) {
            throw new Error(data.message || 'Error al guardar cuota');
        }

        cerrarModalCuota();
        showTableAlert('✅ Nueva cuota guardada correctamente en SQL Server.', false);
        cargarParticipantes();

    } catch (err) {
        alert('Error: ' + err.message);
    } finally {
        btn.disabled = false;
        btn.textContent = 'Guardar Cuota en SQL Server';
    }
}

// Modal Voucher
function verVoucher(url, titulo) {
    document.getElementById('voucherModalTitle').textContent = titulo;
    const container = document.getElementById('voucherContainer');

    if (url.toLowerCase().endsWith('.pdf')) {
        container.innerHTML = `<iframe src="${url}" class="w-full h-96 rounded-2xl border border-slate-300"></iframe>`;
    } else {
        container.innerHTML = `<img src="${url}" alt="Voucher" class="max-h-[75vh] max-w-full rounded-2xl object-contain shadow-lg border border-slate-200">`;
    }

    document.getElementById('voucherModal').classList.remove('hidden');
}

function cerrarModalVoucher() {
    document.getElementById('voucherModal').classList.add('hidden');
    document.getElementById('voucherContainer').innerHTML = '';
}

// Exportar a Excel
function exportarExcel() {
    const token = localStorage.getItem('auth_token');
    fetch('/api/admin/participantes/exportar', {
        headers: { 'Authorization': 'Bearer ' + token }
    })
    .then(response => {
        if (!response.ok) throw new Error('Error al descargar Excel');
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Participantes_EBDN_SQLServer_${new Date().toISOString().slice(0, 10)}.xlsx`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    })
    .catch(err => alert(err.message));
}

function showTableAlert(msg, isError) {
    const box = document.getElementById('tableAlertBox');
    if (!msg) {
        box.classList.add('hidden');
        return;
    }
    box.textContent = msg;
    box.className = isError
        ? 'p-4 rounded-2xl text-xs sm:text-sm font-bold bg-rose-50 text-rose-700 border border-rose-200'
        : 'p-4 rounded-2xl text-xs sm:text-sm font-bold bg-emerald-50 text-emerald-700 border border-emerald-200';
    box.classList.remove('hidden');
    setTimeout(() => box.classList.add('hidden'), 4500);
}

function logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_user');
    localStorage.removeItem('auth_name');
    localStorage.removeItem('auth_role');
    window.location.href = 'login.html';
}
