-- ============================================================
-- PsyGst v1.0 — PostgreSQL Schema Migration (Supabase Zero Data Loss)
-- V1__schema_pg.sql
-- ============================================================

-- 1. T_Sistema (tenants)
CREATE TABLE T_Sistema (
    IdSistema       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre          VARCHAR(100) NOT NULL,
    Activo          BOOLEAN NOT NULL DEFAULT TRUE,
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. T_Rol
CREATE TABLE T_Rol (
    IdRol   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre  VARCHAR(50) NOT NULL UNIQUE,
    Baja    BOOLEAN NOT NULL DEFAULT FALSE
);

-- 3. T_Profesion
CREATE TABLE T_Profesion (
    IdProfesion UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre      VARCHAR(100) NOT NULL,
    Baja        BOOLEAN NOT NULL DEFAULT FALSE
);

-- 4. T_ObraSocial
CREATE TABLE T_ObraSocial (
    IdObraSocial            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre                  VARCHAR(100) NOT NULL,
    ContactoLiquidacion     VARCHAR(200),
    Baja                    BOOLEAN NOT NULL DEFAULT FALSE
);

-- 5. T_Motivo (baja reasons)
CREATE TABLE T_Motivo (
    IdMotivo    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Descripcion VARCHAR(100) NOT NULL,
    Baja        BOOLEAN NOT NULL DEFAULT FALSE
);

-- 6. T_Profesional
CREATE TABLE T_Profesional (
    IdProfesional       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre              VARCHAR(100) NOT NULL,
    Apellido            VARCHAR(100) NOT NULL,
    CUIT                VARCHAR(20),
    NroLicencia         VARCHAR(50),
    Email               VARCHAR(100),
    Celular             VARCHAR(20),
    CBU                 VARCHAR(100),
    Alias               VARCHAR(50),
    IdProfesion         UUID REFERENCES T_Profesion(IdProfesion),
    IdSistema           UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja                BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. T_Auth
CREATE TABLE T_Auth (
    IdAuth          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Username        VARCHAR(100) NOT NULL UNIQUE,
    Password        VARCHAR(255) NOT NULL,
    IdRol           UUID NOT NULL REFERENCES T_Rol(IdRol),
    IdProfesional   UUID REFERENCES T_Profesional(IdProfesional),
    Activo          BOOLEAN NOT NULL DEFAULT TRUE,
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UltimoAcceso    TIMESTAMP
);

-- 8. T_Arancel
CREATE TABLE T_Arancel (
    IdArancel       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdObraSocial    UUID NOT NULL REFERENCES T_ObraSocial(IdObraSocial),
    Modalidad       VARCHAR(20) NOT NULL CHECK (Modalidad IN ('PRESENCIAL', 'VIRTUAL')),
    Precio          DECIMAL(10,2) NOT NULL,
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. T_Paciente
CREATE TABLE T_Paciente (
    IdPaciente      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    Nombre          VARCHAR(100) NOT NULL,
    Apellido        VARCHAR(100) NOT NULL,
    DNI             VARCHAR(20) NOT NULL,
    Email           VARCHAR(100),
    Celular         VARCHAR(20),
    IdObraSocial    UUID REFERENCES T_ObraSocial(IdObraSocial),
    NroAfiliado     VARCHAR(100),
    Observaciones   VARCHAR(500),
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    IdMotivo        UUID REFERENCES T_Motivo(IdMotivo),
    FechaBaja       TIMESTAMP,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX UQ_Paciente_DNI_Sistema ON T_Paciente(DNI, IdSistema) WHERE Baja = FALSE;

-- 10. T_Turno
CREATE TABLE T_Turno (
    IdTurno         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdPaciente      UUID NOT NULL REFERENCES T_Paciente(IdPaciente),
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    Fecha           DATE NOT NULL,
    HoraComienzo    TIME NOT NULL,
    HoraFin         TIME NOT NULL,
    Modalidad       VARCHAR(20) NOT NULL CHECK (Modalidad IN ('PRESENCIAL', 'VIRTUAL')),
    Estado          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMADO' CHECK (Estado IN ('CONFIRMADO', 'REALIZADO', 'CANCELADO')),
    PrecioFinal     DECIMAL(10,2) NOT NULL,
    IdObraSocial    UUID REFERENCES T_ObraSocial(IdObraSocial),
    Observaciones   VARCHAR(500),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT CK_Turno_Horario CHECK (HoraFin > HoraComienzo)
);

CREATE INDEX IDX_Turno_Profesional_Fecha ON T_Turno(IdProfesional, Fecha, Estado, Baja);

-- 11. T_Pagos
CREATE TABLE T_Pagos (
    IdPago          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdTurno         UUID NOT NULL UNIQUE REFERENCES T_Turno(IdTurno),
    Pagado          BOOLEAN NOT NULL DEFAULT FALSE,
    MetodoPago      VARCHAR(50),
    Monto           DECIMAL(10,2) NOT NULL,
    ComprobanteImg  VARCHAR(500),
    FechaPago       TIMESTAMP,
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. T_Recibo
CREATE TABLE T_Recibo (
    IdRecibo        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdPago          UUID REFERENCES T_Pagos(IdPago),
    NroRecibo       VARCHAR(20) NOT NULL,
    MontoTotal      DECIMAL(10,2) NOT NULL,
    RutaPDF         VARCHAR(500),
    FechaEmision    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 13. T_HistoriaClinica
CREATE TABLE T_HistoriaClinica (
    IdHistoriaClinica UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdPaciente        UUID NOT NULL REFERENCES T_Paciente(IdPaciente),
    IdTurno           UUID REFERENCES T_Turno(IdTurno),
    Contenido         TEXT NOT NULL,
    Resumen           VARCHAR(200),
    IdProfesional     UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema         UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja              BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FechaModificacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 14. T_Notificacion
CREATE TABLE T_Notificacion (
    IdNotificacion  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdTurno         UUID REFERENCES T_Turno(IdTurno),
    IdPaciente      UUID REFERENCES T_Paciente(IdPaciente),
    Tipo            VARCHAR(50) NOT NULL,
    Canal           VARCHAR(20) NOT NULL,
    Estado          VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (Estado IN ('PENDIENTE', 'ENVIADO', 'FALLIDO', 'CANCELADO')),
    Intentos        INT NOT NULL DEFAULT 0,
    Detalle         VARCHAR(500),
    FechaProgramada TIMESTAMP,
    FechaEnvio      TIMESTAMP,
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 15. T_Facturas
CREATE TABLE T_Facturas (
    IdFactura       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    IdPaciente      UUID NOT NULL REFERENCES T_Paciente(IdPaciente),
    RutaPDF         VARCHAR(500),
    FechaEmision    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    MontoTotal      DECIMAL(10,2) NOT NULL,
    IdProfesional   UUID NOT NULL REFERENCES T_Profesional(IdProfesional),
    IdSistema       UUID NOT NULL REFERENCES T_Sistema(IdSistema),
    Baja            BOOLEAN NOT NULL DEFAULT FALSE,
    FechaCreacion   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 16. T_LogGeneral
CREATE TABLE T_LogGeneral (
    IdLog       BIGSERIAL PRIMARY KEY,
    IdUsuario   VARCHAR(36),
    Accion      VARCHAR(100) NOT NULL,
    Modulo      VARCHAR(50) NOT NULL,
    Datos       TEXT,
    IpOrigen    VARCHAR(50),
    Timestamp   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    IdSistema   UUID REFERENCES T_Sistema(IdSistema),
    Baja        BOOLEAN NOT NULL DEFAULT FALSE
);
