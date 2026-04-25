package com.lauth.command;

import com.lauth.LAuth;
import com.lauth.auth.AuthService;
import com.lauth.config.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {

    private final LAuth plugin;
    private final AuthService auth;
    private final ConfigManager config;

    public ChangePasswordCommand(LAuth plugin, AuthService auth, ConfigManager config) {
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

        if (!auth.isRegistered(player.getName())) {
            player.sendMessage(config.msgComponent("changepassword-self.not-registered"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(config.msgComponent("changepassword-self.usage"));
            return true;
        }

        String oldPassword = args[0];
        String newPassword = args[1];

        if (!auth.checkPassword(player.getName(), oldPassword)) {
            player.sendMessage(config.msgComponent("changepassword-self.wrong-old"));
            return true;
        }

        if (auth.changePassword(player.getName(), newPassword)) {
            player.sendMessage(config.msgComponent("changepassword-self.success"));
            plugin.getLogger().info("[lAuth] " + player.getName() + " sifresini degistirdi.");
        } else {
            player.sendMessage(config.msgComponent("register.failed"));
        }
        return true;
    }
}
