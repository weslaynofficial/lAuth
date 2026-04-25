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

public class RegisterCommand implements CommandExecutor {

    private final LAuth plugin;
    private final AuthService auth;
    private final ConfigManager config;

    public RegisterCommand(LAuth plugin, AuthService auth, ConfigManager config) {
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

        if (auth.isRegistered(player.getName())) {
            player.sendMessage(config.msgComponent("chat-register.already"));
            return true;
        }

        if (args.length < 3) {
            player.sendMessage(config.msgComponent("register.verify_usage_register"));
            return true;
        }

        AuthService.RegisterResult result = auth.startEmailRegistration(player, args[0], args[1], args[2]);

        switch (result) {
            case VERIFICATION_PENDING -> player.sendMessage(config.msgComponent("register.verification_sent"));
            case PASSWORD_MISMATCH -> player.sendMessage(config.msgComponent("register.mismatch"));
            case PASSWORD_TOO_SHORT -> {
                String msg = config.msg("register.too_short").replace("%min%", String.valueOf(auth.getMinPasswordLength()));
                player.sendMessage(config.parse(msg));
            }
            case PASSWORD_TOO_LONG -> {
                String msg = config.msg("register.too_long").replace("%max%", String.valueOf(auth.getMaxPasswordLength()));
                player.sendMessage(config.parse(msg));
            }
            case EMAIL_REQUIRED -> player.sendMessage(config.msgComponent("register.email_required"));
            case EMAIL_INVALID -> player.sendMessage(config.msgComponent("register.email_invalid"));
            case MAIL_SEND_FAILED -> player.sendMessage(config.msgComponent("register.mail_send_failed"));
            default -> player.sendMessage(config.msgComponent("register.failed"));
        }
        return true;
    }

    private void logAction(String action, Player player) {
        if (!plugin.getConfig().getBoolean("logging.enabled", true)) return;
        String format = plugin.getConfig().getString("logging." + action, "");
        if (format.isEmpty()) return;
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        format = format.replace("%player%", player.getName()).replace("%ip%", ip);
        plugin.getLogger().info(format);
    }
}
