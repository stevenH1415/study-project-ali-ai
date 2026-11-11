# SQL 目录（PostgreSQL）

- 数据库：`springali` @ `localhost:5432`（用户 `postgres`）
- 已执行：`01-schema.sql`
  - `sys_user`：系统用户
  - `chat_conversation`：AI 聊天会话
  - `chat_message`：AI 聊天消息
- 演示账号由 `mm-backend` 启动时自动写入：`admin / 123456`（BCrypt 存储）

重新初始化：

```powershell
$env:PGPASSWORD='826118'
D:\soft\PostgreSQL\10\bin\psql.exe -h localhost -p 5432 -U postgres -d springali -f E:\学习\SpringAli\sql\01-schema.sql
```
