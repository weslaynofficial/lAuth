package com.lauth;

import com.lauth.auth.AuthService;
import com.lauth.command.*;
import com.lauth.config.ConfigManager;
import com.lauth.dialog.DialogProvider;
import com.lauth.listener.JoinListener;
import com.lauth.listener.PlayerProtectionListener;
import com.lauth.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LAuth extends JavaPlugin {

    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private AuthService authService;
    private DialogProvider dialogProvider;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        configManager = new ConfigManager(this);
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.isAvailable()) {
            getLogger().severe("Veritabanı başlatılamadı. Eklenti devre dışı bırakılıyor.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        authService = new AuthService(this, databaseManager);
        dialogProvider = new DialogProvider(this, configManager);

        var pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new JoinListener(this, authService, configManager, dialogProvider), this);
        pluginManager.registerEvents(new PlayerProtectionListener(this, authService), this);

        var LAuthCmd = getCommand("LAuth");
        if (LAuthCmd != null) {
            var cmd = new LAuthCommand(configManager, authService, dialogProvider);
            LAuthCmd.setExecutor(cmd);
            LAuthCmd.setTabCompleter(cmd);
        }

        var girisCmd = getCommand("giris");
        if (girisCmd != null) girisCmd.setExecutor(new LoginCommand(this, authService, configManager));

        var kayitCmd = getCommand("kayit");
        if (kayitCmd != null) kayitCmd.setExecutor(new RegisterCommand(this, authService, configManager));

        var cikisCmd = getCommand("cikis");
        if (cikisCmd != null) cikisCmd.setExecutor(new LogoutCommand(this, authService, configManager, dialogProvider));

        var sifreCmd = getCommand("sifredegistir");
        if (sifreCmd != null) sifreCmd.setExecutor(new ChangePasswordCommand(this, authService, configManager));

        var verifyCmd = getCommand("maildogrula");
        if (verifyCmd != null) verifyCmd.setExecutor(new MailVerifyCommand(this, authService, configManager));

        var resendCmd = getCommand("mailkod");
        if (resendCmd != null) resendCmd.setExecutor(new MailResendCommand(authService, configManager));

        getLogger().info("============================lAuth============================");
        getLogger().info("LOURSA PROJECT - Giris Sistemi v1.0.4");
        getLogger().info("Mod: " + (dialogProvider.isDialogAvailable() ? "Dialog GUI" : "Chat Tabanlı"));
        getLogger().info("Şifreleme: PBKDF2-SHA256 (65536 iterasyon)");
        getLogger().info("lAuth aktif!");
        getLogger().info("============================lAuth============================");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.close();
        getLogger().info("lAuth devre disi birakildi.");
    }

    public ConfigManager getConfigManager() { return configManager; }
    public AuthService getAuthService() { return authService; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DialogProvider getDialogProvider() { return dialogProvider; }
}
