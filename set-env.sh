#!/usr/bin/env bash
# 用法（bash / git bash）：
#   source ./set-env.sh            # 导出环境变量到当前 shell
#   source ./set-env.sh -r         # 导出后直接 mvn spring-boot:run
# 注意：必须用 source，否则变量只在子进程生效。

set -e
ENV_FILE="$(dirname "$0")/.env"
if [ ! -f "$ENV_FILE" ]; then
  echo "未找到 .env，请先复制 .env.example 为 .env 并填入真实值" >&2
  return 1 2>/dev/null || exit 1
fi

while IFS='=' read -r key val; do
  key="${key//[$'\t\r\n ']}"
  [ -z "$key" ] && continue
  [[ "$key" == \#* ]] && continue
  export "$key=$val"
  echo "已设置 $key" >&2
done < "$ENV_FILE"

if [ "${1:-}" = "-r" ]; then
  cd "$(dirname "$0")/server"
  mvn spring-boot:run
fi
