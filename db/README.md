# 数据库

- `init.sql` — 初始化库表
- MySQL 8，连接地址 `127.0.0.1:3306`，库名 `wait`

## 使用

```bash
mysql -uroot -p < init.sql
```

## 表说明

| 表 | 用途 |
| --- | --- |
| `user` | Casdoor 用户的本地影子记录，存本地业务字段（gender/status 等） |
| `user_profile` | 交友业务扩展资料（bio/birthday/tags 等），与 `user` 1:1 |

后续业务表（匹配、推荐、对话、举报等）按需追加。
