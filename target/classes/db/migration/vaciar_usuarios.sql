-- ==============================================================
-- SCRIPT DE VACIADO DE USUARIOS (CREDENCIALES, PROFESIONALES Y SISTEMAS)
-- IMPORTANTE: Para evitar errores de integridad, DEBES ejecutar 
-- primero el script 'vaciar_tablas.sql' si estos usuarios ya
-- tenían pacientes o turnos asignados.
-- ==============================================================

-- 1. Eliminar Credenciales (los accesos de login)
DELETE FROM T_Auth;

-- 2. Eliminar los Perfiles de Profesionales
DELETE FROM T_Profesional;

-- 3. Eliminar los Sistemas (Espacios de trabajo/Tenants)
DELETE FROM T_Sistema;

-- (Opcional) Reiniciar los IDs autoincrementables a 0
-- DBCC CHECKIDENT ('T_Auth', RESEED, 0);
-- DBCC CHECKIDENT ('T_Profesional', RESEED, 0);
-- DBCC CHECKIDENT ('T_Sistema', RESEED, 0);

PRINT 'Todos los usuarios, profesionales y sistemas han sido eliminados de la base de datos.'
