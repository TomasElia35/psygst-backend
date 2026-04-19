-- ============================================================
-- PsyGst — V5__Refactor_To_UUID_Final.sql
-- Migración Zero Data Loss: INT PKs/FKs → NVARCHAR(36) UUIDs
-- Compatible: Azure SQL Server
-- 
-- ESTRATEGIA:
--   1. Tablas con UUID existente → reutilizar ese valor como nueva PK
--   2. Tablas lookup sin UUID    → generar NEWID() como nueva PK
--   3. Columnas INT antiguas quedan como Old_Id* para verificación
--   4. T_LogGeneral.IdLog (BIGINT) NO se migra por decisión de diseño
--
-- BACKUP CONFIRMADO ANTES DE EJECUTAR ✓
-- ============================================================

BEGIN TRANSACTION;

BEGIN TRY

-- ============================================================
-- FASE 0 — DIAGNÓSTICO Y VALIDACIONES PREVIAS
-- Ejecutar primero solo este bloque para detectar inconsistencias
-- ============================================================

PRINT '=== FASE 0: Diagnóstico ===';

-- Verificar NULLs en columnas UUID de tablas de negocio
IF EXISTS (SELECT 1 FROM T_Sistema        WHERE UUID IS NULL) RAISERROR('T_Sistema tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Profesional    WHERE UUID IS NULL) RAISERROR('T_Profesional tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Paciente       WHERE UUID IS NULL) RAISERROR('T_Paciente tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Turno          WHERE UUID IS NULL) RAISERROR('T_Turno tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Pagos          WHERE UUID IS NULL) RAISERROR('T_Pagos tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Recibo         WHERE UUID IS NULL) RAISERROR('T_Recibo tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_HistoriaClinica WHERE UUID IS NULL) RAISERROR('T_HistoriaClinica tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Notificacion   WHERE UUID IS NULL) RAISERROR('T_Notificacion tiene filas sin UUID', 16, 1);
IF EXISTS (SELECT 1 FROM T_Facturas       WHERE UUID IS NULL) RAISERROR('T_Facturas tiene filas sin UUID', 16, 1);

-- Verificar duplicados en UUIDs de negocio
IF EXISTS (SELECT UUID, COUNT(*) FROM T_Paciente    GROUP BY UUID HAVING COUNT(*) > 1) RAISERROR('T_Paciente tiene UUIDs duplicados', 16, 1);
IF EXISTS (SELECT UUID, COUNT(*) FROM T_Turno       GROUP BY UUID HAVING COUNT(*) > 1) RAISERROR('T_Turno tiene UUIDs duplicados', 16, 1);
IF EXISTS (SELECT UUID, COUNT(*) FROM T_Profesional GROUP BY UUID HAVING COUNT(*) > 1) RAISERROR('T_Profesional tiene UUIDs duplicados', 16, 1);

PRINT 'FASE 0 OK — Sin NULLs ni duplicados detectados';

-- ============================================================
-- FASE 1 — AGREGAR COLUMNAS NUEVAS
-- PK: New_PK_UUID   |   FK: New_FK_{Tabla}_UUID
-- ============================================================

PRINT '=== FASE 1: Agregando columnas nuevas ===';

-- Tablas maestras: Nueva PK UUID
ALTER TABLE T_Sistema         ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Rol             ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Profesion       ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_ObraSocial      ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Motivo          ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Profesional     ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Auth            ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Arancel         ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Paciente        ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Turno           ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Pagos           ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Recibo          ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_HistoriaClinica ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Notificacion    ADD New_PK_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Facturas        ADD New_PK_UUID NVARCHAR(36) NULL;

-- T_Profesional: FK → T_Profesion, T_Sistema
ALTER TABLE T_Profesional ADD New_FK_Profesion_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Profesional ADD New_FK_Sistema_UUID   NVARCHAR(36) NULL;

-- T_Auth: FK → T_Rol, T_Profesional
ALTER TABLE T_Auth ADD New_FK_Rol_UUID        NVARCHAR(36) NULL;
ALTER TABLE T_Auth ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;

-- T_Arancel: FK → T_Profesional, T_ObraSocial, T_Sistema
ALTER TABLE T_Arancel ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Arancel ADD New_FK_ObraSocial_UUID  NVARCHAR(36) NULL;
ALTER TABLE T_Arancel ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_Paciente: FK → T_ObraSocial, T_Profesional, T_Sistema, T_Motivo
ALTER TABLE T_Paciente ADD New_FK_ObraSocial_UUID  NVARCHAR(36) NULL;
ALTER TABLE T_Paciente ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Paciente ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;
ALTER TABLE T_Paciente ADD New_FK_Motivo_UUID       NVARCHAR(36) NULL;

-- T_Turno: FK → T_Paciente, T_Profesional, T_ObraSocial, T_Sistema
ALTER TABLE T_Turno ADD New_FK_Paciente_UUID    NVARCHAR(36) NULL;
ALTER TABLE T_Turno ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Turno ADD New_FK_ObraSocial_UUID  NVARCHAR(36) NULL;
ALTER TABLE T_Turno ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_Pagos: FK → T_Turno, T_Profesional, T_Sistema
ALTER TABLE T_Pagos ADD New_FK_Turno_UUID       NVARCHAR(36) NULL;
ALTER TABLE T_Pagos ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Pagos ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_Recibo: FK → T_Pagos, T_Profesional, T_Sistema
ALTER TABLE T_Recibo ADD New_FK_Pago_UUID        NVARCHAR(36) NULL;
ALTER TABLE T_Recibo ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Recibo ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_HistoriaClinica: FK → T_Paciente, T_Turno, T_Profesional, T_Sistema
ALTER TABLE T_HistoriaClinica ADD New_FK_Paciente_UUID    NVARCHAR(36) NULL;
ALTER TABLE T_HistoriaClinica ADD New_FK_Turno_UUID       NVARCHAR(36) NULL;
ALTER TABLE T_HistoriaClinica ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_HistoriaClinica ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_Notificacion: FK → T_Turno, T_Paciente, T_Profesional, T_Sistema
ALTER TABLE T_Notificacion ADD New_FK_Turno_UUID       NVARCHAR(36) NULL;
ALTER TABLE T_Notificacion ADD New_FK_Paciente_UUID    NVARCHAR(36) NULL;
ALTER TABLE T_Notificacion ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Notificacion ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_Facturas: FK → T_Paciente, T_Profesional, T_Sistema
ALTER TABLE T_Facturas ADD New_FK_Paciente_UUID    NVARCHAR(36) NULL;
ALTER TABLE T_Facturas ADD New_FK_Profesional_UUID NVARCHAR(36) NULL;
ALTER TABLE T_Facturas ADD New_FK_Sistema_UUID      NVARCHAR(36) NULL;

-- T_LogGeneral: FK → T_Sistema (IdLog permanece BIGINT)
ALTER TABLE T_LogGeneral ADD New_FK_Sistema_UUID NVARCHAR(36) NULL;

PRINT 'FASE 1 OK';

-- ============================================================
-- FASE 2 — POBLAR NUEVAS PKs UUID
-- Tablas con UUID existente: reutilizar. Lookup: NEWID()
-- ============================================================

PRINT '=== FASE 2: Poblando UUIDs ===';

-- Reutilizar UUIDs existentes
UPDATE T_Sistema          SET New_PK_UUID = UUID;
UPDATE T_Profesional      SET New_PK_UUID = UUID;
UPDATE T_Paciente         SET New_PK_UUID = UUID;
UPDATE T_Turno            SET New_PK_UUID = UUID;
UPDATE T_Pagos            SET New_PK_UUID = UUID;
UPDATE T_Recibo           SET New_PK_UUID = UUID;
UPDATE T_HistoriaClinica  SET New_PK_UUID = UUID;
UPDATE T_Notificacion     SET New_PK_UUID = UUID;
UPDATE T_Facturas         SET New_PK_UUID = UUID;

-- Generar nuevos UUIDs para tablas lookup
UPDATE T_Rol       SET New_PK_UUID = NEWID();
UPDATE T_Profesion SET New_PK_UUID = NEWID();
UPDATE T_ObraSocial SET New_PK_UUID = NEWID();
UPDATE T_Motivo    SET New_PK_UUID = NEWID();
UPDATE T_Arancel   SET New_PK_UUID = NEWID();
UPDATE T_Auth      SET New_PK_UUID = NEWID();

-- Validar que no quedaron NULLs en ninguna PK nueva
IF EXISTS (SELECT 1 FROM T_Sistema         WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Sistema', 16, 1);
IF EXISTS (SELECT 1 FROM T_Rol             WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Rol', 16, 1);
IF EXISTS (SELECT 1 FROM T_Profesion       WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Profesion', 16, 1);
IF EXISTS (SELECT 1 FROM T_ObraSocial      WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_ObraSocial', 16, 1);
IF EXISTS (SELECT 1 FROM T_Motivo          WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Motivo', 16, 1);
IF EXISTS (SELECT 1 FROM T_Profesional     WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Profesional', 16, 1);
IF EXISTS (SELECT 1 FROM T_Auth            WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Auth', 16, 1);
IF EXISTS (SELECT 1 FROM T_Arancel         WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Arancel', 16, 1);
IF EXISTS (SELECT 1 FROM T_Paciente        WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Paciente', 16, 1);
IF EXISTS (SELECT 1 FROM T_Turno           WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Turno', 16, 1);
IF EXISTS (SELECT 1 FROM T_Pagos           WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Pagos', 16, 1);
IF EXISTS (SELECT 1 FROM T_Recibo          WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Recibo', 16, 1);
IF EXISTS (SELECT 1 FROM T_HistoriaClinica WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_HistoriaClinica', 16, 1);
IF EXISTS (SELECT 1 FROM T_Notificacion    WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Notificacion', 16, 1);
IF EXISTS (SELECT 1 FROM T_Facturas        WHERE New_PK_UUID IS NULL) RAISERROR('New_PK_UUID NULL en T_Facturas', 16, 1);

PRINT 'FASE 2 OK';

-- ============================================================
-- FASE 3 — MAPEO DE FKS (UPDATE con JOIN por INT antes del swap)
-- Cada FK child apunta al New_PK_UUID del parent por su INT actual
-- ============================================================

PRINT '=== FASE 3: Mapeando Foreign Keys ===';

-- T_Profesional → T_Profesion, T_Sistema
UPDATE p SET p.New_FK_Profesion_UUID = pr.New_PK_UUID
FROM T_Profesional p JOIN T_Profesion pr ON p.IdProfesion = pr.IdProfesion;

UPDATE p SET p.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Profesional p JOIN T_Sistema s ON p.IdSistema = s.IdSistema;

-- T_Auth → T_Rol, T_Profesional (IdProfesional puede ser NULL para admins)
UPDATE a SET a.New_FK_Rol_UUID = r.New_PK_UUID
FROM T_Auth a JOIN T_Rol r ON a.IdRol = r.IdRol;

UPDATE a SET a.New_FK_Profesional_UUID = p.New_PK_UUID
FROM T_Auth a JOIN T_Profesional p ON a.IdProfesional = p.IdProfesional;

-- T_Arancel → T_Profesional, T_ObraSocial, T_Sistema
UPDATE a SET a.New_FK_Profesional_UUID = p.New_PK_UUID
FROM T_Arancel a JOIN T_Profesional p ON a.IdProfesional = p.IdProfesional;

UPDATE a SET a.New_FK_ObraSocial_UUID = os.New_PK_UUID
FROM T_Arancel a JOIN T_ObraSocial os ON a.IdObraSocial = os.IdObraSocial;

UPDATE a SET a.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Arancel a JOIN T_Sistema s ON a.IdSistema = s.IdSistema;

-- T_Paciente → T_ObraSocial, T_Profesional, T_Sistema, T_Motivo
UPDATE p SET p.New_FK_ObraSocial_UUID = os.New_PK_UUID
FROM T_Paciente p JOIN T_ObraSocial os ON p.IdObraSocial = os.IdObraSocial;

UPDATE p SET p.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Paciente p JOIN T_Profesional pr ON p.IdProfesional = pr.IdProfesional;

UPDATE p SET p.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Paciente p JOIN T_Sistema s ON p.IdSistema = s.IdSistema;

UPDATE p SET p.New_FK_Motivo_UUID = m.New_PK_UUID
FROM T_Paciente p JOIN T_Motivo m ON p.IdMotivo = m.IdMotivo;
-- IdMotivo puede ser NULL: UPDATE solo donde aplica (JOIN implícitamente excluye NULLs) ✓

-- T_Turno → T_Paciente, T_Profesional, T_ObraSocial, T_Sistema
UPDATE t SET t.New_FK_Paciente_UUID = p.New_PK_UUID
FROM T_Turno t JOIN T_Paciente p ON t.IdPaciente = p.IdPaciente;

UPDATE t SET t.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Turno t JOIN T_Profesional pr ON t.IdProfesional = pr.IdProfesional;

UPDATE t SET t.New_FK_ObraSocial_UUID = os.New_PK_UUID
FROM T_Turno t JOIN T_ObraSocial os ON t.IdObraSocial = os.IdObraSocial;
-- IdObraSocial puede ser NULL ✓

UPDATE t SET t.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Turno t JOIN T_Sistema s ON t.IdSistema = s.IdSistema;

-- T_Pagos → T_Turno, T_Profesional, T_Sistema
UPDATE p SET p.New_FK_Turno_UUID = t.New_PK_UUID
FROM T_Pagos p JOIN T_Turno t ON p.IdTurno = t.IdTurno;

UPDATE p SET p.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Pagos p JOIN T_Profesional pr ON p.IdProfesional = pr.IdProfesional;

UPDATE p SET p.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Pagos p JOIN T_Sistema s ON p.IdSistema = s.IdSistema;

-- T_Recibo → T_Pagos, T_Profesional, T_Sistema
UPDATE r SET r.New_FK_Pago_UUID = pg.New_PK_UUID
FROM T_Recibo r JOIN T_Pagos pg ON r.IdPago = pg.IdPago;
-- IdPago puede ser NULL ✓

UPDATE r SET r.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Recibo r JOIN T_Profesional pr ON r.IdProfesional = pr.IdProfesional;

UPDATE r SET r.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Recibo r JOIN T_Sistema s ON r.IdSistema = s.IdSistema;

-- T_HistoriaClinica → T_Paciente, T_Turno, T_Profesional, T_Sistema
UPDATE h SET h.New_FK_Paciente_UUID = p.New_PK_UUID
FROM T_HistoriaClinica h JOIN T_Paciente p ON h.IdPaciente = p.IdPaciente;

UPDATE h SET h.New_FK_Turno_UUID = t.New_PK_UUID
FROM T_HistoriaClinica h JOIN T_Turno t ON h.IdTurno = t.IdTurno;
-- IdTurno puede ser NULL ✓

UPDATE h SET h.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_HistoriaClinica h JOIN T_Profesional pr ON h.IdProfesional = pr.IdProfesional;

UPDATE h SET h.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_HistoriaClinica h JOIN T_Sistema s ON h.IdSistema = s.IdSistema;

-- T_Notificacion → T_Turno, T_Paciente, T_Profesional, T_Sistema
UPDATE n SET n.New_FK_Turno_UUID = t.New_PK_UUID
FROM T_Notificacion n JOIN T_Turno t ON n.IdTurno = t.IdTurno;

UPDATE n SET n.New_FK_Paciente_UUID = p.New_PK_UUID
FROM T_Notificacion n JOIN T_Paciente p ON n.IdPaciente = p.IdPaciente;

UPDATE n SET n.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Notificacion n JOIN T_Profesional pr ON n.IdProfesional = pr.IdProfesional;

UPDATE n SET n.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Notificacion n JOIN T_Sistema s ON n.IdSistema = s.IdSistema;

-- T_Facturas → T_Paciente, T_Profesional, T_Sistema
UPDATE f SET f.New_FK_Paciente_UUID = p.New_PK_UUID
FROM T_Facturas f JOIN T_Paciente p ON f.IdPaciente = p.IdPaciente;

UPDATE f SET f.New_FK_Profesional_UUID = pr.New_PK_UUID
FROM T_Facturas f JOIN T_Profesional pr ON f.IdProfesional = pr.IdProfesional;

UPDATE f SET f.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_Facturas f JOIN T_Sistema s ON f.IdSistema = s.IdSistema;

-- T_LogGeneral → T_Sistema
UPDATE lg SET lg.New_FK_Sistema_UUID = s.New_PK_UUID
FROM T_LogGeneral lg JOIN T_Sistema s ON lg.IdSistema = s.IdSistema;

-- Validar mapeos críticos (no deben tener NULLs en FKs NOT NULL)
IF EXISTS (SELECT 1 FROM T_Paciente  WHERE New_FK_Profesional_UUID IS NULL) RAISERROR('T_Paciente: New_FK_Profesional_UUID tiene NULLs', 16, 1);
IF EXISTS (SELECT 1 FROM T_Paciente  WHERE New_FK_Sistema_UUID     IS NULL) RAISERROR('T_Paciente: New_FK_Sistema_UUID tiene NULLs', 16, 1);
IF EXISTS (SELECT 1 FROM T_Turno     WHERE New_FK_Paciente_UUID    IS NULL) RAISERROR('T_Turno: New_FK_Paciente_UUID tiene NULLs', 16, 1);
IF EXISTS (SELECT 1 FROM T_Turno     WHERE New_FK_Profesional_UUID IS NULL) RAISERROR('T_Turno: New_FK_Profesional_UUID tiene NULLs', 16, 1);
IF EXISTS (SELECT 1 FROM T_Pagos     WHERE New_FK_Turno_UUID       IS NULL) RAISERROR('T_Pagos: New_FK_Turno_UUID tiene NULLs', 16, 1);
IF EXISTS (SELECT 1 FROM T_Auth      WHERE New_FK_Rol_UUID         IS NULL) RAISERROR('T_Auth: New_FK_Rol_UUID tiene NULLs', 16, 1);

PRINT 'FASE 3 OK';

-- ============================================================
-- FASE 4A — DROP: todas las FK constraints (SQL dinámico)
-- ============================================================

PRINT '=== FASE 4A: Eliminando FK constraints (dinámico) ===';

DECLARE @dropFks NVARCHAR(MAX) = '';
SELECT @dropFks = @dropFks +
    'ALTER TABLE [' + OBJECT_NAME(fk.parent_object_id) + '] DROP CONSTRAINT [' + fk.name + '];' + CHAR(10)
FROM sys.foreign_keys fk
WHERE OBJECT_NAME(fk.parent_object_id) IN (
    'T_Sistema','T_Rol','T_Profesion','T_ObraSocial','T_Motivo',
    'T_Profesional','T_Auth','T_Arancel','T_Paciente','T_Turno',
    'T_Pagos','T_Recibo','T_HistoriaClinica','T_Notificacion',
    'T_Facturas','T_LogGeneral'
);
EXEC sp_executesql @dropFks;

PRINT 'FASE 4A OK';

-- ============================================================
-- FASE 4B — DROP: PK constraints (SQL dinámico)
-- ============================================================

PRINT '=== FASE 4B: Eliminando PK constraints (dinámico) ===';

DECLARE @dropPks NVARCHAR(MAX) = '';
SELECT @dropPks = @dropPks +
    'ALTER TABLE [' + OBJECT_NAME(kc.parent_object_id) + '] DROP CONSTRAINT [' + kc.name + '];' + CHAR(10)
FROM sys.key_constraints kc
WHERE kc.type = 'PK'
AND OBJECT_NAME(kc.parent_object_id) IN (
    'T_Sistema','T_Rol','T_Profesion','T_ObraSocial','T_Motivo',
    'T_Profesional','T_Auth','T_Arancel','T_Paciente','T_Turno',
    'T_Pagos','T_Recibo','T_HistoriaClinica','T_Notificacion',
    'T_Facturas'
    -- T_LogGeneral PK permanece como BIGINT, no se toca
);
EXEC sp_executesql @dropPks;

PRINT 'FASE 4B OK';

-- ============================================================
-- FASE 4C — DROP: índices de performance y UQ conocidos
-- ============================================================

PRINT '=== FASE 4C: Eliminando índices ===';

IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'IDX_Turno_Profesional_Fecha')
    DROP INDEX IDX_Turno_Profesional_Fecha ON T_Turno;

IF EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UQ_Paciente_DNI_Sistema')
    DROP INDEX UQ_Paciente_DNI_Sistema ON T_Paciente;

PRINT 'FASE 4C OK';

-- ============================================================
-- FASE 4D — ALTER COLUMN: hacer NOT NULL las nuevas PKs y FKs requeridas
-- ============================================================

PRINT '=== FASE 4D: Aplicando NOT NULL a nuevas columnas ===';

-- PKs (todas deben ser NOT NULL)
ALTER TABLE T_Sistema         ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Rol             ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Profesion       ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_ObraSocial      ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Motivo          ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Profesional     ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Auth            ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Arancel         ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Paciente        ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Turno           ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Pagos           ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Recibo          ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_HistoriaClinica ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Notificacion    ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;
ALTER TABLE T_Facturas        ALTER COLUMN New_PK_UUID NVARCHAR(36) NOT NULL;

-- FKs NOT NULL (según esquema original V1)
ALTER TABLE T_Profesional     ALTER COLUMN New_FK_Sistema_UUID      NVARCHAR(36) NOT NULL;
ALTER TABLE T_Auth            ALTER COLUMN New_FK_Rol_UUID           NVARCHAR(36) NOT NULL;
ALTER TABLE T_Arancel         ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Arancel         ALTER COLUMN New_FK_ObraSocial_UUID    NVARCHAR(36) NOT NULL;
ALTER TABLE T_Arancel         ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Paciente        ALTER COLUMN New_FK_ObraSocial_UUID    NVARCHAR(36) NOT NULL;
ALTER TABLE T_Paciente        ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Paciente        ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Turno           ALTER COLUMN New_FK_Paciente_UUID      NVARCHAR(36) NOT NULL;
ALTER TABLE T_Turno           ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Turno           ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Pagos           ALTER COLUMN New_FK_Turno_UUID         NVARCHAR(36) NOT NULL;
ALTER TABLE T_Pagos           ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Pagos           ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Recibo          ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Recibo          ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_HistoriaClinica ALTER COLUMN New_FK_Paciente_UUID      NVARCHAR(36) NOT NULL;
ALTER TABLE T_HistoriaClinica ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_HistoriaClinica ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Notificacion    ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Notificacion    ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;
ALTER TABLE T_Facturas        ALTER COLUMN New_FK_Paciente_UUID      NVARCHAR(36) NOT NULL;
ALTER TABLE T_Facturas        ALTER COLUMN New_FK_Profesional_UUID   NVARCHAR(36) NOT NULL;
ALTER TABLE T_Facturas        ALTER COLUMN New_FK_Sistema_UUID        NVARCHAR(36) NOT NULL;

PRINT 'FASE 4D OK';

-- ============================================================
-- FASE 4E — RENAME: columnas INT → Old_Id* (backup de verificación)
-- ============================================================

PRINT '=== FASE 4E: Renombrando columnas INT → Old_Id* ===';

-- PKs Integer → Old_Id*
EXEC sp_rename 'T_Sistema.IdSistema',              'Old_IdSistema',              'COLUMN';
EXEC sp_rename 'T_Rol.IdRol',                      'Old_IdRol',                  'COLUMN';
EXEC sp_rename 'T_Profesion.IdProfesion',          'Old_IdProfesion',            'COLUMN';
EXEC sp_rename 'T_ObraSocial.IdObraSocial',        'Old_IdObraSocial',           'COLUMN';
EXEC sp_rename 'T_Motivo.IdMotivo',                'Old_IdMotivo',               'COLUMN';
EXEC sp_rename 'T_Profesional.IdProfesional',      'Old_IdProfesional',          'COLUMN';
EXEC sp_rename 'T_Auth.IdAuth',                    'Old_IdAuth',                 'COLUMN';
EXEC sp_rename 'T_Arancel.IdArancel',              'Old_IdArancel',              'COLUMN';
EXEC sp_rename 'T_Paciente.IdPaciente',            'Old_IdPaciente',             'COLUMN';
EXEC sp_rename 'T_Turno.IdTurno',                  'Old_IdTurno',                'COLUMN';
EXEC sp_rename 'T_Pagos.IdPago',                   'Old_IdPago',                 'COLUMN';
EXEC sp_rename 'T_Recibo.IdRecibo',                'Old_IdRecibo',               'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.IdHistoriaClinica', 'Old_IdHistoriaClinica',   'COLUMN';
EXEC sp_rename 'T_Notificacion.IdNotificacion',    'Old_IdNotificacion',         'COLUMN';
EXEC sp_rename 'T_Facturas.IdFactura',             'Old_IdFactura',              'COLUMN';

-- FKs Integer → Old_Id* en tablas relacionadas
EXEC sp_rename 'T_Profesional.IdProfesion',        'Old_FK_IdProfesion',         'COLUMN';
EXEC sp_rename 'T_Profesional.IdSistema',          'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Auth.IdRol',                     'Old_FK_IdRol',               'COLUMN';
EXEC sp_rename 'T_Auth.IdProfesional',             'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Arancel.IdProfesional',          'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Arancel.IdObraSocial',           'Old_FK_IdObraSocial',        'COLUMN';
EXEC sp_rename 'T_Arancel.IdSistema',              'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Paciente.IdObraSocial',          'Old_FK_IdObraSocial',        'COLUMN';
EXEC sp_rename 'T_Paciente.IdProfesional',         'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Paciente.IdSistema',             'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Paciente.IdMotivo',              'Old_FK_IdMotivo',            'COLUMN';
EXEC sp_rename 'T_Turno.IdPaciente',               'Old_FK_IdPaciente',          'COLUMN';
EXEC sp_rename 'T_Turno.IdProfesional',            'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Turno.IdObraSocial',             'Old_FK_IdObraSocial',        'COLUMN';
EXEC sp_rename 'T_Turno.IdSistema',                'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Pagos.IdTurno',                  'Old_FK_IdTurno',             'COLUMN';
EXEC sp_rename 'T_Pagos.IdProfesional',            'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Pagos.IdSistema',                'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Recibo.IdPago',                  'Old_FK_IdPago',              'COLUMN';
EXEC sp_rename 'T_Recibo.IdProfesional',           'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Recibo.IdSistema',               'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.IdPaciente',     'Old_FK_IdPaciente',          'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.IdTurno',        'Old_FK_IdTurno',             'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.IdProfesional',  'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.IdSistema',      'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Notificacion.IdTurno',           'Old_FK_IdTurno',             'COLUMN';
EXEC sp_rename 'T_Notificacion.IdPaciente',        'Old_FK_IdPaciente',          'COLUMN';
EXEC sp_rename 'T_Notificacion.IdProfesional',     'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Notificacion.IdSistema',         'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_Facturas.IdPaciente',            'Old_FK_IdPaciente',          'COLUMN';
EXEC sp_rename 'T_Facturas.IdProfesional',         'Old_FK_IdProfesional',       'COLUMN';
EXEC sp_rename 'T_Facturas.IdSistema',             'Old_FK_IdSistema',           'COLUMN';
EXEC sp_rename 'T_LogGeneral.IdSistema',           'Old_FK_IdSistema',           'COLUMN';

PRINT 'FASE 4E OK';

-- ============================================================
-- FASE 4F — RENAME: columnas New_PK_UUID → Id* (swap a nombres estándar)
-- ============================================================

PRINT '=== FASE 4F: Renombrando New_PK_UUID → Id* ===';

EXEC sp_rename 'T_Sistema.New_PK_UUID',              'IdSistema',          'COLUMN';
EXEC sp_rename 'T_Rol.New_PK_UUID',                  'IdRol',              'COLUMN';
EXEC sp_rename 'T_Profesion.New_PK_UUID',            'IdProfesion',        'COLUMN';
EXEC sp_rename 'T_ObraSocial.New_PK_UUID',           'IdObraSocial',       'COLUMN';
EXEC sp_rename 'T_Motivo.New_PK_UUID',               'IdMotivo',           'COLUMN';
EXEC sp_rename 'T_Profesional.New_PK_UUID',          'IdProfesional',      'COLUMN';
EXEC sp_rename 'T_Auth.New_PK_UUID',                 'IdAuth',             'COLUMN';
EXEC sp_rename 'T_Arancel.New_PK_UUID',              'IdArancel',          'COLUMN';
EXEC sp_rename 'T_Paciente.New_PK_UUID',             'IdPaciente',         'COLUMN';
EXEC sp_rename 'T_Turno.New_PK_UUID',                'IdTurno',            'COLUMN';
EXEC sp_rename 'T_Pagos.New_PK_UUID',                'IdPago',             'COLUMN';
EXEC sp_rename 'T_Recibo.New_PK_UUID',               'IdRecibo',           'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.New_PK_UUID',      'IdHistoriaClinica',  'COLUMN';
EXEC sp_rename 'T_Notificacion.New_PK_UUID',         'IdNotificacion',     'COLUMN';
EXEC sp_rename 'T_Facturas.New_PK_UUID',             'IdFactura',          'COLUMN';

-- FK columns → Id* (nombres estándar de JPA)
EXEC sp_rename 'T_Profesional.New_FK_Profesion_UUID',       'IdProfesion',   'COLUMN';
EXEC sp_rename 'T_Profesional.New_FK_Sistema_UUID',         'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Auth.New_FK_Rol_UUID',                    'IdRol',         'COLUMN';
EXEC sp_rename 'T_Auth.New_FK_Profesional_UUID',            'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Arancel.New_FK_Profesional_UUID',         'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Arancel.New_FK_ObraSocial_UUID',          'IdObraSocial',  'COLUMN';
EXEC sp_rename 'T_Arancel.New_FK_Sistema_UUID',             'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Paciente.New_FK_ObraSocial_UUID',         'IdObraSocial',  'COLUMN';
EXEC sp_rename 'T_Paciente.New_FK_Profesional_UUID',        'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Paciente.New_FK_Sistema_UUID',            'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Paciente.New_FK_Motivo_UUID',             'IdMotivo',      'COLUMN';
EXEC sp_rename 'T_Turno.New_FK_Paciente_UUID',              'IdPaciente',    'COLUMN';
EXEC sp_rename 'T_Turno.New_FK_Profesional_UUID',           'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Turno.New_FK_ObraSocial_UUID',            'IdObraSocial',  'COLUMN';
EXEC sp_rename 'T_Turno.New_FK_Sistema_UUID',               'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Pagos.New_FK_Turno_UUID',                 'IdTurno',       'COLUMN';
EXEC sp_rename 'T_Pagos.New_FK_Profesional_UUID',           'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Pagos.New_FK_Sistema_UUID',               'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Recibo.New_FK_Pago_UUID',                 'IdPago',        'COLUMN';
EXEC sp_rename 'T_Recibo.New_FK_Profesional_UUID',          'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Recibo.New_FK_Sistema_UUID',              'IdSistema',     'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.New_FK_Paciente_UUID',    'IdPaciente',    'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.New_FK_Turno_UUID',       'IdTurno',       'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.New_FK_Profesional_UUID', 'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_HistoriaClinica.New_FK_Sistema_UUID',     'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Notificacion.New_FK_Turno_UUID',          'IdTurno',       'COLUMN';
EXEC sp_rename 'T_Notificacion.New_FK_Paciente_UUID',       'IdPaciente',    'COLUMN';
EXEC sp_rename 'T_Notificacion.New_FK_Profesional_UUID',    'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Notificacion.New_FK_Sistema_UUID',        'IdSistema',     'COLUMN';
EXEC sp_rename 'T_Facturas.New_FK_Paciente_UUID',           'IdPaciente',    'COLUMN';
EXEC sp_rename 'T_Facturas.New_FK_Profesional_UUID',        'IdProfesional', 'COLUMN';
EXEC sp_rename 'T_Facturas.New_FK_Sistema_UUID',            'IdSistema',     'COLUMN';
EXEC sp_rename 'T_LogGeneral.New_FK_Sistema_UUID',          'IdSistema',     'COLUMN';

PRINT 'FASE 4F OK';

-- ============================================================
-- FASE 4G — ADD PRIMARY KEY constraints (con nombres explícitos)
-- ============================================================

PRINT '=== FASE 4G: Agregando PRIMARY KEYs UUID ===';

ALTER TABLE T_Sistema         ADD CONSTRAINT PK_T_Sistema         PRIMARY KEY (IdSistema);
ALTER TABLE T_Rol             ADD CONSTRAINT PK_T_Rol             PRIMARY KEY (IdRol);
ALTER TABLE T_Profesion       ADD CONSTRAINT PK_T_Profesion       PRIMARY KEY (IdProfesion);
ALTER TABLE T_ObraSocial      ADD CONSTRAINT PK_T_ObraSocial      PRIMARY KEY (IdObraSocial);
ALTER TABLE T_Motivo          ADD CONSTRAINT PK_T_Motivo          PRIMARY KEY (IdMotivo);
ALTER TABLE T_Profesional     ADD CONSTRAINT PK_T_Profesional     PRIMARY KEY (IdProfesional);
ALTER TABLE T_Auth            ADD CONSTRAINT PK_T_Auth            PRIMARY KEY (IdAuth);
ALTER TABLE T_Arancel         ADD CONSTRAINT PK_T_Arancel         PRIMARY KEY (IdArancel);
ALTER TABLE T_Paciente        ADD CONSTRAINT PK_T_Paciente        PRIMARY KEY (IdPaciente);
ALTER TABLE T_Turno           ADD CONSTRAINT PK_T_Turno           PRIMARY KEY (IdTurno);
ALTER TABLE T_Pagos           ADD CONSTRAINT PK_T_Pagos           PRIMARY KEY (IdPago);
ALTER TABLE T_Recibo          ADD CONSTRAINT PK_T_Recibo          PRIMARY KEY (IdRecibo);
ALTER TABLE T_HistoriaClinica ADD CONSTRAINT PK_T_HistoriaClinica PRIMARY KEY (IdHistoriaClinica);
ALTER TABLE T_Notificacion    ADD CONSTRAINT PK_T_Notificacion    PRIMARY KEY (IdNotificacion);
ALTER TABLE T_Facturas        ADD CONSTRAINT PK_T_Facturas        PRIMARY KEY (IdFactura);

PRINT 'FASE 4G OK';

-- ============================================================
-- FASE 4H — ADD FOREIGN KEY constraints (con nombres explícitos)
-- Recrear TODAS las relaciones de negocio sobre las nuevas columnas UUID
-- ============================================================

PRINT '=== FASE 4H: Agregando FOREIGN KEYs UUID ===';

-- T_Profesional
ALTER TABLE T_Profesional ADD CONSTRAINT FK_Profesional_Profesion
    FOREIGN KEY (IdProfesion) REFERENCES T_Profesion(IdProfesion);

ALTER TABLE T_Profesional ADD CONSTRAINT FK_Profesional_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Auth
ALTER TABLE T_Auth ADD CONSTRAINT FK_Auth_Rol
    FOREIGN KEY (IdRol) REFERENCES T_Rol(IdRol);

ALTER TABLE T_Auth ADD CONSTRAINT FK_Auth_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

-- T_Arancel
ALTER TABLE T_Arancel ADD CONSTRAINT FK_Arancel_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Arancel ADD CONSTRAINT FK_Arancel_ObraSocial
    FOREIGN KEY (IdObraSocial) REFERENCES T_ObraSocial(IdObraSocial);

ALTER TABLE T_Arancel ADD CONSTRAINT FK_Arancel_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Paciente
ALTER TABLE T_Paciente ADD CONSTRAINT FK_Paciente_ObraSocial
    FOREIGN KEY (IdObraSocial) REFERENCES T_ObraSocial(IdObraSocial);

ALTER TABLE T_Paciente ADD CONSTRAINT FK_Paciente_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Paciente ADD CONSTRAINT FK_Paciente_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

ALTER TABLE T_Paciente ADD CONSTRAINT FK_Paciente_Motivo
    FOREIGN KEY (IdMotivo) REFERENCES T_Motivo(IdMotivo);

-- T_Turno
ALTER TABLE T_Turno ADD CONSTRAINT FK_Turno_Paciente
    FOREIGN KEY (IdPaciente) REFERENCES T_Paciente(IdPaciente);

ALTER TABLE T_Turno ADD CONSTRAINT FK_Turno_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Turno ADD CONSTRAINT FK_Turno_ObraSocial
    FOREIGN KEY (IdObraSocial) REFERENCES T_ObraSocial(IdObraSocial);

ALTER TABLE T_Turno ADD CONSTRAINT FK_Turno_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Pagos (IdTurno UNIQUE: 1 pago por turno, RN-F03)
ALTER TABLE T_Pagos ADD CONSTRAINT FK_Pagos_Turno
    FOREIGN KEY (IdTurno) REFERENCES T_Turno(IdTurno);

ALTER TABLE T_Pagos ADD CONSTRAINT UQ_Pagos_Turno
    UNIQUE (IdTurno);

ALTER TABLE T_Pagos ADD CONSTRAINT FK_Pagos_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Pagos ADD CONSTRAINT FK_Pagos_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Recibo
ALTER TABLE T_Recibo ADD CONSTRAINT FK_Recibo_Pagos
    FOREIGN KEY (IdPago) REFERENCES T_Pagos(IdPago);

ALTER TABLE T_Recibo ADD CONSTRAINT FK_Recibo_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Recibo ADD CONSTRAINT FK_Recibo_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_HistoriaClinica
ALTER TABLE T_HistoriaClinica ADD CONSTRAINT FK_HistoriaClinica_Paciente
    FOREIGN KEY (IdPaciente) REFERENCES T_Paciente(IdPaciente);

ALTER TABLE T_HistoriaClinica ADD CONSTRAINT FK_HistoriaClinica_Turno
    FOREIGN KEY (IdTurno) REFERENCES T_Turno(IdTurno);

ALTER TABLE T_HistoriaClinica ADD CONSTRAINT FK_HistoriaClinica_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_HistoriaClinica ADD CONSTRAINT FK_HistoriaClinica_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Notificacion
ALTER TABLE T_Notificacion ADD CONSTRAINT FK_Notificacion_Turno
    FOREIGN KEY (IdTurno) REFERENCES T_Turno(IdTurno);

ALTER TABLE T_Notificacion ADD CONSTRAINT FK_Notificacion_Paciente
    FOREIGN KEY (IdPaciente) REFERENCES T_Paciente(IdPaciente);

ALTER TABLE T_Notificacion ADD CONSTRAINT FK_Notificacion_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Notificacion ADD CONSTRAINT FK_Notificacion_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_Facturas
ALTER TABLE T_Facturas ADD CONSTRAINT FK_Facturas_Paciente
    FOREIGN KEY (IdPaciente) REFERENCES T_Paciente(IdPaciente);

ALTER TABLE T_Facturas ADD CONSTRAINT FK_Facturas_Profesional
    FOREIGN KEY (IdProfesional) REFERENCES T_Profesional(IdProfesional);

ALTER TABLE T_Facturas ADD CONSTRAINT FK_Facturas_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

-- T_LogGeneral (IdLog permanece BIGINT, solo la FK a Sistema cambia)
ALTER TABLE T_LogGeneral ADD CONSTRAINT FK_LogGeneral_Sistema
    FOREIGN KEY (IdSistema) REFERENCES T_Sistema(IdSistema);

PRINT 'FASE 4H OK';

-- ============================================================
-- FASE 4I — RECREAR índices de performance
-- ============================================================

PRINT '=== FASE 4I: Recreando índices ===';

-- RN-T01: Detección de solapamiento de turnos (IdProfesional ahora NVARCHAR(36))
CREATE INDEX IDX_Turno_Profesional_Fecha
    ON T_Turno(IdProfesional, Fecha, Estado, Baja);

-- RN-P01: DNI único por tenant activo (IdSistema ahora NVARCHAR(36))
CREATE UNIQUE INDEX UQ_Paciente_DNI_Sistema
    ON T_Paciente(DNI, IdSistema)
    WHERE Baja = 0;

PRINT 'FASE 4I OK';

-- ============================================================
-- VERIFICACIÓN FINAL
-- ============================================================

PRINT '=== VERIFICACIÓN FINAL ===';

SELECT
    'T_Paciente'        AS Tabla, COUNT(*) AS Total FROM T_Paciente        UNION ALL
SELECT 'T_Turno',           COUNT(*) FROM T_Turno                          UNION ALL
SELECT 'T_Pagos',           COUNT(*) FROM T_Pagos                          UNION ALL
SELECT 'T_Recibo',          COUNT(*) FROM T_Recibo                         UNION ALL
SELECT 'T_HistoriaClinica', COUNT(*) FROM T_HistoriaClinica                UNION ALL
SELECT 'T_Notificacion',    COUNT(*) FROM T_Notificacion                   UNION ALL
SELECT 'T_Profesional',     COUNT(*) FROM T_Profesional                    UNION ALL
SELECT 'T_Facturas',        COUNT(*) FROM T_Facturas;

-- Verificar integridad: un JOIN clave
SELECT COUNT(*) AS TurnoConPaciente
FROM T_Turno t
JOIN T_Paciente p ON t.IdPaciente = p.IdPaciente
WHERE t.Baja = 0;

SELECT COUNT(*) AS PagoConTurno
FROM T_Pagos pg
JOIN T_Turno t ON pg.IdTurno = t.IdTurno
WHERE pg.Baja = 0;

PRINT '=== MIGRACIÓN COMPLETADA EXITOSAMENTE ===';
PRINT 'Columnas Old_Id* disponibles para verificación manual.';
PRINT 'Ejecutar V6 para eliminarlas después de 2 semanas de estabilidad.';

COMMIT TRANSACTION;

END TRY
BEGIN CATCH
    ROLLBACK TRANSACTION;
    DECLARE @ErrorMessage NVARCHAR(4000) = ERROR_MESSAGE();
    DECLARE @ErrorLine    INT            = ERROR_LINE();
    DECLARE @ErrorSev     INT            = ERROR_SEVERITY();
    PRINT 'ERROR EN MIGRACIÓN. ROLLBACK ejecutado.';
    PRINT 'Línea: ' + CAST(@ErrorLine AS NVARCHAR(10));
    PRINT 'Mensaje: ' + @ErrorMessage;
    RAISERROR(@ErrorMessage, @ErrorSev, 1);
END CATCH;
