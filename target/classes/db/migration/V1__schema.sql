-- ============================================================
-- PsyGst v1.0 — Schema Migration
-- V1__schema.sql
-- All 14 tables with UUID, IdSistema multi-tenant, Baja lógica
-- ============================================================

-- 1. T_Sistema (tenants)
CREATE TABLE T_Sistema (
    IdSistema       INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    Nombre          NVARCHAR(100) NOT NULL,
    Activo          BIT NOT NULL DEFAULT 1,
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE()
);

-- 2. T_Rol
CREATE TABLE T_Rol (
    IdRol   INT IDENTITY(1,1) PRIMARY KEY,
    Nombre  NVARCHAR(50) NOT NULL UNIQUE,
    Baja    BIT NOT NULL DEFAULT 0
);

-- 3. T_Profesion
CREATE TABLE T_Profesion (
    IdProfesion INT IDENTITY(1,1) PRIMARY KEY,
    Nombre      NVARCHAR(100) NOT NULL,
    Baja        BIT NOT NULL DEFAULT 0
);

-- 4. T_ObraSocial
CREATE TABLE T_ObraSocial (
    IdObraSocial            INT IDENTITY(1,1) PRIMARY KEY,
    Nombre                  NVARCHAR(100) NOT NULL,
    ContactoLiquidacion     NVARCHAR(200),
    Baja                    BIT NOT NULL DEFAULT 0
);

-- 5. T_Motivo (baja reasons)
CREATE TABLE T_Motivo (
    IdMotivo    INT IDENTITY(1,1) PRIMARY KEY,
    Descripcion NVARCHAR(100) NOT NULL,
    Baja        BIT NOT NULL DEFAULT 0
);

-- 6. T_Profesional
CREATE TABLE T_Profesional (
    IdProfesional       INT IDENTITY(1,1) PRIMARY KEY,
    UUID                NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    Nombre              NVARCHAR(100) NOT NULL,
    Apellido            NVARCHAR(100) NOT NULL,
    CUIT                NVARCHAR(20),
    NroLicencia         NVARCHAR(50),
    Email               NVARCHAR(100),
    Celular             NVARCHAR(20),
    CBU                 NVARCHAR(100),
    Alias               NVARCHAR(50),
    IdProfesion         INT REFERENCES T_Profesion(IdProfesion),
    IdSistema           INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja                BIT NOT NULL DEFAULT 0,
    FechaCreacion       DATETIME2 DEFAULT GETDATE(),
    FechaModificacion   DATETIME2 DEFAULT GETDATE()
);

-- 7. T_Auth
CREATE TABLE T_Auth (
    IdAuth          INT IDENTITY(1,1) PRIMARY KEY,
    Username        NVARCHAR(100) NOT NULL UNIQUE,
    Password        NVARCHAR(255) NOT NULL,  -- bcrypt hash (RN-S02)
    IdRol           INT NOT NULL REFERENCES T_Rol(IdRol),
    IdProfesional   INT REFERENCES T_Profesional(IdProfesional),
    Activo          BIT NOT NULL DEFAULT 1,
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE(),
    UltimoAcceso    DATETIME2
);

-- 8. T_Arancel
CREATE TABLE T_Arancel (
    IdArancel       INT IDENTITY(1,1) PRIMARY KEY,
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdObraSocial    INT NOT NULL REFERENCES T_ObraSocial(IdObraSocial),
    Modalidad       NVARCHAR(20) NOT NULL CHECK (Modalidad IN ('PRESENCIAL', 'VIRTUAL')),
    Precio          DECIMAL(10,2) NOT NULL,
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE(),
    FechaModificacion DATETIME2 DEFAULT GETDATE()
);

-- 9. T_Paciente
CREATE TABLE T_Paciente (
    IdPaciente      INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    Nombre          NVARCHAR(100) NOT NULL,
    Apellido        NVARCHAR(100) NOT NULL,
    DNI             NVARCHAR(20) NOT NULL,
    Email           NVARCHAR(100),
    Celular         NVARCHAR(20),
    IdObraSocial    INT NOT NULL REFERENCES T_ObraSocial(IdObraSocial) DEFAULT 1,
    NroAfiliado     NVARCHAR(100),
    Observaciones   NVARCHAR(500),
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,
    IdMotivo        INT REFERENCES T_Motivo(IdMotivo),
    FechaBaja       DATETIME2,
    FechaCreacion   DATETIME2 DEFAULT GETDATE(),
    FechaModificacion DATETIME2 DEFAULT GETDATE()
);

-- RN-P01: DNI unique per active paciente per tenant
CREATE UNIQUE INDEX UQ_Paciente_DNI_Sistema
    ON T_Paciente(DNI, IdSistema)
    WHERE Baja = 0;

-- 10. T_Turno
CREATE TABLE T_Turno (
    IdTurno         INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    IdPaciente      INT NOT NULL REFERENCES T_Paciente(IdPaciente),
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    Fecha           DATE NOT NULL,
    HoraComienzo    TIME NOT NULL,
    HoraFin         TIME NOT NULL,
    Modalidad       NVARCHAR(20) NOT NULL CHECK (Modalidad IN ('PRESENCIAL', 'VIRTUAL')),
    Estado          NVARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO'
                    CHECK (Estado IN ('CONFIRMADO', 'REALIZADO', 'CANCELADO')),
    PrecioFinal     DECIMAL(10,2) NOT NULL,  -- RN-T06: frozen at creation
    IdObraSocial    INT REFERENCES T_ObraSocial(IdObraSocial),
    Observaciones   NVARCHAR(500),
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE(),
    FechaModificacion DATETIME2 DEFAULT GETDATE(),
    -- RN-T02: HoraFin > HoraComienzo
    CONSTRAINT CK_Turno_Horario CHECK (HoraFin > HoraComienzo)
);

-- Index for RN-T01 overlap detection performance
CREATE INDEX IDX_Turno_Profesional_Fecha ON T_Turno(IdProfesional, Fecha, Estado, Baja);

-- 11. T_Pagos (1:1 with T_Turno)
CREATE TABLE T_Pagos (
    IdPago          INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    IdTurno         INT NOT NULL UNIQUE REFERENCES T_Turno(IdTurno),  -- exactly 1 per turno (RN-F03)
    Pagado          BIT NOT NULL DEFAULT 0,
    MetodoPago      NVARCHAR(50),
    Monto           DECIMAL(10,2) NOT NULL,
    ComprobanteImg  NVARCHAR(500),  -- RN-F05: optional
    FechaPago       DATETIME2,
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE()
);

-- 12. T_Recibo
CREATE TABLE T_Recibo (
    IdRecibo        INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    IdPago          INT REFERENCES T_Pagos(IdPago),
    NroRecibo       NVARCHAR(20) NOT NULL,  -- REC-YYYY-NNNNN (RN-F02)
    MontoTotal      DECIMAL(10,2) NOT NULL,
    RutaPDF         NVARCHAR(500),
    FechaEmision    DATETIME2 DEFAULT GETDATE(),
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,  -- RN-F04: anulado, no regen with same number
    FechaCreacion   DATETIME2 DEFAULT GETDATE()
);

-- 13. T_HistoriaClinica
CREATE TABLE T_HistoriaClinica (
    IdHistoriaClinica INT IDENTITY(1,1) PRIMARY KEY,
    UUID              NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    IdPaciente        INT NOT NULL REFERENCES T_Paciente(IdPaciente),
    IdTurno           INT REFERENCES T_Turno(IdTurno),
    -- RN-H02: Contenido stored AES-256-GCM encrypted (application level)
    Contenido         NVARCHAR(MAX) NOT NULL,
    Resumen           NVARCHAR(200),
    IdProfesional     INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema         INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja              BIT NOT NULL DEFAULT 0,  -- RN-H03: no physical delete
    FechaCreacion     DATETIME2 DEFAULT GETDATE(),
    FechaModificacion DATETIME2 DEFAULT GETDATE()
);

-- 14. T_Notificacion
CREATE TABLE T_Notificacion (
    IdNotificacion  INT IDENTITY(1,1) PRIMARY KEY,
    UUID            NVARCHAR(36) NOT NULL UNIQUE DEFAULT NEWID(),
    IdTurno         INT REFERENCES T_Turno(IdTurno),
    IdPaciente      INT REFERENCES T_Paciente(IdPaciente),
    Tipo            NVARCHAR(50) NOT NULL,   -- RECORDATORIO_24HS, CANCELACION, DATOS_PAGO
    Canal           NVARCHAR(20) NOT NULL,   -- EMAIL, WHATSAPP
    Estado          NVARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                    CHECK (Estado IN ('PENDIENTE', 'ENVIADO', 'FALLIDO', 'CANCELADO')),
    Intentos        INT NOT NULL DEFAULT 0,  -- RN-N02: max 3
    Detalle         NVARCHAR(500),
    FechaProgramada DATETIME2,
    FechaEnvio      DATETIME2,
    IdProfesional   INT NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       INT NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BIT NOT NULL DEFAULT 0,
    FechaCreacion   DATETIME2 DEFAULT GETDATE()
);

-- 15. T_LogGeneral (audit)
CREATE TABLE T_LogGeneral (
    IdLog       BIGINT IDENTITY(1,1) PRIMARY KEY,
    IdUsuario   NVARCHAR(36),
    Accion      NVARCHAR(100) NOT NULL,
    Modulo      NVARCHAR(50) NOT NULL,
    Datos       NVARCHAR(MAX),
    IpOrigen    NVARCHAR(50),
    Timestamp   DATETIME2 NOT NULL DEFAULT GETDATE(),
    IdSistema   INT REFERENCES T_Sistema(IdSistema),
    Baja        BIT NOT NULL DEFAULT 0
);
