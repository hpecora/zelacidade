#!/usr/bin/env bash
# =====================================================================
#  ZelaCidade - Provisionamento da infraestrutura no Azure (Azure CLI)
#  Global Solution 2026/2 - DevOps Tools & Cloud Computing
#
#  Recursos criados:
#    1. Resource Group
#    2. Azure SQL Server (lógico) + regras de firewall
#    3. Azure SQL Database (PaaS)
#    4. App Service Plan (Linux)
#    5. Web App (Java 21 SE) + variáveis de ambiente (App Settings)
#
#  Como usar (Azure Cloud Shell - Bash, ou terminal com "az login"):
#    export RM=rm123456                 # seu RM (deixa os nomes únicos)
#    export SQL_PASSWORD='SuaSenhaForte#2026'   # NÃO coloque a senha no repositório
#    bash scripts/infra-azure.sh
#
#  Se a sua assinatura (Azure for Students) não permitir a região,
#  troque:  export LOCATION=eastus2   (ou eastus / westus2)
# =====================================================================
set -euo pipefail

# ---------- Variáveis (podem ser sobrescritas via export) ----------
RM="${RM:?Defina a variável RM. Ex.: export RM=rm123456}"
LOCATION="${LOCATION:-brazilsouth}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-zelacidade-${RM}}"

SQL_SERVER="${SQL_SERVER:-sql-zelacidade-${RM}}"
SQL_DB="${SQL_DB:-db-zelacidade}"
SQL_ADMIN="${SQL_ADMIN:-zcadmin}"

APP_PLAN="${APP_PLAN:-plan-zelacidade-${RM}}"
WEBAPP_NAME="${WEBAPP_NAME:-app-zelacidade-${RM}}"
APP_SKU="${APP_SKU:-B1}"
RUNTIME="JAVA:21-java21"

if [[ -z "${SQL_PASSWORD:-}" ]]; then
  read -r -s -p "Senha do administrador do SQL Server (min. 8 caracteres, maiúscula, minúscula, número e símbolo): " SQL_PASSWORD
  echo
fi

echo "==> Assinatura atual:"
az account show --query "{assinatura:name, id:id}" -o table

# ---------- 1. Resource Group ----------
echo "==> [1/5] Criando Resource Group ${RESOURCE_GROUP} em ${LOCATION}"
az group create \
  --name "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --tags projeto=zelacidade disciplina=devops gs=2026-2 \
  -o none

# ---------- 2. Azure SQL Server ----------
echo "==> [2/5] Criando Azure SQL Server ${SQL_SERVER}"
az sql server create \
  --name "$SQL_SERVER" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --admin-user "$SQL_ADMIN" \
  --admin-password "$SQL_PASSWORD" \
  -o none

echo "    Liberando acesso dos serviços do Azure (App Service -> SQL)"
az sql server firewall-rule create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0 \
  -o none

MEU_IP="$(curl -s https://api.ipify.org || true)"
if [[ -n "$MEU_IP" ]]; then
  echo "    Liberando o IP atual (${MEU_IP}) para usar o Editor de consultas / sqlcmd"
  az sql server firewall-rule create \
    --resource-group "$RESOURCE_GROUP" \
    --server "$SQL_SERVER" \
    --name AllowMeuIP \
    --start-ip-address "$MEU_IP" \
    --end-ip-address "$MEU_IP" \
    -o none
fi

# ---------- 3. Azure SQL Database ----------
echo "==> [3/5] Criando banco ${SQL_DB} (camada Basic)"
az sql db create \
  --resource-group "$RESOURCE_GROUP" \
  --server "$SQL_SERVER" \
  --name "$SQL_DB" \
  --service-objective Basic \
  --backup-storage-redundancy Local \
  -o none

# ---------- 4. App Service Plan ----------
echo "==> [4/5] Criando App Service Plan ${APP_PLAN} (${APP_SKU}, Linux)"
az appservice plan create \
  --name "$APP_PLAN" \
  --resource-group "$RESOURCE_GROUP" \
  --location "$LOCATION" \
  --sku "$APP_SKU" \
  --is-linux \
  -o none

# ---------- 5. Web App ----------
echo "==> [5/5] Criando Web App ${WEBAPP_NAME} (${RUNTIME})"
az webapp create \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --plan "$APP_PLAN" \
  --runtime "$RUNTIME" \
  -o none

DB_URL="jdbc:sqlserver://${SQL_SERVER}.database.windows.net:1433;database=${SQL_DB};encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"

echo "    Configurando variáveis de ambiente da aplicação (App Settings)"
az webapp config appsettings set \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --settings \
    DB_URL="$DB_URL" \
    DB_USERNAME="$SQL_ADMIN" \
    DB_PASSWORD="$SQL_PASSWORD" \
  -o none

echo "    Configurando comando de inicialização e logs"
az webapp config set \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --startup-file "java -jar /home/site/wwwroot/zelacidade.jar --server.port=80" \
  -o none

az webapp log config \
  --name "$WEBAPP_NAME" \
  --resource-group "$RESOURCE_GROUP" \
  --application-logging filesystem \
  --docker-container-logging filesystem \
  --level information \
  -o none

# ---------- (Opcional) Criar as tabelas com sqlcmd ----------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if command -v sqlcmd >/dev/null 2>&1; then
  echo "==> Executando script-bd.sql no banco"
  sqlcmd -S "${SQL_SERVER}.database.windows.net" -d "$SQL_DB" \
         -U "$SQL_ADMIN" -P "$SQL_PASSWORD" -i "${SCRIPT_DIR}/script-bd.sql"
else
  echo "==> sqlcmd não encontrado: execute scripts/script-bd.sql no Editor de consultas do Portal Azure."
fi

echo
echo "================= RECURSOS CRIADOS ================="
az resource list --resource-group "$RESOURCE_GROUP" --query "[].{nome:name, tipo:type}" -o table
echo
echo "URL da aplicação : https://${WEBAPP_NAME}.azurewebsites.net"
echo "Swagger          : https://${WEBAPP_NAME}.azurewebsites.net/swagger-ui.html"
echo "SQL Server       : ${SQL_SERVER}.database.windows.net  (banco: ${SQL_DB})"
echo
echo "Use estes valores nas variáveis da pipeline:"
echo "  webAppName    = ${WEBAPP_NAME}"
echo "  resourceGroup = ${RESOURCE_GROUP}"
echo "  sqlServer     = ${SQL_SERVER}"
echo "  sqlDatabase   = ${SQL_DB}"
echo "  dbUsername    = ${SQL_ADMIN}"
echo "  DB_PASSWORD   = (variável SECRETA, cadastrada na pipeline)"
