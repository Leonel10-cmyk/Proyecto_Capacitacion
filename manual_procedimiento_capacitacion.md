# Manual de Procedimiento y Distribución del Personal
## Capacitación Escuela Bíblica de Niños

Este documento detalla las funciones específicas, la cantidad de personas requeridas y los flujos de trabajo recomendados para el día del evento, asegurando un ingreso rápido, ordenado y sin contratiempos.

---

## 1. Distribución del Personal (Total Recomendado: 6 personas)

Para evitar congestiones en la entrada, se divide al equipo en tres áreas independientes:

### A. Control de Acceso Neto (2 Personas - Scanners)
*   **Ubicación:** Puerta de ingreso principal.
*   **Función:** Única y exclusivamente escanear códigos QR.
*   **Equipo:** Un teléfono celular con buena cámara y conexión a internet (o laptop con webcam) cada uno.
*   **Instrucciones:**
    1. Escanear el código QR del participante.
    2. Si la pantalla se pone **VERDE** y dice "Acceso Permitido", el participante avanza a la Mesa de Materiales.
    3. Si la pantalla se pone **ROJA** (código ya usado) o **AMARILLA** (deuda pendiente, error de registro, o no tiene QR), el scanner **NO** resuelve el problema. Le indica amablemente al participante: *"Por favor, acérquese a la Mesa de Incidencias a su derecha para que revisen su caso"*. Esto evita que la fila de ingreso se detenga.

### B. Mesa de Incidencias y Nuevos Registros (2 Personas - Registradores)
*   **Ubicación:** Una mesa separada de la fila principal, claramente señalizada.
*   **Función:** Resolver errores de lectura, buscar registros manuales y registrar a personas nuevas.
*   **Equipo:** 2 Laptops conectadas a internet con el panel de administración abierto.
*   **Instrucciones:**
    *   **Caso "Olvidé mi QR":** Buscar al participante por su DNI en el panel de asistencia. Si figura registrado y pagado, marcar su asistencia manualmente en el sistema y enviarlo a Materiales.
    *   **Caso "Tengo Deuda Pendiente":** Recibir el pago en efectivo o Yape de la cuota faltante, actualizar el pago en el panel `/admin-pagos.html` a "Pagado" y permitirle el ingreso.
    *   **Caso "Inscripción Nueva (Día del Evento)":** Llenar el formulario de registro de la web con los datos de la persona, cobrar el monto respectivo y registrar el pago en efectivo seleccionando al encargado de mesa como receptor.

### C. Entrega de Materiales (1 a 2 Personas)
*   **Ubicación:** Justo después del control de ingreso.
*   **Función:** Entregar manuales impresos y credenciales.
*   **Instrucciones:**
    *   Recibir a los participantes validados por los Scanners.
    *   Verificar que en su ticket QR figure la modalidad "Con Manual" antes de entregarlo para evitar pérdidas de inventario.

---

## 2. Respuestas a Procedimientos Críticos

### ¿Cómo procesar y validar pagos de PayPal asociados a un tercero?
Dado que los fondos irán a la cuenta de PayPal de otra persona (un tercero), la validación debe realizarse de forma coordinada siguiendo estos pasos:

1.  **Registro:** El participante extranjero realiza el pago en el link de PayPal y registra su **ID de Transacción** en la web. El sistema guarda este código con el prefijo `PAYPAL-` en la base de datos de Google Sheets.
2.  **Reporte del Tercero:** Solicita al dueño de la cuenta PayPal un reporte de transacciones de los últimos días (o pídele que te avise mediante WhatsApp cuando ingrese un pago con el nombre del participante).
3.  **Verificación Manual:**
    *   En el panel `/admin-pagos.html`, busca al participante.
    *   Compara el ID de transacción registrado (`PAYPAL-xxxxxxxxx`) con el que aparece en la cuenta de PayPal real del tercero.
    *   Si los montos y códigos coinciden, haz clic en **"Validar"** en el panel administrativo para registrar el ingreso y enviar automáticamente el ticket QR al correo del participante.

### ¿Qué hacer si un participante ingresó mal su correo electrónico?
Si un participante escribe mal su correo, el mensaje con su ticket no le llegará. Para solucionarlo rápidamente:

1.  **¿Dónde están guardados los tickets?**
    *   Todos los PDFs de los tickets se guardan de forma segura en la carpeta dedicada de **Google Drive**.
    *   El enlace directo a cada archivo PDF se almacena automáticamente en tu archivo de **Google Sheets** (columna de Tickets).
2.  **Procedimiento de reenvío:**
    *   Abre tu Google Sheets de control.
    *   Busca al participante por su DNI o Nombre.
    *   Verás que su correo tiene un error ortográfico (ej. `juan@gamil.com` en vez de `gmail.com`).
    *   **Paso A:** Corrige el correo directamente en la celda de Google Sheets para tener tus datos limpios.
    *   **Paso B:** Haz clic en el enlace del Ticket (columna de la derecha) que te llevará al PDF en Google Drive. Copia ese enlace de Drive y envíaselo directamente por WhatsApp al participante para que pueda ingresar.

---

## 3. Lista de Verificación (Checklist) para el Organizador

### 1 Semana Antes:
*   [ ] Compartir el instructivo de registro resumido en los grupos de WhatsApp.
*   [ ] Verificar que el dueño de la cuenta de PayPal esté listo para confirmar los IDs de transacción recibidos.
*   [ ] Realizar pruebas de registro local para verificar que los correos automáticos estén llegando correctamente.

### El Día del Evento (1.5 horas antes):
*   [ ] Configurar la mesa de entrada: Laptops listas para la Mesa de Incidencias, celulares con batería al 100% para los Scanners.
*   [ ] Probar la conexión a internet en la zona de ingreso.
*   [ ] Colocar la señalización clara: "FILA DE INGRESO (QR)" y "MESA DE INCIDENCIAS / NUEVOS REGISTROS".
*   [ ] Contar el inventario inicial de manuales físicos antes de iniciar las entregas.
