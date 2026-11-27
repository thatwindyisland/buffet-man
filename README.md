# Buffet Man

自助餐厅商品管理后端，提供商品增删改查和有货筛选。项目使用 Java 17、Spring MVC、嵌入式 Tomcat、MyBatis 和 MySQL 8.4。

## Docker Compose 启动

需要 Docker Engine 和 Docker Compose。从项目根目录运行以下命令创建本地配置。已有 `.env` 时可直接使用。

```bash
cp .env.example .env
```

在 `.env` 中填写 `BUFFET_DB_PASSWORD`（应用账号密码）和 `MYSQL_ROOT_PASSWORD`（MySQL 管理员密码）。可以用 `BUFFET_PORT` 修改宿主机端口，默认是 `8080`。

```bash
docker compose up --build -d
docker compose ps
curl -fsS http://localhost:8080/goods/list
```

Compose 分别构建后端镜像 `buffet-man:local` 和数据库镜像 `buffet-mysql:8.4`。应用端口绑定到 `0.0.0.0`，其他设备可通过 `http://<宿主机 IP>:8080/goods/list` 访问。后端在容器网络内连接 `db:3306`。MySQL 首次初始化时执行 [schema.sql](src/main/resources/schema.sql)，创建 `buffet` 库中的 `goods` 表。后端等待 MySQL 健康检查通过后启动。

```bash
docker compose logs -f app db
docker compose down
```

`docker compose down` 会停止并移除容器，商品数据继续保存在 `mysql-data` 卷中。数据库账号和密码在数据卷首次初始化时设置，之后单独编辑 `.env` 不会更新 MySQL 中已有的密码。

### MySQL 健康检查报 ERROR 1045

复用已有数据卷时，先确认 `.env` 中的 `BUFFET_DB_PASSWORD` 与数据库中 `buffet` 账号的密码一致。若要保留 `.env` 中的新密码，可以用已有的 MySQL root 密码登录并更新应用账号。若启动 Compose 时用了 `sudo`，下列 Docker 命令也加上 `sudo`。

```bash
docker compose exec -e MYSQL_HISTFILE=/dev/null db mysql -uroot -p
```

```sql
SELECT User, Host FROM mysql.user WHERE User = 'buffet';
CREATE USER IF NOT EXISTS 'buffet'@'%' IDENTIFIED BY '<BUFFET_DB_PASSWORD 的值>';
ALTER USER 'buffet'@'%' IDENTIFIED BY '<BUFFET_DB_PASSWORD 的值>';
GRANT ALL ON buffet.* TO 'buffet'@'%';
```

上面的 SQL 针对 Compose 默认创建的 `'buffet'@'%'` 账号。如果查询结果还有针对本机连接的 `buffet` 账号，例如 `'buffet'@'127.0.0.1'`，也将该账号的密码同步，否则它可能优先匹配健康检查。退出 MySQL 后重新运行 `docker compose up -d`，再用 `docker compose ps` 确认数据库变为 healthy。

### 重建旧版商品表

旧版数据卷的 `goods_id` 是 `VARCHAR`。以下命令会清空商品数据并重建 `goods` 表，将主键改为 `INT`。在项目根目录执行。若启动 Compose 时用了 `sudo`，下列 Docker 命令也加上 `sudo`。

```bash
docker compose stop app &&
  { printf 'DROP TABLE IF EXISTS goods;\n'; cat src/main/resources/schema.sql; } |
  docker compose exec -T db sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql --default-character-set=utf8mb4 --user=root --database="$MYSQL_DATABASE"' &&
  docker compose up --build -d --wait --wait-timeout 120
```

## 部署到 Apple Silicon Mac

Apple Silicon 上的 Docker 运行 `linux/arm64` 镜像。若源机器是 x86-64，先构建 ARM64 版本。导出和导入镜像会保留原有架构。

在 Linux 源机器的项目根目录执行。如果当前账号可以直接运行 Docker，可省略 `sudo`。

```bash
sudo docker buildx build --platform linux/arm64 --load -t buffet-man:arm64 .
sudo docker buildx build --platform linux/arm64 --load -t buffet-mysql:arm64 -f Dockerfile.mysql .
sudo docker image save -o buffet-arm64.tar buffet-man:arm64 buffet-mysql:arm64
gzip buffet-arm64.tar
```

将 `buffet-arm64.tar.gz`、`compose.yaml` 和 `.env.example` 复制到 Mac 上的同一个目录。启动 [Docker Desktop for Apple Silicon](https://docs.docker.com/desktop/setup/install/mac-install/)，进入该目录后执行以下命令。如果已有 `.env`，跳过复制模板的命令。

```bash
docker image load -i buffet-arm64.tar.gz
docker tag buffet-man:arm64 buffet-man:local
docker tag buffet-mysql:arm64 buffet-mysql:8.4
cp .env.example .env
chmod 600 .env
```

在 `.env` 中填写 `BUFFET_DB_PASSWORD` 和 `MYSQL_ROOT_PASSWORD`，然后用导入的镜像启动服务。

```bash
docker compose up -d --no-build --pull never --wait --wait-timeout 120
curl -fsS http://localhost:8080/goods/list
```

如果 Linux 上的 Buildx 无法执行 ARM64 构建步骤，可以把项目源码复制到 Mac，配置 `.env` 后直接运行 `docker compose up --build -d`。镜像仅包含建表脚本，已有商品数据需要通过 MySQL 导出和恢复。 [Docker 多平台构建文档](https://docs.docker.com/build/building/multi-platform/)

## 商品接口

写入接口使用 `Content-Type: application/json`。商品编号 `goodsId` 由调用方提供，范围为 1 到 2147483647，响应中以 JSON 数字返回。库存状态 `stockStatus` 取 `有货` 或 `无货`。新增时可省略 `stockQty`，默认值为 `0`。有货筛选以 `stockStatus` 为准。

| 方法和路径 | 请求体 | 成功结果 |
| --- | --- | --- |
| `GET /goods/list` | — | `200`，全部商品 |
| `POST /goods/add` | `goodsId`、`goodsName`、`price`、`stockStatus`，可选 `stockQty` | `201`，新增的商品 |
| `PUT /goods/{goodsId}` | `price`、`stockStatus` | `200`，更新后的商品 |
| `DELETE /goods/{goodsId}` | — | `200`，返回 `goodsId` 和 `deleted: true` |
| `PUT /goods/status/{goodsId}` | `stockStatus` | `200`，返回最新的 `stockStatus` |
| `GET /goods/stock` | — | `200`，有货商品 |

字段校验失败或 JSON 无法解析时返回 `400`。更新或删除不存在的商品时返回 `404`，新增时编号重复返回 `409`。错误响应的 `error` 字段包含错误说明。

例如新增一份炒饭，再查询有货商品。

```bash
curl -fsS -X POST http://localhost:8080/goods/add \
  -H 'Content-Type: application/json' \
  -d '{"goodsId":1001,"goodsName":"炒饭","price":12.50,"stockStatus":"有货","stockQty":20}'

curl -fsS http://localhost:8080/goods/stock
```

应用还提供 `GET /api/health` 和 `GET /api/info`。要确认数据库可访问，可以请求 `GET /goods/list`。

### 接口冒烟测试

启动服务后运行 [test-goods.sh](scripts/test-goods.sh)。脚本需要 `curl` 和 `jq`，会用临时商品验证六个接口，结束时清理测试数据。

```bash
./scripts/test-goods.sh
./scripts/test-goods.sh http://localhost:8081
```

第二条命令适用于将 `BUFFET_PORT` 改为 `8081` 的情况。

## 直接运行 fat JAR

需要 JDK 17、Maven 和 MySQL。先创建 `buffet` 数据库，在其中执行 [schema.sql](src/main/resources/schema.sql)，并为应用准备可读写该库的账号。将数据库连接地址、账号和密码通过 `BUFFET_DB_URL`、`BUFFET_DB_USER` 和 `BUFFET_DB_PASSWORD` 传给应用进程。连接地址示例为 `jdbc:mysql://localhost:3306/buffet`。

```bash
mvn -DcreateDependencyReducedPom=false clean package
java -jar target/buffet-man-1.0-SNAPSHOT.jar
```

直接运行时，嵌入式 Tomcat 监听 `8080`。Maven Shade 已将运行时依赖打入 JAR。
