# Guía de Configuración

## 1. Configuración de Google Cloud (API de Sheets y Drive)

Para que la aplicación pueda leer y escribir en Google Sheets, y subir las imágenes a Google Drive, necesitas crear una cuenta de servicio de Google Cloud y obtener el archivo `credentials.json`.

### Pasos:
1. Ve a la consola de Google Cloud: https://console.cloud.google.com/
2. Crea un nuevo proyecto (ej. "Sistema de Registro").
3. Ve a "APIs y Servicios" > "Biblioteca". Busca y **habilita** estas dos APIs:
   - **Google Sheets API**
   - **Google Drive API**
4. Ve a "APIs y Servicios" > "Credenciales".
5. Haz clic en "Crear credenciales" y selecciona "Cuenta de servicio".
6. Llena los datos (ej. "bot-registro") y haz clic en "Crear y continuar" y luego en "Listo".
7. En la lista de Cuentas de servicio, haz clic en la que acabas de crear (el correo que termina en `.iam.gserviceaccount.com`).
   - **Copia ese correo, lo necesitarás más adelante.**
8. Ve a la pestaña "Claves" -> "Agregar clave" -> "Crear clave nueva" -> Formato JSON.
9. Se descargará un archivo. Renómbralo a `credentials.json` y colócalo en la raíz de este proyecto.

### 2. Configurar el Google Sheets
1. Crea un nuevo Google Sheets.
2. Nómbralo (ej. "Registro Capacitación").
3. En la primera fila (fila 1), pon los siguientes encabezados exactos:
   - `ID`
   - `Nombre`
   - `Apellidos`
   - `DNI`
   - `Codigo_Pago`
   - `Correo`
   - `Iglesia`
   - `Comprobante`
   - `Estado`
   - `Asistencia`
4. Haz clic en el botón "Compartir" arriba a la derecha.
5. Pega el **correo de la cuenta de servicio** (el que copiaste en el paso 7 de la sección anterior) y dale permisos de **Editor**.
6. Copia el **ID de la hoja de cálculo** de la URL. (Ej. en `https://docs.google.com/spreadsheets/d/1X2Y3Z.../edit`, el ID es `1X2Y3Z...`).

### 3. Configurar la carpeta de Google Drive
1. Crea una carpeta en tu Google Drive donde se guardarán las imágenes de los vouchers.
2. Haz clic derecho en la carpeta -> "Compartir" -> Pega el **correo de la cuenta de servicio** y dale permisos de **Editor**.
3. Cambia el acceso general a "Cualquier usuario que tenga el vínculo" (Lector) para que las imágenes se puedan ver.
4. Copia el **ID de la carpeta** de la URL. (Ej. en `https://drive.google.com/drive/folders/1A2B3C...`, el ID es `1A2B3C...`).

---

## 4. Archivo .env
Crea un archivo llamado `.env` en la raíz del proyecto y copia las variables de entorno. Puedes basarte en el archivo `.env.example` proporcionado.

---

## 5. Formato del Archivo Yape (Pruebas)
Para el panel de administración, cuando subas el CSV o Excel de Yape, el sistema buscará una columna llamada **"Código de Pago"**, "Código", o "Numero de Operacion". 
Asegúrate de que en el archivo CSV de prueba que descargues, exista una columna que contenga el código que el usuario ingresa en el formulario. Si el nombre de la columna varía, lo podemos ajustar en el archivo `backend/routes/validate.js`.
