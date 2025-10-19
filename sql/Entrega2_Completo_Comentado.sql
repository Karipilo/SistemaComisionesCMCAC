-- FUNCIÓN ALMACENADA:
-- Calcula el total que le cuesta un auditor a la empresa en un periodo específico
CREATE OR REPLACE FUNCTION FN_OBTENER_COSTO_EMPRESA (
    p_run_auditor IN VARCHAR2,  -- RUT del auditor (con guión)
    p_mes         IN NUMBER,    -- Mes del proceso (1 al 12)
    p_anno        IN NUMBER     -- Año del proceso (e.g., 2021)
) RETURN NUMBER IS
    v_total NUMBER;  -- Variable de retorno
BEGIN
    SELECT total_comision_empresa INTO v_total
    FROM DETALLE_COMISIONES_AUDITORIAS_MES
    WHERE run_auditor = p_run_auditor
      AND mes_proceso = p_mes
      AND anno_proceso = p_anno;
    RETURN NVL(v_total, 0);  -- Si no encuentra dato, devuelve 0
EXCEPTION
    WHEN NO_DATA_FOUND THEN RETURN 0;
    WHEN OTHERS THEN RETURN 0;
END;
/

-- PROCEDIMIENTO CON CURSOR EXPLÍCITO SIN PARÁMETROS:
-- Recorre la tabla de detalle y puede ser útil para llenar estructuras en frontend o back
CREATE OR REPLACE PROCEDURE PRC_LISTAR_COMISIONES IS
    CURSOR cur_comisiones IS  -- Cursor que lista los datos básicos de comisiones
        SELECT run_auditor, nombre_auditor, total_comision_audit
        FROM DETALLE_COMISIONES_AUDITORIAS_MES;

    v_run    VARCHAR2(20);
    v_nombre VARCHAR2(100);
    v_total  NUMBER;
BEGIN
    OPEN cur_comisiones;
    LOOP
        FETCH cur_comisiones INTO v_run, v_nombre, v_total;
        EXIT WHEN cur_comisiones%NOTFOUND;
        -- Aquí se podría almacenar en una tabla intermedia, mostrar en frontend o retornar
        NULL;
    END LOOP;
    CLOSE cur_comisiones;
EXCEPTION
    WHEN OTHERS THEN
        -- Captura el error y lo registra en tabla de errores
        INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
        VALUES ('PRC_LISTAR_COMISIONES', SQLCODE, SQLERRM);
        COMMIT;
END;
/

-- PACKAGE CON CURSOR CON PARÁMETROS (REF CURSOR):
-- Permite obtener un reporte resumido por profesión (útil para dashboards o informes)
CREATE OR REPLACE PACKAGE PKG_REPORTE_COMISION IS
    TYPE t_cursor IS REF CURSOR;  -- Definición del tipo cursor dinámico
    PROCEDURE PRC_REPORTE_POR_PROFESION (
        p_mes IN NUMBER,           -- Mes del proceso
        p_anno IN NUMBER,          -- Año del proceso
        p_reporte OUT t_cursor     -- Cursor de salida con el reporte
    );
END PKG_REPORTE_COMISION;
/

CREATE OR REPLACE PACKAGE BODY PKG_REPORTE_COMISION IS
    PROCEDURE PRC_REPORTE_POR_PROFESION (
        p_mes IN NUMBER,
        p_anno IN NUMBER,
        p_reporte OUT t_cursor
    ) IS
    BEGIN
        OPEN p_reporte FOR
            SELECT nombre_profesion,
                   SUM(total_comision_audit) AS total_por_profesion,
                   SUM(total_comision_empresa) AS total_costo_empresa
            FROM DETALLE_COMISIONES_AUDITORIAS_MES
            WHERE mes_proceso = p_mes
              AND anno_proceso = p_anno
            GROUP BY nombre_profesion
            ORDER BY 2 DESC;
    EXCEPTION
        WHEN OTHERS THEN
            -- Captura error y lo registra
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('PKG_REPORTE_COMISION', SQLCODE, SQLERRM);
            COMMIT;
    END;
END PKG_REPORTE_COMISION;
/


-- SISTEMA DE CÁLCULO DE COMISIONES Y RESUMEN DE COSTOS EMPRESA
-- 1. ESTRUCTURAS BASE

DROP TABLE RESUMEN_COMISIONES_AUDITORIAS_MES CASCADE CONSTRAINTS;
DROP TABLE ERROR_PROCESO CASCADE CONSTRAINTS;
DROP SEQUENCE SEQ_ERROR_PROCESO;

CREATE SEQUENCE SEQ_ERROR_PROCESO START WITH 1 INCREMENT BY 1;

CREATE TABLE ERROR_PROCESO (
  id_error        NUMBER(10) DEFAULT SEQ_ERROR_PROCESO.NEXTVAL NOT NULL,
  programa_origen VARCHAR2(100) NOT NULL,
  fecha_error     DATE DEFAULT SYSDATE NOT NULL,
  sql_code        NUMBER NOT NULL,
  sql_mensaje     VARCHAR2(4000) NOT NULL,
  CONSTRAINT pk_error_proceso PRIMARY KEY (id_error)
);

CREATE TABLE RESUMEN_COMISIONES_AUDITORIAS_MES (
  mes_proceso         NUMBER(2) NOT NULL,
  anno_proceso        NUMBER(4) NOT NULL,
  total_auditores     NUMBER(5) NOT NULL,
  suma_comision_audit NUMBER(12,2) NOT NULL,
  suma_costo_empresa  NUMBER(12,2) NOT NULL,
  CONSTRAINT pk_resumen_comisiones PRIMARY KEY (mes_proceso, anno_proceso)
);

-- 2. PROCEDIMIENTOS CORREGIDOS

CREATE OR REPLACE PROCEDURE PRC_LIMPIAR_COMISIONES IS
    v_mes_proceso  NUMBER := 8;
    v_anno_proceso NUMBER := 2021;
BEGIN
    DELETE FROM DETALLE_COMISIONES_AUDITORIAS_MES
    WHERE mes_proceso = v_mes_proceso AND anno_proceso = v_anno_proceso;
EXCEPTION
    WHEN OTHERS THEN
        DECLARE
            v_code NUMBER := SQLCODE;
            v_msg  VARCHAR2(4000) := SQLERRM;
        BEGIN
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('PRC_LIMPIAR_COMISIONES', v_code, v_msg);
            COMMIT;
        END;
END PRC_LIMPIAR_COMISIONES;
/
 
CREATE OR REPLACE PROCEDURE PRC_AJUSTAR_COMISION_EXTRA (
    p_mes_proceso  IN NUMBER,
    p_anno_proceso IN NUMBER,
    p_nuevo_factor IN NUMBER
)
IS
BEGIN
    UPDATE DETALLE_COMISIONES_AUDITORIAS_MES D
    SET comision_extra = (
        SELECT ROUND(A.sueldo * ((NVL(TC.porc_incentivo, 0) + p_nuevo_factor) / 100), 2)
        FROM auditor A
        LEFT JOIN tipo_contrato TC ON A.cod_tpcontrato = TC.cod_tpcontrato
        WHERE D.run_auditor = A.numrun || '-' || A.dvrun
    )
    WHERE D.mes_proceso = p_mes_proceso
      AND D.anno_proceso = p_anno_proceso;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        DECLARE
            v_code NUMBER := SQLCODE;
            v_msg  VARCHAR2(4000) := SQLERRM;
        BEGIN
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('PRC_AJUSTAR_COMISION_EXTRA', v_code, v_msg);
            COMMIT;
        END;
        ROLLBACK;
END PRC_AJUSTAR_COMISION_EXTRA;
/
 
CREATE OR REPLACE PROCEDURE PRC_RESUMIR_COMISIONES (
    p_mes_proceso  IN NUMBER,
    p_anno_proceso IN NUMBER
)
IS
    v_total_auditores NUMBER(5);
    v_suma_comision   NUMBER(12,2);
    v_suma_costo      NUMBER(12,2);
BEGIN
    DELETE FROM RESUMEN_COMISIONES_AUDITORIAS_MES
    WHERE mes_proceso = p_mes_proceso AND anno_proceso = p_anno_proceso;

    SELECT COUNT(run_auditor),
           NVL(SUM(total_comision_audit), 0),
           NVL(SUM(total_comision_empresa), 0)
    INTO v_total_auditores, v_suma_comision, v_suma_costo
    FROM DETALLE_COMISIONES_AUDITORIAS_MES
    WHERE mes_proceso = p_mes_proceso AND anno_proceso = p_anno_proceso;

    IF v_total_auditores > 0 THEN
        INSERT INTO RESUMEN_COMISIONES_AUDITORIAS_MES
            (mes_proceso, anno_proceso, total_auditores, suma_comision_audit, suma_costo_empresa)
        VALUES (p_mes_proceso, p_anno_proceso, v_total_auditores, v_suma_comision, v_suma_costo);
    END IF;
    COMMIT;
EXCEPTION
    WHEN OTHERS THEN
        DECLARE
            v_code NUMBER := SQLCODE;
            v_msg  VARCHAR2(4000) := SQLERRM;
        BEGIN
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('PRC_RESUMIR_COMISIONES', v_code, v_msg);
            COMMIT;
        END;
        ROLLBACK;
END PRC_RESUMIR_COMISIONES;
/
 
-- 3. TRIGGER CON TRAZABILIDAD


CREATE OR REPLACE TRIGGER TRG_CALCULAR_RECARGO_EMP
BEFORE INSERT OR UPDATE OF total_comision_audit
ON DETALLE_COMISIONES_AUDITORIAS_MES
FOR EACH ROW
DECLARE
    v_msg VARCHAR2(4000);
BEGIN
    :NEW.total_comision_empresa := ROUND(NVL(:NEW.total_comision_audit, 0) * 1.10, 2);

    IF INSERTING THEN
        v_msg := 'INSERT -> total_comision_audit: ' || NVL(:NEW.total_comision_audit, 0)
              || ' | total_comision_empresa: ' || NVL(:NEW.total_comision_empresa, 0);
        INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
        VALUES ('TRG_CALCULAR_RECARGO_EMP (INSERT)', 0, v_msg);
    END IF;

    IF UPDATING THEN
        v_msg := 'UPDATE -> OLD total_comision_audit: ' || NVL(:OLD.total_comision_audit, 0)
              || ' | NEW total_comision_audit: ' || NVL(:NEW.total_comision_audit, 0)
              || ' | NEW total_comision_empresa: ' || NVL(:NEW.total_comision_empresa, 0);
        INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
        VALUES ('TRG_CALCULAR_RECARGO_EMP (UPDATE)', 0, v_msg);
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20001, 'Error en TRG_CALCULAR_RECARGO_EMP: ' || SQLERRM);
END TRG_CALCULAR_RECARGO_EMP;
/
 
-- 4. BLOQUE PRINCIPAL DE PROCESAMIENTO

DECLARE
    v_mes_proceso  NUMBER := 8;
    v_anno_proceso NUMBER := 2021;
BEGIN
    PRC_LIMPIAR_COMISIONES;
    COMMIT;

    INSERT INTO DETALLE_COMISIONES_AUDITORIAS_MES
    (mes_proceso, anno_proceso, run_auditor, nombre_auditor, nombre_profesion,
     comision_total_audit, comision_monto_audit, comision_prof_critica,
     comision_extra, total_comision_audit)
    SELECT
        v_mes_proceso, v_anno_proceso, run_auditor, MAX(nombre_auditor),
        MAX(nombre_profesion), MAX(comision_total_audit), MAX(comision_monto_audit),
        MAX(comision_prof_critica), MAX(comision_extra),
        ROUND(MAX(comision_total_audit) + MAX(comision_monto_audit)
        + MAX(comision_prof_critica) + MAX(comision_extra), 2)
    FROM (
        SELECT
            A.numrun || '-' || A.dvrun AS run_auditor,
            A.nombre || ' ' || A.appaterno || ' ' || A.apmaterno AS nombre_auditor,
            P.nombre_profesion,
            ROUND(T_AUDIT.monto_total_auditorias * (NVL(P_TOTAL.porc_total_audit, 0) / 100), 2) AS comision_total_audit,
            ROUND(T_AUDIT.monto_total_auditorias * NVL(P_MONTO.porc_monto_audit, 0), 2) AS comision_monto_audit,
            ROUND(CASE P.nivel_criticidad WHEN 1 THEN A.sueldo * 0.05 WHEN 2 THEN A.sueldo * 0.03
                  WHEN 3 THEN A.sueldo * 0.01 WHEN 4 THEN A.sueldo * 0.005 ELSE 0 END, 2) AS comision_prof_critica,
            ROUND(A.sueldo * (NVL(TC.porc_incentivo, 0) / 100), 2) AS comision_extra
        FROM auditor A
        INNER JOIN profesion P ON A.cod_profesion = P.cod_profesion
        LEFT JOIN tipo_contrato TC ON A.cod_tpcontrato = TC.cod_tpcontrato
        INNER JOIN (
            SELECT id_auditor, COUNT(id_auditor) AS total_auditorias,
                   SUM(monto_auditoria) AS monto_total_auditorias
            FROM auditoria
            WHERE EXTRACT(MONTH FROM fin_auditoria) = v_mes_proceso
              AND EXTRACT(YEAR FROM fin_auditoria) = v_anno_proceso
            GROUP BY id_auditor
        ) T_AUDIT ON A.id_auditor = T_AUDIT.id_auditor
        LEFT JOIN porc_total_auditorias P_TOTAL ON T_AUDIT.total_auditorias 
          BETWEEN P_TOTAL.total_audit_min AND P_TOTAL.total_audit_max
        LEFT JOIN porc_monto_auditorias P_MONTO ON T_AUDIT.monto_total_auditorias 
          BETWEEN P_MONTO.monto_audit_min AND P_MONTO.monto_audit_max
    ) S
    GROUP BY run_auditor, v_mes_proceso, v_anno_proceso;

    COMMIT;
    PRC_RESUMIR_COMISIONES(v_mes_proceso, v_anno_proceso);
EXCEPTION
    WHEN OTHERS THEN
        DECLARE
            v_code NUMBER := SQLCODE;
            v_msg  VARCHAR2(4000) := SQLERRM;
        BEGIN
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('BLOQUE_PRINCIPAL', v_code, v_msg);
            COMMIT;
        END;
        ROLLBACK;
END;
/
 
-- 5. BLOQUE DE PRUEBA DE ERROR (FORZADO)

CREATE OR REPLACE PROCEDURE PRC_LIMPIAR_COMISIONES IS
    v_mes_proceso  NUMBER := 8;
    v_anno_proceso NUMBER := 2021;
    v_dummy NUMBER;
BEGIN
    -- Error intencional: división por cero
    SELECT 1 / 0 INTO v_dummy FROM DUAL;

    DELETE FROM DETALLE_COMISIONES_AUDITORIAS_MES
    WHERE mes_proceso = v_mes_proceso AND anno_proceso = v_anno_proceso;
EXCEPTION
    WHEN OTHERS THEN
        DECLARE
            v_code NUMBER := SQLCODE;
            v_msg  VARCHAR2(4000) := SQLERRM;
        BEGIN
            INSERT INTO ERROR_PROCESO (programa_origen, sql_code, sql_mensaje)
            VALUES ('PRC_LIMPIAR_COMISIONES_TEST', v_code, v_msg);
            COMMIT;
        END;
END PRC_LIMPIAR_COMISIONES;
/
 
BEGIN
  PRC_LIMPIAR_COMISIONES;
END;
/
 
SELECT '--- REGISTRO DE ERROR_PROCESO (Debe mostrar el error ORA-01476) ---' AS MENSAJE FROM DUAL;
SELECT id_error, programa_origen, fecha_error, sql_code, sql_mensaje
FROM ERROR_PROCESO
ORDER BY id_error DESC;