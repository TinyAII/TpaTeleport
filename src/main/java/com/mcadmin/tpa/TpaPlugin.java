/*
 * TpaTeleport - TPA 传送插件
 * Copyright (c) 2026 TinyAII  ·  MIT License（见仓库根 LICENSE）
 *
 * 功能：请求同意传送（/传送 <玩家>）+ 反向请求（/传这里 <玩家>）+ 同意/拒绝（/同意 /拒绝 [玩家]）。
 *       防骚扰：请求超时自动取消、传送后冷却、拒绝后冷却期内禁止再请求、同一玩家最多 pending 3 个目标。
 *
 * 实现要点：
 *   - 用内存 Map 记录待处理请求（发起方→目标方+时间戳），/同意 触发 PlayerTeleportEvent 传发起方到目标方。
 *   - 防骚扰靠 config 的四个时间参数（timeout-seconds/cooldown-seconds/deny-cooldown-seconds/max-pending）。
 *   - 全程纯 Bukkit API（零 NMS）。
 *
 * 反编译恢复：源码随开发服清理丢失，本源码由已发布 jar（v1.0.0）经 CFR 0.152 反编译恢复后做开源清理
 *             （还原中文/补类头/LICENSE），逻辑与原始版一致。
 */
package com.mcadmin.tpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class TpaPlugin
extends JavaPlugin {
    private final Map<String, List<TpaRequest>> requests = new HashMap<String, List<TpaRequest>>();
    private final Map<String, Long> cooldowns = new HashMap<String, Long>();
    private final Map<String, Long> denyCooldowns = new HashMap<String, Long>();

    public void onEnable() {
        this.saveDefaultConfig();
        Bukkit.getScheduler().runTaskTimer((Plugin)this, () -> {
            long now = System.currentTimeMillis();
            int timeout = this.getConfig().getInt("timeout-seconds", 30) * 1000;
            boolean cleaned = false;
            Iterator<Map.Entry<String, List<TpaRequest>>> it = this.requests.entrySet().iterator();
            while (it.hasNext()) {
                List<TpaRequest> list = it.next().getValue();
                list.removeIf(r -> {
                    if (now - r.time > (long)timeout) {
                        Player from = Bukkit.getPlayerExact((String)r.from);
                        if (from != null) {
                            from.sendMessage("§c传送请求已超时（" + r.to + " 未处理）");
                        }
                        return true;
                    }
                    return false;
                });
                if (list.isEmpty()) {
                    it.remove();
                    continue;
                }
                cleaned = true;
            }
        }, 20L, 20L);
        String banner = " _____ _                _    ___ ___\n|_   _(_)_ __  _   _   / \\  |_ _|_ _|\n  | | | | '_ \\| | | | / _ \\  | | | |\n  | | | | | | | |_| |/ ___ \\ | | | |\n  |_| |_|_| |_|\\__, /_/   \\_\\___|___|\n               |___/\n";
        banner.lines().forEach(line -> this.getLogger().info((String)line));
        this.getLogger().info("TpaTeleport TPA 传送插件 v" + this.getDescription().getVersion() + " - TinyAII 出品");
        this.getLogger().info("/传送 <玩家> | /传这里 <玩家> | /同意 | /拒绝");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("该命令只能玩家使用");
            return true;
        }
        Player p = (Player)sender;
        switch (cmd.getName()) {
            case "传送": {
                return this.request(p, args, false);
            }
            case "传这里": {
                return this.request(p, args, true);
            }
            case "同意": {
                return this.respond(p, args, true);
            }
            case "拒绝": {
                return this.respond(p, args, false);
            }
        }
        return false;
    }

    private boolean request(Player p, String[] args, boolean here) {
        if (args.length < 1) {
            p.sendMessage("§c用法：" + (here ? "/传这里 <玩家名>" : "/传送 <玩家名>"));
            return true;
        }
        String targetName = args[0];
        Player target = Bukkit.getPlayerExact((String)targetName);
        if (target == null) {
            p.sendMessage("§c玩家 " + targetName + " 不在线");
            return true;
        }
        if (target.equals((Object)p)) {
            p.sendMessage("§c不能传送给自己");
            return true;
        }
        String denyKey = p.getName() + ":" + target.getName();
        long denyCd = this.denyCooldowns.getOrDefault(denyKey, 0L);
        if (System.currentTimeMillis() < denyCd) {
            int sec = (int)((denyCd - System.currentTimeMillis()) / 1000L);
            p.sendMessage("§c对方刚刚拒绝过你，需等 " + sec + " 秒后再请求");
            return true;
        }
        long cd = this.cooldowns.getOrDefault(p.getName(), 0L);
        if (System.currentTimeMillis() < cd) {
            int sec = (int)((cd - System.currentTimeMillis()) / 1000L);
            p.sendMessage("§c传送冷却中，还需等 " + sec + " 秒");
            return true;
        }
        List<TpaRequest> list = this.requests.computeIfAbsent(target.getName(), k -> new ArrayList<>());
        for (TpaRequest r : list) {
            if (!r.from.equals(p.getName()) || r.here != here) continue;
            p.sendMessage("§c你已经向 " + target.getName() + " 发送过请求，等对方回复或超时");
            return true;
        }
        int maxPending = this.getConfig().getInt("max-pending", 3);
        if (list.size() >= maxPending) {
            p.sendMessage("§c对方已有 " + list.size() + " 个待处理请求，稍后再试");
            return true;
        }
        TpaRequest req = new TpaRequest();
        req.from = p.getName();
        req.to = target.getName();
        req.here = here;
        req.time = System.currentTimeMillis();
        list.add(req);
        p.sendMessage("§a已向 §e" + target.getName() + " §a发送传送请求，等待对方同意...");
        this.sendRequestMessage(target, req);
        return true;
    }

    private void sendRequestMessage(Player target, TpaRequest req) {
        String action = req.here ? "想让你传送到他的身边" : "想传送到你身边";
        target.sendMessage("");
        target.sendMessage((Component)((TextComponent.Builder)Component.text().append((Component)Component.text((String)"══════════════", (TextColor)NamedTextColor.GOLD))).build());
        target.sendMessage((Component)((TextComponent.Builder)((TextComponent.Builder)Component.text().append((Component)Component.text((String)("§e" + req.from + " §7" + action)))).append((Component)Component.text((String)("  §7[距发出 " + this.getConfig().getInt("timeout-seconds", 30) + "s 超时]")))).build());
        Component accept = Component.text((String)"                    ").append(((TextComponent)Component.text((String)"[✅ 同意]", (TextColor)NamedTextColor.GREEN, (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD}).clickEvent(ClickEvent.runCommand((String)("/同意 " + req.from)))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)("点击同意 " + req.from + " 的传送请求"), (TextColor)NamedTextColor.GREEN))));
        Component deny = Component.text((String)" ").append(((TextComponent)Component.text((String)"[❌ 拒绝]", (TextColor)NamedTextColor.RED, (TextDecoration[])new TextDecoration[]{TextDecoration.BOLD}).clickEvent(ClickEvent.runCommand((String)("/拒绝 " + req.from)))).hoverEvent((HoverEventSource)HoverEvent.showText((Component)Component.text((String)("点击拒绝 " + req.from + " 的传送请求"), (TextColor)NamedTextColor.RED))));
        target.sendMessage(accept.append(deny));
        target.sendMessage((Component)((TextComponent.Builder)Component.text().append((Component)Component.text((String)"══════════════", (TextColor)NamedTextColor.GOLD))).build());
        target.sendMessage("");
    }

    private boolean respond(Player p, String[] args, boolean accept) {
        String fromName = args.length >= 1 ? args[0] : null;
        List<TpaRequest> list = this.requests.get(p.getName());
        if (list == null || list.isEmpty()) {
            p.sendMessage("§7你当前没有收到的传送请求" + (accept ? "（想请别人过来用 /传这里 <玩家名>）" : ""));
            return true;
        }
        TpaRequest req = null;
        if (fromName != null) {
            for (TpaRequest r : list) {
                if (!r.from.equals(fromName)) continue;
                req = r;
                break;
            }
            if (req == null) {
                p.sendMessage("§c没有 " + fromName + " 给你的传送请求");
                return true;
            }
        } else {
            req = list.get(list.size() - 1);
        }
        list.remove(req);
        Player from = Bukkit.getPlayerExact((String)req.from);
        if (accept) {
            if (from == null || !from.isOnline()) {
                p.sendMessage("§c发起者 " + req.from + " 已不在线");
                return true;
            }
            this.cooldowns.put(req.from, System.currentTimeMillis() + (long)this.getConfig().getInt("cooldown-seconds", 10) * 1000L);
            if (req.here) {
                this.teleportTo(from, p.getLocation());
                p.sendMessage("§a已同意 §e" + req.from + " §a传送到你身边");
                from.sendMessage("§a对方已同意，正在传送...");
            } else {
                this.teleportTo(p, from.getLocation());
                from.sendMessage("§a对方已同意，正在传送...");
                p.sendMessage("§a已同意，正在传送到 §e" + req.from + " §a身边");
            }
        } else {
            this.denyCooldowns.put(req.from + ":" + p.getName(), System.currentTimeMillis() + (long)this.getConfig().getInt("deny-cooldown-seconds", 600) * 1000L);
            p.sendMessage("§c已拒绝 §e" + req.from + " §c的传送请求");
            if (from != null && from.isOnline()) {
                from.sendMessage("§c" + p.getName() + " 拒绝了你的传送请求");
            }
        }
        return true;
    }

    private void teleportTo(Player p, Location targetLoc) {
        p.teleport(targetLoc);
        p.sendMessage("§a传送完成！");
    }

    public static class TpaRequest {
        public String from;
        public String to;
        public boolean here;
        public long time;
    }
}

