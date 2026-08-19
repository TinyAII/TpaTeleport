# TPA 传送 TpaTeleport

> 请求同意传送 + 反向 + 防骚扰，零依赖，Paper 1.18+。MIT 开源。

TPA 传送插件：玩家发起传送请求，对方同意才传送；支持反向请求（tpahere）；防骚扰（请求超时、传送冷却、拒绝后冷却禁请求、同时间最多 pending 3 个目标）。

- 📨 **请求传送**：`/传送 <玩家>` 请求传送到对方身边
- 🤝 **反向请求**：`/传这里 <玩家>` 请求对方传送到你身边
- ✅ **同意 / 拒绝**：`/同意 [玩家]` / `/拒绝 [玩家]`
- ⏳ **请求超时**：发出后对方 N 秒不处理自动取消
- ❄️ **传送冷却**：传送完成后 N 秒内不能再请求
- 🚫 **拒后冷却**：被拒绝后 N 秒内不能再请求该玩家
- 📋 **防刷**：同一玩家同时最多 pending 3 个目标（防刷屏骚扰全服）
- 🎨 品牌横幅 TinyAII；**MIT 开源**

---

## 安装

1. 下载 `tpa-teleport-1.0.0.jar`
2. 放入 `plugins/`，重启

## 命令

| 命令 | 别名 | 说明 |
|---|---|---|
| `/传送 <玩家>` | `/tpa` | 请求传送到对方身边 |
| `/传这里 <玩家>` | `/tpahere` | 请求对方传送到你身边（反向） |
| `/同意 [玩家]` | `/tpy`, `/tpaccept` | 同意最近的传送请求 |
| `/拒绝 [玩家]` | `/tpn`, `/tpdeny` | 拒绝最近的传送请求 |

## 配置（`plugins/TpaTeleport/config.yml`）

```yaml
timeout-seconds: 30        # 请求超时（秒）
cooldown-seconds: 10        # 传送后冷却（秒）
deny-cooldown-seconds: 600 # 拒绝后禁止再请求（秒）
max-pending: 3              # 同时间最多 pending 请求数
```

## 实现原理（开源可读）

- 内存 Map 记录待处理请求（发起方→目标方 + 时间戳），`/同意` 触发 PlayerTeleportEvent 传送发起方到目标方
- 防骚扰靠 config 四个时间参数（timeout / cooldown / deny-cooldown / max-pending）
- 纯 Bukkit API（零 NMS）

## 兼容

- Paper / Spigot / Purpur / Leaves 1.18+（建议 1.18+；纯 Bukkit 也支持 1.13+）
- Java 21
- 零依赖

## 开源许可

**MIT License** — Copyright (c) 2026 TinyAII。源码见 `src/main/java/com/mcadmin/tpa/`，可自由使用/修改/分发，请保留版权与许可声明。

---

# TpaTeleport (English)

Request-accept teleport + reverse + anti-harassment. MIT open source, zero deps, Paper 1.18+.

## Commands
`/传送 <player>` (/tpa) · `/传这里 <player>` (/tpahere) · `/同意 [player]` (/tpy) · `/拒绝 [player]` (/tpn)

## Config
```yaml
timeout-seconds: 30
cooldown-seconds: 10
deny-cooldown-seconds: 600
max-pending: 3
```

## Compatibility
Paper / Spigot / Purpur / Leaves 1.18+, Java 21, zero dependencies

## License
**MIT** — Copyright (c) 2026 TinyAII. Source in `src/`. Free to use/modify/distribute; keep the copyright notice.

## Author
TinyAII · MIT 开源 · 零依赖
