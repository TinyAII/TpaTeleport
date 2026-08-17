# TPA 传送 TpaTeleport

[![Paper](https://img.shields.io/badge/Paper-1.18%2B-brightgreen)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.java.com)
[![Version](https://img.shields.io/badge/TpaTeleport-v1.0.0-blue)](https://github.com/TinyAII/TpaTeleport/releases)

请求同意式玩家传送插件：A 请求传到 B、B 聊天框点按钮同意/拒绝，支持反向传送 + 全套防骚扰机制。

## 功能特性

- 🚀 **请求传送**：`/传送 <玩家>` 请求传送到对方身边，对方同意后执行
- 🔄 **反向传送**：`/传这里 <玩家>` 让对方传送到你身边（同样需对方同意）
- 🖱️ **聊天框按钮**：收到请求显示 `[✅ 同意] [❌ 拒绝]`，鼠标点击即可处理；也可用指令 `/同意` `/拒绝`
- ⏱️ **防骚扰全家桶**：
  - 30 秒超时自动取消（config 可调）
  - 传送后 10 秒冷却（防连刷）
  - 重复请求拦截（同一目标只允许一条待处理）
  - 每人最多 3 条待处理请求（防刷屏轰炸）
  - 被拒绝后 10 分钟不能再请求同一人（防报复性骚扰）
- ✅ **在线校验**：目标不在线直接提示，不会白等

## 安装

1. 下载 `tpa-teleport-1.0.0.jar`
2. 放入服务器 `plugins/` 目录
3. 重启服务器或执行 `reload`
4. 完成！无任何前置依赖

## 命令

| 命令 | 说明 |
| --- | --- |
| `/传送 <玩家名>`（/tpa） | 请求传送到对方身边 |
| `/传这里 <玩家名>`（/tpahere） | 请求对方传到你身边（反向） |
| `/同意 [玩家名]`（/tpy） | 同意请求（多个请求时可指定玩家名） |
| `/拒绝 [玩家名]`（/tpn） | 拒绝请求 |

## 配置（plugins/TpaTeleport/config.yml）

```yaml
timeout-seconds: 30        # 请求超时（秒）
cooldown-seconds: 10       # 传送后冷却（秒）
deny-cooldown-seconds: 600 # 被拒绝后禁止再请求（秒）
max-pending: 3             # 每人同时最多待处理请求数
```

## 兼容性

- Paper / Spigot / Purpur / Leaves 1.18+
- Java 17+
- 无任何前置依赖

## 作者

TinyAII 工作室

<details>
<summary>🇬🇧 English Version (click to expand)</summary>

# TPA Teleport

Request-based player teleport plugin: A requests to teleport to B, B accepts/denies via clickable chat buttons. Includes reverse teleport (tpahere) and full anti-harassment protection.

## Features

- 🚀 **TPA**: `/tpa <player>` request to teleport to a player, executes after they accept
- 🔄 **TPAHere**: `/tpahere <player>` request a player to teleport to you (also requires consent)
- 🖱️ **Clickable buttons**: `[✅ Accept] [❌ Deny]` in chat, or use `/tpy` `/tpn` commands
- ⏱️ **Anti-harassment**: 30s timeout, 10s cooldown, duplicate request blocking, max 3 pending per player, 10-min block after denial
- ✅ **Online check**: instant feedback if target is offline

## Commands

| Command | Description |
| --- | --- |
| `/传送 <player>` (/tpa) | Request teleport to a player |
| `/传这里 <player>` (/tpahere) | Request a player to teleport to you |
| `/同意 [player]` (/tpy) | Accept a request |
| `/拒绝 [player]` (/tpn) | Deny a request |

## Compatibility

- Paper / Spigot / Purpur / Leaves 1.18+
- Java 17+
- No dependencies

## Author

TinyAII Studio

</details>
