CREATE TABLE T_Facturas (
    idFactura INT IDENTITY(1,1) PRIMARY KEY,
    uuid VARCHAR(36) NOT NULL UNIQUE,
    idPaciente INT NOT NULL,
    idProfesional INT NOT NULL,
    idSistema INT NOT NULL,
    nombreArchivo VARCHAR(255) NOT NULL,
    rutaArchivo VARCHAR(500) NOT NULL,
    fechaCreacion DATETIME2 DEFAULT GETDATE(),
    baja TINYINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_factura_paciente FOREIGN KEY(idPaciente) REFERENCES T_Paciente(IdPaciente),
    CONSTRAINT fk_factura_profesional FOREIGN KEY(idProfesional) REFERENCES T_Profesional(IdProfesional),
    CONSTRAINT fk_factura_sistema FOREIGN KEY(idSistema) REFERENCES T_Sistema(IdSistema)
);
