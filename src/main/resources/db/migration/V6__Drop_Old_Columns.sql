-- V6: Eliminar definitivamente las columnas obsoletas remanentes de la migración a UUID
-- Todas las columnas que empiezan con 'Old_' (tanto PKs como FKs) son eliminadas.
-- Para poder eliminarlas en SQL Server, primero debemos eliminar cualquier restricción dependiente (como DEFAULT constraints, UNIQUE, INDEXES).

DECLARE @TableName NVARCHAR(256), @ColumnName NVARCHAR(256), @ConstraintName NVARCHAR(256), @Sql NVARCHAR(MAX);

-- 1. Eliminar cualquier Default Constraint asociada a columnas 'Old_%'
DECLARE default_cursor CURSOR FOR
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    d.name AS ConstraintName
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.default_constraints d ON c.default_object_id = d.object_id
WHERE c.name LIKE 'Old_%';

OPEN default_cursor;
FETCH NEXT FROM default_cursor INTO @TableName, @ColumnName, @ConstraintName;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Sql = 'ALTER TABLE [' + @TableName + '] DROP CONSTRAINT [' + @ConstraintName + '];';
    EXEC sp_executesql @Sql;
    FETCH NEXT FROM default_cursor INTO @TableName, @ColumnName, @ConstraintName;
END
CLOSE default_cursor;
DEALLOCATE default_cursor;

-- 2. Eliminar cualquier Check Constraint asociada a columnas 'Old_%'
DECLARE check_cursor CURSOR FOR
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    chk.name AS ConstraintName
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.check_constraints chk ON chk.parent_object_id = t.object_id
WHERE c.name LIKE 'Old_%' AND chk.definition LIKE '%Old_%';

OPEN check_cursor;
FETCH NEXT FROM check_cursor INTO @TableName, @ColumnName, @ConstraintName;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Sql = 'ALTER TABLE [' + @TableName + '] DROP CONSTRAINT [' + @ConstraintName + '];';
    EXEC sp_executesql @Sql;
    FETCH NEXT FROM check_cursor INTO @TableName, @ColumnName, @ConstraintName;
END
CLOSE check_cursor;
DEALLOCATE check_cursor;

-- 3. Eliminar restricciones UNIQUE asociadas a columnas 'Old_%'
DECLARE unique_cursor CURSOR FOR
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    kc.name AS ConstraintName
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.key_constraints kc ON kc.parent_object_id = t.object_id
JOIN sys.index_columns ic ON ic.object_id = kc.parent_object_id AND ic.index_id = kc.unique_index_id AND ic.column_id = c.column_id
WHERE c.name LIKE 'Old_%' AND kc.type = 'UQ';

OPEN unique_cursor;
FETCH NEXT FROM unique_cursor INTO @TableName, @ColumnName, @ConstraintName;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Sql = 'ALTER TABLE [' + @TableName + '] DROP CONSTRAINT [' + @ConstraintName + '];';
    EXEC sp_executesql @Sql;
    FETCH NEXT FROM unique_cursor INTO @TableName, @ColumnName, @ConstraintName;
END
CLOSE unique_cursor;
DEALLOCATE unique_cursor;

-- 4. Eliminar Índices (no asociados a PK/UQ constraints) sobre columnas 'Old_%'
DECLARE index_cursor CURSOR FOR
SELECT 
    t.name AS TableName,
    c.name AS ColumnName,
    i.name AS IndexName
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
JOIN sys.index_columns ic ON ic.object_id = t.object_id AND ic.column_id = c.column_id
JOIN sys.indexes i ON i.object_id = t.object_id AND i.index_id = ic.index_id
WHERE c.name LIKE 'Old_%' AND i.is_primary_key = 0 AND i.is_unique_constraint = 0 AND i.type > 0;

OPEN index_cursor;
FETCH NEXT FROM index_cursor INTO @TableName, @ColumnName, @ConstraintName;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Sql = 'DROP INDEX [' + @ConstraintName + '] ON [' + @TableName + '];';
    EXEC sp_executesql @Sql;
    FETCH NEXT FROM index_cursor INTO @TableName, @ColumnName, @ConstraintName;
END
CLOSE index_cursor;
DEALLOCATE index_cursor;

-- 5. Finalmente, eliminar todas las columnas 'Old_%' de todas las tablas
DECLARE drop_cursor CURSOR FOR
SELECT 
    t.name AS TableName,
    c.name AS ColumnName
FROM sys.tables t
JOIN sys.columns c ON t.object_id = c.object_id
WHERE c.name LIKE 'Old_%';

OPEN drop_cursor;
FETCH NEXT FROM drop_cursor INTO @TableName, @ColumnName;
WHILE @@FETCH_STATUS = 0
BEGIN
    SET @Sql = 'ALTER TABLE [' + @TableName + '] DROP COLUMN [' + @ColumnName + '];';
    EXEC sp_executesql @Sql;
    FETCH NEXT FROM drop_cursor INTO @TableName, @ColumnName;
END
CLOSE drop_cursor;
DEALLOCATE drop_cursor;

PRINT 'Limpieza de columnas Old_ completada correctamente';
