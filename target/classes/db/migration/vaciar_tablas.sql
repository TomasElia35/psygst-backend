-- ==========================================
-- SCRIPT DE VACIADO DE TABLAS TRANSACTIONALES
-- (Mantiene usuarios, roles, y sistemas)
-- ==========================================

-- 1. Eliminar Logs de Auditoría
DELETE FROM T_LogGeneral;

-- 2. Eliminar Notificaciones (depende de Turno y Paciente)
DELETE FROM T_Notificacion;

-- 3. Eliminar Historias Clínicas (depende de Turno y Paciente)
DELETE FROM T_HistoriaClinica;

-- 4. Eliminar Recibos (depende de Pagos)
DELETE FROM T_Recibo;

-- 5. Eliminar Pagos (depende de Turno)
DELETE FROM T_Pagos;

-- 6. Eliminar Turnos (depende de Paciente)
DELETE FROM T_Turno;

-- 7. Eliminar Pacientes (depende de Profesional y Obra Social)
DELETE FROM T_Paciente;

-- 8. Eliminar Aranceles (depende de Profesional y Obra Social)
DELETE FROM T_Arancel;

-- Reiniciar los IDs autoincrementables a 0 para que arranquen en 1 (Opcional, descomentar si se desea)
-- DBCC CHECKIDENT ('T_LogGeneral', RESEED, 0);
-- DBCC CHECKIDENT ('T_Notificacion', RESEED, 0);
-- DBCC CHECKIDENT ('T_HistoriaClinica', RESEED, 0);
-- DBCC CHECKIDENT ('T_Recibo', RESEED, 0);
-- DBCC CHECKIDENT ('T_Pagos', RESEED, 0);
-- DBCC CHECKIDENT ('T_Turno', RESEED, 0);
-- DBCC CHECKIDENT ('T_Paciente', RESEED, 0);
-- DBCC CHECKIDENT ('T_Arancel', RESEED, 0);

PRINT 'Tablas transaccionales vaciadas. Los usuarios y configuraciones se mantienen intactos.'
