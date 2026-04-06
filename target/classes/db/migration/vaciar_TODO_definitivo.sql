-- =========================================================================
-- SCRIPT DEFINITIVO PARA VACIAR TODA LA BASE DE DATOS (PRODUCCIÓN/RESET)
-- =========================================================================

-- IMPORTANTE: SQL Server requiere que estas opciones estén activadas 
-- para poder modificar tablas que tienen índices especiales (como T_Paciente)
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ANSI_PADDING ON;

-- PASO 1: Eliminar datos transaccionales (Siempre de hijos a padres)
DELETE FROM T_LogGeneral;
DELETE FROM T_Notificacion;
DELETE FROM T_HistoriaClinica;
DELETE FROM T_Recibo;
DELETE FROM T_Pagos;
DELETE FROM T_Turno;
DELETE FROM T_Arancel;
DELETE FROM T_Paciente;

-- PASO 2: Eliminar usuarios y configuraciones base
DELETE FROM T_Auth;
DELETE FROM T_Profesional;
DELETE FROM T_Sistema;

-- PASO 3: Reiniciar todos los contadores (IDs) a cero
DBCC CHECKIDENT ('T_LogGeneral', RESEED, 0);
DBCC CHECKIDENT ('T_Notificacion', RESEED, 0);
DBCC CHECKIDENT ('T_HistoriaClinica', RESEED, 0);
DBCC CHECKIDENT ('T_Recibo', RESEED, 0);
DBCC CHECKIDENT ('T_Pagos', RESEED, 0);
DBCC CHECKIDENT ('T_Turno', RESEED, 0);
DBCC CHECKIDENT ('T_Paciente', RESEED, 0);
DBCC CHECKIDENT ('T_Arancel', RESEED, 0);
DBCC CHECKIDENT ('T_Auth', RESEED, 0);
DBCC CHECKIDENT ('T_Profesional', RESEED, 0);
DBCC CHECKIDENT ('T_Sistema', RESEED, 0);

PRINT '======================================================'
PRINT 'BASE DE DATOS COMPLETAMENTE VACIADA Y REINICIADA.'
PRINT '======================================================'
