
-- Script generado automáticamente para base de datos de empresa turística
-- Fecha: 2025-04-04 14:25:20

CREATE DATABASE IF NOT EXISTS agencia_turistica;
USE agencia_turistica;

-- Enums como tablas o tipos
CREATE TABLE Rol (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) UNIQUE
);

CREATE TABLE EstadoReserva (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) UNIQUE
);

CREATE TABLE FormaPago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) UNIQUE
);

CREATE TABLE TipoReporte (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE
);

-- Tabla Usuario
CREATE TABLE Usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    identificacion VARCHAR(50) UNIQUE,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    rol_id INT,
    codigo_verificacion VARCHAR(100),
    expiracion_codigo_verificacion DATETIME,
    codigo_recuperacion_password VARCHAR(100),
    expiracion_codigo_recuperacion DATETIME,
    cuenta_verificada BOOLEAN,
    fecha_registro DATE,
    FOREIGN KEY (rol_id) REFERENCES Rol(id)
);

-- Empleado y Administrador
CREATE TABLE Empleado (
    id INT PRIMARY KEY,
    fecha_contratacion DATE,
    FOREIGN KEY (id) REFERENCES Usuario(id)
);

CREATE TABLE Administrador (
    id INT PRIMARY KEY,
    fecha_contratacion DATE,
    FOREIGN KEY (id) REFERENCES Usuario(id)
);

-- Cliente
CREATE TABLE Cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    apellido VARCHAR(100),
    identificacion VARCHAR(50) UNIQUE,
    correo VARCHAR(100),
    telefono VARCHAR(20),
    fecha_nacimiento DATE
);

-- Actividad
CREATE TABLE Actividad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT,
    ubicacion VARCHAR(100),
    precio DECIMAL(10, 2),
    duracion INT,
    cupo_maximo INT,
    cupos_disponibles INT,
    fecha_inicio DATETIME,
    fecha_fin DATETIME
);

-- Hospedaje y habitaciones
CREATE TABLE Hospedaje (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    direccion VARCHAR(150),
    ciudad VARCHAR(100),
    telefono VARCHAR(20),
    estrellas INT,
    descripcion TEXT
);

CREATE TABLE TipoHabitacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT,
    precio DECIMAL(10, 2)
);

CREATE TABLE Habitacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_habitacion_id INT,
    hospedaje_id INT,
    capacidad INT,
    precio_por_noche DECIMAL(10, 2),
    disponible BOOLEAN,
    FOREIGN KEY (tipo_habitacion_id) REFERENCES TipoHabitacion(id),
    FOREIGN KEY (hospedaje_id) REFERENCES Hospedaje(id)
);

-- Características por tipo de habitación
CREATE TABLE CaracteristicaHabitacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_habitacion_id INT,
    caracteristica VARCHAR(100),
    FOREIGN KEY (tipo_habitacion_id) REFERENCES TipoHabitacion(id)
);

-- Paquete turístico
CREATE TABLE PaqueteTuristico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    descripcion TEXT,
    precio_base DECIMAL(10,2),
    duracion_dias INT,
    fecha_inicio DATE,
    fecha_fin DATE,
    cupo_maximo INT,
    cupos_disponibles INT
);

-- Relaciones entre actividades, hospedajes y paquetes
CREATE TABLE Paquete_Actividad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paquete_id INT,
    actividad_id INT,
    FOREIGN KEY (paquete_id) REFERENCES PaqueteTuristico(id),
    FOREIGN KEY (actividad_id) REFERENCES Actividad(id)
);

CREATE TABLE Paquete_Hospedaje (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paquete_id INT,
    hospedaje_id INT,
    FOREIGN KEY (paquete_id) REFERENCES PaqueteTuristico(id),
    FOREIGN KEY (hospedaje_id) REFERENCES Hospedaje(id)
);

-- Reserva
CREATE TABLE Reserva (
    id VARCHAR(50) PRIMARY KEY,
    cliente_id INT,
    empleado_id INT,
    paquete_id INT,
    fecha_inicio DATE,
    fecha_fin DATE,
    precio_total DECIMAL(10,2),
    estado_id INT,
    forma_pago_id INT,
    FOREIGN KEY (cliente_id) REFERENCES Cliente(id),
    FOREIGN KEY (empleado_id) REFERENCES Empleado(id),
    FOREIGN KEY (paquete_id) REFERENCES PaqueteTuristico(id),
    FOREIGN KEY (estado_id) REFERENCES EstadoReserva(id),
    FOREIGN KEY (forma_pago_id) REFERENCES FormaPago(id)
);

CREATE TABLE Reserva_Habitacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reserva_id VARCHAR(50),
    habitacion_id INT,
    fecha_inicio DATE,
    fecha_fin DATE,
    FOREIGN KEY (reserva_id) REFERENCES Reserva(id),
    FOREIGN KEY (habitacion_id) REFERENCES Habitacion(id)
);

-- Reporte
CREATE TABLE Reporte (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(100),
    descripcion TEXT,
    fecha_generacion DATETIME,
    tipo_reporte_id INT,
    contenido TEXT,
    FOREIGN KEY (tipo_reporte_id) REFERENCES TipoReporte(id)
);
