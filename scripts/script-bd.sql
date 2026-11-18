/* =====================================================================
   ZelaCidade - CitizenTech | Global Solution 2026/2
   DDL dos objetos de banco - Azure SQL Database (T-SQL)
   Como executar: Portal Azure > Banco SQL "db-zelacidade" > Editor de consultas
   (ou: sqlcmd -S <servidor>.database.windows.net -d db-zelacidade -U <usuario> -P <senha> -i script-bd.sql)
   ===================================================================== */

-- Remove na ordem inversa das dependências (permite reexecutar o script)
IF OBJECT_ID('dbo.TB_ZC_OCORRENCIA', 'U') IS NOT NULL DROP TABLE dbo.TB_ZC_OCORRENCIA;
IF OBJECT_ID('dbo.TB_ZC_CATEGORIA',  'U') IS NOT NULL DROP TABLE dbo.TB_ZC_CATEGORIA;
IF OBJECT_ID('dbo.TB_ZC_CIDADAO',    'U') IS NOT NULL DROP TABLE dbo.TB_ZC_CIDADAO;
GO

/* ---------------------------------------------------------------------
   TB_ZC_CIDADAO - cidadão que registra ocorrências
   --------------------------------------------------------------------- */
CREATE TABLE dbo.TB_ZC_CIDADAO (
    ID_CIDADAO   BIGINT IDENTITY(1,1) NOT NULL,
    NOME         VARCHAR(100)  NOT NULL,
    EMAIL        VARCHAR(120)  NOT NULL,
    TELEFONE     VARCHAR(20)   NULL,
    BAIRRO       VARCHAR(80)   NOT NULL,
    DT_CADASTRO  DATETIME2     NOT NULL CONSTRAINT DF_ZC_CIDADAO_DT DEFAULT SYSDATETIME(),
    CONSTRAINT PK_ZC_CIDADAO PRIMARY KEY (ID_CIDADAO),
    CONSTRAINT UK_ZC_CIDADAO_EMAIL UNIQUE (EMAIL)
);
GO

/* ---------------------------------------------------------------------
   TB_ZC_CATEGORIA - tipo de problema urbano e prazo de atendimento
   --------------------------------------------------------------------- */
CREATE TABLE dbo.TB_ZC_CATEGORIA (
    ID_CATEGORIA            BIGINT IDENTITY(1,1) NOT NULL,
    NOME                    VARCHAR(60)  NOT NULL,
    DESCRICAO               VARCHAR(255) NULL,
    PRAZO_ATENDIMENTO_DIAS  INT          NOT NULL,
    CONSTRAINT PK_ZC_CATEGORIA PRIMARY KEY (ID_CATEGORIA),
    CONSTRAINT UK_ZC_CATEGORIA_NOME UNIQUE (NOME),
    CONSTRAINT CK_ZC_CATEGORIA_PRAZO CHECK (PRAZO_ATENDIMENTO_DIAS BETWEEN 1 AND 90)
);
GO

/* ---------------------------------------------------------------------
   TB_ZC_OCORRENCIA - problema registrado em um espaço público
   --------------------------------------------------------------------- */
CREATE TABLE dbo.TB_ZC_OCORRENCIA (
    ID_OCORRENCIA    BIGINT IDENTITY(1,1) NOT NULL,
    TITULO           VARCHAR(100)  NOT NULL,
    DESCRICAO        VARCHAR(500)  NULL,
    ENDERECO         VARCHAR(200)  NOT NULL,
    LATITUDE         DECIMAL(9,6)  NULL,
    LONGITUDE        DECIMAL(9,6)  NULL,
    STATUS           VARCHAR(20)   NOT NULL CONSTRAINT DF_ZC_OCORRENCIA_STATUS DEFAULT 'ABERTA',
    PRIORIDADE       INT           NOT NULL,
    DT_ABERTURA      DATETIME2     NOT NULL CONSTRAINT DF_ZC_OCORRENCIA_DT DEFAULT SYSDATETIME(),
    DT_ATUALIZACAO   DATETIME2     NULL,
    ID_CIDADAO       BIGINT        NOT NULL,
    ID_CATEGORIA     BIGINT        NOT NULL,
    CONSTRAINT PK_ZC_OCORRENCIA PRIMARY KEY (ID_OCORRENCIA),
    CONSTRAINT FK_ZC_OCORRENCIA_CIDADAO   FOREIGN KEY (ID_CIDADAO)   REFERENCES dbo.TB_ZC_CIDADAO (ID_CIDADAO),
    CONSTRAINT FK_ZC_OCORRENCIA_CATEGORIA FOREIGN KEY (ID_CATEGORIA) REFERENCES dbo.TB_ZC_CATEGORIA (ID_CATEGORIA),
    CONSTRAINT CK_ZC_OCORRENCIA_STATUS CHECK (STATUS IN ('ABERTA','EM_ANALISE','EM_ATENDIMENTO','RESOLVIDA','CANCELADA')),
    CONSTRAINT CK_ZC_OCORRENCIA_PRIORIDADE CHECK (PRIORIDADE BETWEEN 1 AND 5)
);
GO

CREATE INDEX IX_ZC_OCORRENCIA_STATUS ON dbo.TB_ZC_OCORRENCIA (STATUS);
GO

/* ---------------------------------------------------------------------
   Carga inicial (categorias padrão)
   --------------------------------------------------------------------- */
INSERT INTO dbo.TB_ZC_CATEGORIA (NOME, DESCRICAO, PRAZO_ATENDIMENTO_DIAS) VALUES
 ('Buraco na via',        'Buracos e afundamentos no asfalto',              10),
 ('Iluminação pública',   'Poste apagado, piscando ou danificado',           5),
 ('Calçada danificada',   'Calçada quebrada ou sem acessibilidade',         15),
 ('Descarte irregular',   'Lixo ou entulho descartado em local público',     3);
GO

/* ---------------------------------------------------------------------
   Consultas úteis para o vídeo (comprovar o CRUD direto no banco)
   --------------------------------------------------------------------- */
-- SELECT * FROM dbo.TB_ZC_CIDADAO;
-- SELECT * FROM dbo.TB_ZC_CATEGORIA;
-- SELECT * FROM dbo.TB_ZC_OCORRENCIA;
-- SELECT o.ID_OCORRENCIA, o.TITULO, o.STATUS, c.NOME AS CIDADAO, cat.NOME AS CATEGORIA
--   FROM dbo.TB_ZC_OCORRENCIA o
--   JOIN dbo.TB_ZC_CIDADAO   c   ON c.ID_CIDADAO   = o.ID_CIDADAO
--   JOIN dbo.TB_ZC_CATEGORIA cat ON cat.ID_CATEGORIA = o.ID_CATEGORIA;
