package com.lauth.command;

import com.lauth.LAuth;
import com.lauth.auth.AuthService;
import com.lauth.config.ConfigManager;
import com.lauth.util.EffectUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class MailVerifyCommand implements CommandExecutor {

    private final LAuth plugin;
    private final AuthService auth;
    private final ConfigManager config;

    public MailVerifyCommand(LAuth plugin, AuthService auth, ConfigManager config) {
        this.plugin = plugin;
        this.auth = auth;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.msgComponent("admin.player-only"));
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(config.msgComponent("register.verify_usage"));
            return true;
        }

        AuthService.VerifyResult result = auth.verifyEmailCode(player, args[0]);
        switch (result) {
            case SUCCESS -> {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                player.sendMessage(config.msgComponent("register.success"));
                EffectUtil.playEffect(plugin, player, "register-success");
            }
            case INVALID_CODE -> player.sendMessage(config.msgComponent("register.verify_invalid_code"));
            case EXPIRED -> player.sendMessage(config.msgComponent("register.verify_expired"));
            case NOT_PENDING -> player.sendMessage(config.msgComponent("register.verify_not_pending"));
            default -> player.sendMessage(config.msgComponent("register.failed"));
        }
        return true;
    }
}
