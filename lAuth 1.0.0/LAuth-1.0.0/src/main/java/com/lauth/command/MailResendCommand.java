package com.lauth.command;

import com.lauth.auth.AuthService;
import com.lauth.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MailResendCommand implements CommandExecutor {

    private final AuthService auth;
    private final ConfigManager config;

    public MailResendCommand(AuthService auth, ConfigManager config) {
        this.auth = auth;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(config.msgComponent("admin.player-only"));
            return true;
        }

        if (!auth.hasPendingRegistration(player)) {
            player.sendMessage(config.msgComponent("register.verify_not_pending"));
            return true;
        }

        if (auth.resendVerificationCode(player)) {
            player.sendMessage(config.msgComponent("register.verification_sent"));
        } else {
            player.sendMessage(config.msgComponent("register.mail_send_failed"));
        }
        return true;
    }
}
