-- ==============================================================
-- SCRIPT DE BASE DE DATOS PARA CAPACITACIÓN EBDN
-- MOTOR: MICROSOFT SQL SERVER 2019 / 2022
-- ==============================================================

-- 1. Crear Base de Datos
IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'CapacitacionDB')
BEGIN
    CREATE DATABASE CapacitacionDB;
    PRINT 'Base de datos CapacitacionDB creada correctamente.';
END
GO

USE CapacitacionDB;
GO

-- 2. Tabla de Usuarios Administradores (para Login seguro)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'usuarios')
BEGIN
    CREATE TABLE usuarios (
        id INT IDENTITY(1,1) PRIMARY KEY,
        username VARCHAR(50) NOT NULL UNIQUE,
        password VARCHAR(255) NOT NULL, -- Hash BCrypt
        nombre_completo VARCHAR(150) NOT NULL,
        correo VARCHAR(120),
        rol VARCHAR(50) DEFAULT 'ROLE_ADMIN',
        activo BIT DEFAULT 1,
        fecha_creacion DATETIME DEFAULT GETDATE()
    );
    PRINT 'Tabla usuarios creada.';
END
GO

-- 3. Tabla de Participantes (Hoja de Datos principal de inscripciones)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'participantes')
BEGIN
    CREATE TABLE participantes (
        id INT IDENTITY(1,1) PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL,
        apellidos VARCHAR(100) NOT NULL,
        dni VARCHAR(20) NOT NULL UNIQUE,
        correo VARCHAR(120),
        telefono VARCHAR(30),
        iglesia VARCHAR(150),
        cargo VARCHAR(100),
        modalidad VARCHAR(50) NOT NULL, -- Presencial, Virtual, Solo manual, etc.
        costo_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        total_pagado DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        deuda DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        estado_pago VARCHAR(50) DEFAULT 'Pendiente', -- 'Pago Completo', 'Falta Pagar S/ X', 'Pendiente'
        estado_registro VARCHAR(50) DEFAULT 'Pendiente', -- 'Pendiente', 'Validado', 'Rechazado'
        asistencia_dia1 BIT DEFAULT 0,
        asistencia_dia2 BIT DEFAULT 0,
        ticket_qr VARCHAR(255),
        observaciones VARCHAR(500),
        fecha_registro DATETIME DEFAULT GETDATE()
    );
    CREATE INDEX IX_Participantes_DNI ON participantes(dni);
    PRINT 'Tabla participantes creada.';
END
GO

-- 4. Tabla de Pagos y Cuotas
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'pagos')
BEGIN
    CREATE TABLE pagos (
        id INT IDENTITY(1,1) PRIMARY KEY,
        participante_id INT NOT NULL,
        numero_cuota INT NOT NULL DEFAULT 1,
        medio_pago VARCHAR(50), -- Yape, Plin, Transferencia, PayPal, Efectivo
        monto DECIMAL(10,2) NOT NULL DEFAULT 0.00,
        codigo_operacion VARCHAR(100),
        comprobante_url VARCHAR(500),
        estado_validacion VARCHAR(50) DEFAULT 'Pendiente', -- 'Pendiente', 'Validado', 'Rechazado'
        fecha_pago DATETIME DEFAULT GETDATE(),
        validado_por VARCHAR(100),
        fecha_validacion DATETIME,
        CONSTRAINT FK_Pagos_Participantes FOREIGN KEY (participante_id) 
            REFERENCES participantes(id) ON DELETE CASCADE
    );
    CREATE INDEX IX_Pagos_Operacion ON pagos(codigo_operacion);
    PRINT 'Tabla pagos creada.';
END
GO

-- 5. Usuario por defecto para el sistema:
-- Usuario: admin
-- Contraseña: admin123 (BCrypt hash)
IF NOT EXISTS (SELECT * FROM usuarios WHERE username = 'admin')
BEGIN
    INSERT INTO usuarios (username, password, nombre_completo, correo, rol, activo)
    VALUES ('admin', '$2a$10$3J1Z.E2m9Hk8Q7W.p3mBvOxT4fV3L8g1O8m2y9Bv5k3r7k5L1aI8k', 'Administrador Principal', 'admin@capacitacion.pe', 'ROLE_ADMIN', 1);
    PRINT 'Usuario admin creado (admin / admin123).';
END
GO

SELECT 'Tablas configuradas exitosamente en CapacitacionDB' AS Mensaje;
GO
