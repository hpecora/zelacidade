#!/usr/bin/env bash
# Remove todos os recursos do ZelaCidade (use DEPOIS da apresentação, para não gastar créditos)
#   export RM=rm123456
#   bash scripts/infra-destroy.sh
set -euo pipefail
RM="${RM:?Defina a variável RM. Ex.: export RM=rm123456}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-zelacidade-${RM}}"

echo "Excluindo o Resource Group ${RESOURCE_GROUP} e TODOS os recursos dentro dele..."
az group delete --name "$RESOURCE_GROUP" --yes --no-wait
echo "Exclusão iniciada (leva alguns minutos)."
