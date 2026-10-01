document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registroForm');
    const comprobanteInput = document.getElementById('comprobante');
    const fileLabel = document.getElementById('fileLabel');
    const alertBox = document.getElementById('alertBox');
    const submitBtn = document.getElementById('submitBtn');
    const btnText = document.getElementById('btnText');
    const btnLoader = document.getElementById('btnLoader');
    const modalidadSelect = document.getElementById('modalidadSelect');
    const montoPagoInput = document.getElementById('montoPago');
    const medioPagoSelect = document.getElementById('medioPago');
    const paymentInstructionsBox = document.getElementById('paymentInstructionsBox');

    // Precios por modalidad
    const precios = {
        'Presencial': 25.00,
        'Virtual': 20.00,
        'Solo manual': 35.00,
        'Presencial + manual': 60.00,
        'Virtual + manual': 55.00
    };

    // Auto-completar monto según modalidad
    function actualizarMontoSugerido() {
        const sel = modalidadSelect.value;
        if (precios[sel] && (!montoPagoInput.value || parseFloat(montoPagoInput.value) === 0)) {
            montoPagoInput.value = precios[sel].toFixed(2);
        }
    }
    modalidadSelect.addEventListener('change', actualizarMontoSugerido);
    actualizarMontoSugerido();

    // ==============================================================
    // CONFIGURACIÓN DE MEDIOS DE PAGO (EDITABLE POR EL ADMINISTRADOR)
    // ==============================================================
    const CONFIG_PAGOS = {
        // Enlace referencial de PayPal (puedes cambiarlo aquí):
        paypalUrl: 'https://paypal.me/CapacitacionEBDN',
        
        // Datos para Yape y Plin:
        telefonoYapePlin: '987 654 321',
        titularYapePlin: 'Capacitación EBDN - Tesorería',
        // Imagen o QR referencial (puedes colocar la ruta a tu propia imagen en img/qr-yape.png):
        qrYapeUrl: 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=YAPE-987654321-CAPACITACION',
        qrPlinUrl: 'https://api.qrserver.com/v1/create-qr-code/?size=180x180&data=PLIN-987654321-CAPACITACION',

        // Cuentas bancarias:
        bcpCuenta: '191-98765432-0-99',
        bcpCci: '002-191-0098765432099-52',
        bbvaCuenta: '0011-0123-0200123456-18',
        bbvaCci: '011-123-000200123456-18',
        bancoTitular: 'Asociación Capacitación EBDN'
    };

    // Actualizar caja de instrucciones de pago según la opción seleccionada
    function renderInstruccionesPago() {
        const medio = medioPagoSelect.value;
        let html = '';

        if (medio === 'Yape') {
            html = `
                <div class="bg-purple-50 border border-purple-200 rounded-2xl p-4 sm:p-5 flex flex-col sm:flex-row items-center gap-5 payment-card">
                    <div class="bg-white p-2.5 rounded-2xl shadow-sm border border-purple-100 flex flex-col items-center">
                        <img src="${CONFIG_PAGOS.qrYapeUrl}" alt="QR Yape" class="w-32 h-32 rounded-xl object-contain">
                        <span class="text-[10px] font-bold text-purple-700 mt-1 uppercase">Escanear con Yape</span>
                    </div>
                    <div class="space-y-1.5 text-center sm:text-left flex-1">
                        <span class="inline-block bg-purple-600 text-white text-[10px] font-extrabold px-2.5 py-0.5 rounded-full uppercase">
                            Pago por Yape
                        </span>
                        <div class="text-sm font-extrabold text-slate-800">
                            Número Yape: <span class="text-purple-700 font-mono text-base">${CONFIG_PAGOS.telefonoYapePlin}</span>
                            <button type="button" onclick="copiarAlPortapapeles('${CONFIG_PAGOS.telefonoYapePlin}')" class="ml-1 text-xs text-purple-600 hover:text-purple-800 underline font-semibold cursor-pointer">
                                Copiar
                            </button>
                        </div>
                        <div class="text-xs text-slate-600">Titular: <b>${CONFIG_PAGOS.titularYapePlin}</b></div>
                        <p class="text-[11px] text-slate-500 pt-1">
                            💡 <b>Pasos:</b> Abre Yape, escanea el código o yapea al número, y luego adjunta la captura de tu voucher abajo.
                        </p>
                    </div>
                </div>
            `;
        } else if (medio === 'Plin') {
            html = `
                <div class="bg-cyan-50 border border-cyan-200 rounded-2xl p-4 sm:p-5 flex flex-col sm:flex-row items-center gap-5 payment-card">
                    <div class="bg-white p-2.5 rounded-2xl shadow-sm border border-cyan-100 flex flex-col items-center">
                        <img src="${CONFIG_PAGOS.qrPlinUrl}" alt="QR Plin" class="w-32 h-32 rounded-xl object-contain">
                        <span class="text-[10px] font-bold text-cyan-700 mt-1 uppercase">Escanear con Plin</span>
                    </div>
                    <div class="space-y-1.5 text-center sm:text-left flex-1">
                        <span class="inline-block bg-cyan-600 text-white text-[10px] font-extrabold px-2.5 py-0.5 rounded-full uppercase">
                            Pago por Plin
                        </span>
                        <div class="text-sm font-extrabold text-slate-800">
                            Número Plin: <span class="text-cyan-700 font-mono text-base">${CONFIG_PAGOS.telefonoYapePlin}</span>
                            <button type="button" onclick="copiarAlPortapapeles('${CONFIG_PAGOS.telefonoYapePlin}')" class="ml-1 text-xs text-cyan-600 hover:text-cyan-800 underline font-semibold cursor-pointer">
                                Copiar
                            </button>
                        </div>
                        <div class="text-xs text-slate-600">Titular: <b>${CONFIG_PAGOS.titularYapePlin}</b></div>
                        <p class="text-[11px] text-slate-500 pt-1">
                            💡 <b>Pasos:</b> Envía el monto desde tu banca móvil afiliada a Plin y sube la captura de confirmación abajo.
                        </p>
                    </div>
                </div>
            `;
        } else if (medio === 'PayPal') {
            html = `
                <div class="bg-blue-50 border border-blue-200 rounded-2xl p-5 payment-card space-y-3">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2">
                            <span class="w-8 h-8 bg-blue-600 text-white rounded-lg flex items-center justify-center font-bold text-sm">
                                <i class="fa-brands fa-paypal"></i>
                            </span>
                            <div>
                                <h4 class="text-sm font-extrabold text-slate-800">Pago Internacional / Tarjetas vía PayPal</h4>
                                <p class="text-xs text-slate-500">Acepta saldo PayPal o tarjetas de débito/crédito internacionales</p>
                            </div>
                        </div>
                        <span class="text-[10px] font-bold bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full uppercase">Online</span>
                    </div>

                    <div class="bg-white p-3.5 rounded-xl border border-blue-100 flex flex-col sm:flex-row justify-between items-center gap-3">
                        <div class="text-xs text-slate-700">
                            <span>Enlace oficial de pago:</span>
                            <div class="font-mono text-blue-600 font-bold text-xs break-all">${CONFIG_PAGOS.paypalUrl}</div>
                        </div>
                        <a href="${CONFIG_PAGOS.paypalUrl}" target="_blank" rel="noopener noreferrer"
                            class="bg-[#0070ba] hover:bg-[#003087] text-white font-bold text-xs px-4 py-2 rounded-xl transition-all shadow flex items-center gap-1.5 whitespace-nowrap">
                            <i class="fa-brands fa-paypal"></i>
                            <span>Abrir Enlace de PayPal ↗</span>
                        </a>
                    </div>
                    <p class="text-[11px] text-slate-500">
                        📌 <b>Nota:</b> Una vez completado el pago en PayPal, descarga o toma captura del recibo y adjúntalo en el campo de abajo.
                    </p>
                </div>
            `;
        } else if (medio.includes('BCP') || medio.includes('BBVA')) {
            const esBcp = medio.includes('BCP');
            const banco = esBcp ? 'BCP' : 'BBVA';
            const cuenta = esBcp ? CONFIG_PAGOS.bcpCuenta : CONFIG_PAGOS.bbvaCuenta;
            const cci = esBcp ? CONFIG_PAGOS.bcpCci : CONFIG_PAGOS.bbvaCci;

            html = `
                <div class="bg-amber-50 border border-amber-200 rounded-2xl p-4 sm:p-5 payment-card space-y-2">
                    <span class="inline-block bg-amber-600 text-white text-[10px] font-extrabold px-2.5 py-0.5 rounded-full uppercase">
                        Transferencia ${banco}
                    </span>
                    <div class="grid grid-cols-1 sm:grid-cols-2 gap-2 text-xs pt-1">
                        <div class="bg-white p-2.5 rounded-xl border border-amber-100">
                            <span class="text-slate-400 block text-[10px]">Cuenta Corriente ${banco} Soles:</span>
                            <span class="font-mono font-bold text-slate-800 text-xs">${cuenta}</span>
                            <button type="button" onclick="copiarAlPortapapeles('${cuenta}')" class="ml-2 text-amber-700 font-semibold underline text-[10px]">Copiar</button>
                        </div>
                        <div class="bg-white p-2.5 rounded-xl border border-amber-100">
                            <span class="text-slate-400 block text-[10px]">Código Interbancario (CCI):</span>
                            <span class="font-mono font-bold text-slate-800 text-xs">${cci}</span>
                            <button type="button" onclick="copiarAlPortapapeles('${cci}')" class="ml-2 text-amber-700 font-semibold underline text-[10px]">Copiar</button>
                        </div>
                    </div>
                    <div class="text-xs text-slate-600">Titular de Cuenta: <b>${CONFIG_PAGOS.bancoTitular}</b></div>
                </div>
            `;
        } else if (medio === 'Efectivo') {
            html = `
                <div class="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 payment-card flex items-center gap-3">
                    <div class="w-10 h-10 bg-emerald-100 text-emerald-600 rounded-xl flex items-center justify-center text-lg">
                        <i class="fa-solid fa-money-bill-wave"></i>
                    </div>
                    <div class="text-xs text-slate-700">
                        <div class="font-bold text-emerald-800">Pago en Efectivo</div>
                        <p class="text-[11px] text-slate-500">Puedes coordinar tu pago directamente con el encargado de tu congregación o abonarlo en la mesa de registro el primer día del evento.</p>
                    </div>
                </div>
            `;
        }

        paymentInstructionsBox.innerHTML = html;
    }

    medioPagoSelect.addEventListener('change', renderInstruccionesPago);
    renderInstruccionesPago();

    // Cambiar texto al seleccionar archivo
    comprobanteInput.addEventListener('change', (e) => {
        if (e.target.files && e.target.files[0]) {
            fileLabel.textContent = `Archivo seleccionado: ${e.target.files[0].name}`;
            fileLabel.classList.add('text-indigo-600');
        } else {
            fileLabel.textContent = 'Haz clic aquí o arrastra tu comprobante de pago';
            fileLabel.classList.remove('text-indigo-600');
        }
    });

    // Enviar Formulario
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        showAlert('', false);
        setLoading(true);

        const formData = new FormData(form);

        try {
            const res = await fetch('/api/public/registro', {
                method: 'POST',
                body: formData
            });

            const data = await res.json();

            if (!res.ok || !data.success) {
                throw new Error(data.message || 'Error al procesar la inscripción');
            }

            // Mostrar modal de éxito alegre
            const p = data.data;
            const details = `
                <div class="flex justify-between border-b border-slate-200 pb-1.5">
                    <span class="text-slate-500">Maestro(a):</span>
                    <span class="text-slate-800 font-bold">${p.nombreCompleto}</span>
                </div>
                <div class="flex justify-between border-b border-slate-200 pb-1.5">
                    <span class="text-slate-500">DNI:</span>
                    <span class="text-slate-800 font-bold font-mono">${p.dni}</span>
                </div>
                <div class="flex justify-between border-b border-slate-200 pb-1.5">
                    <span class="text-slate-500">Costo Total:</span>
                    <span class="text-indigo-600 font-extrabold">S/ ${parseFloat(p.costoTotal).toFixed(2)}</span>
                </div>
                <div class="flex justify-between border-b border-slate-200 pb-1.5">
                    <span class="text-slate-500">Monto Abonado:</span>
                    <span class="text-emerald-600 font-extrabold">S/ ${parseFloat(p.totalPagado).toFixed(2)}</span>
                </div>
                <div class="flex justify-between border-b border-slate-200 pb-1.5">
                    <span class="text-slate-500">Saldo Pendiente:</span>
                    <span class="text-amber-600 font-extrabold">S/ ${parseFloat(p.deuda).toFixed(2)}</span>
                </div>
                <div class="flex justify-between">
                    <span class="text-slate-500">Estado de Pago:</span>
                    <span class="inline-block px-2 py-0.5 rounded-full font-bold bg-green-100 text-green-700 text-[10px]">${p.estadoPago}</span>
                </div>
            `;
            document.getElementById('successDetails').innerHTML = details;
            document.getElementById('successModal').classList.remove('hidden');

            form.reset();
            fileLabel.textContent = 'Haz clic aquí o arrastra tu comprobante de pago';
            fileLabel.classList.remove('text-indigo-600');
            actualizarMontoSugerido();
            renderInstruccionesPago();

        } catch (err) {
            showAlert(err.message, true);
        } finally {
            setLoading(false);
        }
    });

    function showAlert(msg, isError) {
        if (!msg) {
            alertBox.classList.add('hidden');
            return;
        }
        alertBox.textContent = msg;
        alertBox.className = isError
            ? 'mb-6 p-4 rounded-2xl text-xs sm:text-sm font-bold bg-rose-50 text-rose-700 border border-rose-200'
            : 'mb-6 p-4 rounded-2xl text-xs sm:text-sm font-bold bg-emerald-50 text-emerald-700 border border-emerald-200';
        alertBox.classList.remove('hidden');
    }

    function setLoading(isLoading) {
        submitBtn.disabled = isLoading;
        if (isLoading) {
            btnText.textContent = 'Guardando tus datos en el sistema...';
            btnLoader.classList.remove('hidden');
        } else {
            btnText.textContent = 'Enviar y Confirmar Mi Inscripción';
            btnLoader.classList.add('hidden');
        }
    }
});

// Función global para copiar datos (números, cuentas)
function copiarAlPortapapeles(texto) {
    navigator.clipboard.writeText(texto).then(() => {
        alert('Copiado al portapapeles: ' + texto);
    }).catch(() => {
        prompt('Copia este dato:', texto);
    });
}

function cerrarModalExito() {
    document.getElementById('successModal').classList.add('hidden');
}
