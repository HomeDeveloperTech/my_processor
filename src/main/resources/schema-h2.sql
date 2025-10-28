-- Create schema in H2 to match hardcoded entity schemas
CREATE SCHEMA IF NOT EXISTS "uat_ficoipb";
CREATE SCHEMA IF NOT EXISTS UAT_FICOIPB;
SET SCHEMA "uat_ficoipb";

CREATE TABLE ALUGUEL_ORIGEM
(
    ANOMES                    VARCHAR(6)                          NOT NULL,
    DEVICE_STATUS             VARCHAR(3) NULL,
    INSTITUTION_NUMBER        VARCHAR(8) NULL,
    MERCHANT_BOARDING_DATE    VARCHAR(8) NULL,
    MERCHANT_NUMBER           VARCHAR(20) NULL,
    SERVICE_CONTRACT_ID       VARCHAR(10) NULL,
    SIGNED_SALES_VALUE        DECIMAL(18, 2) NULL,
    TERMINAL_ID               VARCHAR(20) NULL,
    TERMINAL_INSTALATION_DATE VARCHAR(8) NULL,
    DESC_TERMINAL_TYPE        VARCHAR(100) NULL,
    VALOR_ORIGEM              DECIMAL(18, 2) NULL,
    REGIS_NUM                 VARCHAR(50) NULL,
    ID_CLI_TYPE               VARCHAR(3) NULL,
    FEE_CHARG_MODEL_CD        DECIMAL(3, 0) NULL,
    ID_PREPM                  VARCHAR(3) NULL,
    MCC                       VARCHAR(10) NULL,
    CAPTURE                   VARCHAR(10) NULL,
    ACTIVATION_DATE           VARCHAR(8) NULL,
    CNAE                      VARCHAR(10) NULL,
    GRUPO_ECONOMICO           VARCHAR(10) NULL,
    GRUPO_ECONOMICO_DESCRICAO VARCHAR(255) NULL,
    TERMINAL_TYPE             VARCHAR(10) NULL,
    ALIANCA                   VARCHAR(20) NULL,
    DT_INSERT_CONTROLE        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ALUGUEL_EXCECAO
(
    ANOMES               VARCHAR(6)                          NOT NULL,
    INSTITUTION_NUMBER   VARCHAR(8)                          NOT NULL,
    MERCHANT_NUMBER      VARCHAR(20)                         NOT NULL,
    TERMINAL_ID          VARCHAR(20)                         NOT NULL,
    SERVICE_CONTRACT_ID  VARCHAR(10)                         NOT NULL,
    VALOR_EXCECAO        DECIMAL(18, 2)                      NOT NULL,
    DATA_LIMITE          DATE NULL,
    DT_INSERCAO_REGISTRO TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE ALUGUEL_PROCESSAMENTO_ALIANCA
(
    ANOMES                   VARCHAR(6)                          NOT NULL,
    INSTITUTION_NUMBER       VARCHAR(8)                          NOT NULL,
    MERCHANT_NUMBER          VARCHAR(20)                         NOT NULL,
    TERMINAL_ID              VARCHAR(20)                         NOT NULL,
    SERVICE_CONTRACT_ID      VARCHAR(10)                         NOT NULL,
    REGRA_APLICADA_DESCRICAO VARCHAR(255)                        NOT NULL,
    VALOR_ORIGEM             DECIMAL(18, 2)                      NOT NULL,
    VALOR_CORRIGIDO          DECIMAL(18, 2)                      NOT NULL,
    DT_INSERCAO_REGISTRO     TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);
