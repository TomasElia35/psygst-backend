-- ============================================================
-- PsyGst v1.0 — Seed Data
-- V2__seed.sql
-- ============================================================

-- Roles
INSERT INTO T_Rol (Nombre) VALUES ('ADMIN');       -- IdRol = 1
INSERT INTO T_Rol (Nombre) VALUES ('PROFESIONAL'); -- IdRol = 2

-- Profesiones
INSERT INTO T_Profesion (Nombre) VALUES ('Psicología');           -- IdProfesion = 1
INSERT INTO T_Profesion (Nombre) VALUES ('Psiquiatría');          -- IdProfesion = 2
INSERT INTO T_Profesion (Nombre) VALUES ('Trabajo Social');       -- IdProfesion = 3

-- Obras sociales
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('Particular', NULL);        -- IdObraSocial = 1 (default)
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('OSDE', 'liquidaciones@osde.com.ar');
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('Swiss Medical', 'liquidaciones@swissmedical.com.ar');
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('Galeno', 'liquidaciones@galeno.com.ar');
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('IOMA', 'liquidaciones@ioma.gob.ar');
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('PAMI', 'liquidaciones@pami.org.ar');
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('OSECAC', null);
INSERT INTO T_ObraSocial (Nombre, ContactoLiquidacion) VALUES ('Medicus', null);

-- Motivos de baja
INSERT INTO T_Motivo (Descripcion) VALUES ('Alta terapéutica');     -- IdMotivo = 1
INSERT INTO T_Motivo (Descripcion) VALUES ('Abandono');             -- IdMotivo = 2
INSERT INTO T_Motivo (Descripcion) VALUES ('Derivación');           -- IdMotivo = 3
INSERT INTO T_Motivo (Descripcion) VALUES ('Razones económicas');   -- IdMotivo = 4
INSERT INTO T_Motivo (Descripcion) VALUES ('Cambio de profesional'); -- IdMotivo = 5
INSERT INTO T_Motivo (Descripcion) VALUES ('Otro');                 -- IdMotivo = 6

-- Sistema demo (tenant)
INSERT INTO T_Sistema (Nombre, Activo) VALUES ('Consultorio Demo', 1);  -- IdSistema = 1

-- Profesional demo
INSERT INTO T_Profesional (UUID, Nombre, Apellido, CUIT, NroLicencia, Email, Celular, IdProfesion, IdSistema)
VALUES (
    NEWID(),
    'María',
    'García',
    '27-32456789-1',
    'MN 12345',
    'demo@psygst.com',
    '1122334455',
    1,  -- Psicología
    1   -- Sistema Demo
);
-- IdProfesional = 1

-- T_Auth — demo user (password: demo1234 hashed with bcrypt factor 10)
-- Hash generated offline: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh8a
INSERT INTO T_Auth (Username, Password, IdRol, IdProfesional, Activo)
VALUES (
    'demo',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh8a',
    2,  -- PROFESIONAL
    1,  -- Profesional demo
    1   -- Activo
);

-- Aranceles demo
INSERT INTO T_Arancel (IdProfesional, IdObraSocial, Modalidad, Precio, IdSistema)
VALUES (1, 1, 'PRESENCIAL', 15000.00, 1);   -- Particular presencial

INSERT INTO T_Arancel (IdProfesional, IdObraSocial, Modalidad, Precio, IdSistema)
VALUES (1, 1, 'VIRTUAL', 12000.00, 1);   -- Particular virtual

INSERT INTO T_Arancel (IdProfesional, IdObraSocial, Modalidad, Precio, IdSistema)
VALUES (1, 2, 'PRESENCIAL', 8500.00, 1);   -- OSDE presencial

INSERT INTO T_Arancel (IdProfesional, IdObraSocial, Modalidad, Precio, IdSistema)
VALUES (1, 2, 'VIRTUAL', 7000.00, 1);   -- OSDE virtual

-- Sample paciente (for testing)
INSERT INTO T_Paciente (UUID, Nombre, Apellido, DNI, Email, Celular, IdObraSocial, IdProfesional, IdSistema)
VALUES (
    NEWID(),
    'Juan',
    'Pérez',
    '30123456',
    'juan.perez@email.com',
    '1155667788',
    1,   -- Particular
    1,
    1
);
