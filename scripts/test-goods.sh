#!/bin/sh
set -eu

if [ "$#" -gt 1 ]; then
    printf '用法：%s [base-url]\n' "$0" >&2
    exit 2
fi

for command in curl jq; do
    if ! command -v "$command" >/dev/null 2>&1; then
        printf '缺少依赖：%s\n' "$command" >&2
        exit 2
    fi
done

base_url=${1:-http://localhost:8080}
base_url=${base_url%/}
work_dir=$(mktemp -d)
response="$work_dir/response.json"
goods_id=$(od -An -N4 -tu4 /dev/urandom | jq -e '. % 2147483647 + 1')
created=0

cleanup() {
    if [ "$created" -eq 1 ]; then
        curl -fsS --connect-timeout 3 --max-time 10 -X DELETE \
            "$base_url/goods/$goods_id" >/dev/null ||
            printf '清理测试商品失败：%s\n' "$goods_id" >&2
    fi
    rm -rf "$work_dir"
}
trap cleanup EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

request() {
    method=$1
    path=$2
    expected_status=$3
    shift 3

    if [ "$#" -gt 0 ]; then
        set -- -H 'Content-Type: application/json' --data "$1"
    fi
    status=$(curl -sS --connect-timeout 3 --max-time 10 -o "$response" \
        -w '%{http_code}' -X "$method" "$@" "$base_url$path") || exit 1
    if [ "$status" != "$expected_status" ]; then
        printf '%s %s 返回 %s，预期 %s\n' "$method" "$path" "$status" "$expected_status" >&2
        cat "$response" >&2
        printf '\n' >&2
        exit 1
    fi
}

expect_json() {
    if ! jq -e --argjson id "$goods_id" --argjson goods "$expected_goods" "$1" "$response" >/dev/null; then
        printf '%s %s 的 JSON 响应不符合预期\n' "$method" "$path" >&2
        cat "$response" >&2
        printf '\n' >&2
        exit 1
    fi
    printf '%s %s -> %s\n' "$method" "$path" "$status"
}

expected_goods=$(jq -cn --argjson id "$goods_id" \
    '{goodsId:$id, goodsName:"冒烟测试商品", price:12.5, stockStatus:"有货", stockQty:0}')
create_payload=$(printf '%s\n' "$expected_goods" | jq -c 'del(.stockQty)')
request POST /goods/add 201 "$create_payload"
created=1
expect_json '. == $goods'

request POST /goods/add 409 "$create_payload"
expect_json '.error | type == "string"'

request GET /goods/list 200
expect_json 'type == "array" and any(.[]; . == $goods)'

request GET /goods/stock 200
expect_json 'type == "array" and any(.[]; . == $goods)'

request PUT "/goods/$goods_id" 200 '{"price":15.5,"stockStatus":"无货"}'
expected_goods=$(printf '%s\n' "$expected_goods" | jq -c '.price = 15.5 | .stockStatus = "无货"')
expect_json '. == $goods'

request GET /goods/stock 200
expect_json 'type == "array" and all(.[]; .goodsId != $id)'

request PUT "/goods/status/$goods_id" 400 '{"stockStatus":"未知"}'
expect_json '.error | type == "string"'

request PUT "/goods/status/$goods_id" 200 '{"stockStatus":"有货"}'
expect_json '.stockStatus == "有货"'
expected_goods=$(printf '%s\n' "$expected_goods" | jq -c '.stockStatus = "有货"')

request GET /goods/stock 200
expect_json 'type == "array" and any(.[]; . == $goods)'

request DELETE "/goods/$goods_id" 200
expect_json '.goodsId == $id and .deleted == true'
created=0

request DELETE "/goods/$goods_id" 404
expect_json '.error | type == "string"'

request GET /goods/list 200
expect_json 'type == "array" and all(.[]; .goodsId != $id)'

printf '六个商品接口测试通过。\n'
