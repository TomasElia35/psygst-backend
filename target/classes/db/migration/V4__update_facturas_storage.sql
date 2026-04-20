ALTER TABLE T_Facturas DROP COLUMN rutaArchivo;
ALTER TABLE T_Facturas ADD datosArchivo VARBINARY(MAX);
