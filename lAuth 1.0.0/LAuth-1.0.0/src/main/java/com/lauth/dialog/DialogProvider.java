package com.lauth.dialog;

import com.lauth.LAuth;
import com.lauth.auth.AuthService;
import com.lauth.config.ConfigManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Client sürümüne göre otomatik mod seçimi:
 * - 1.21.5+ client (protocol >= 770) → Dialog GUI
 * - Eski client → Chat mesajları + /giris, /kayit komutları
 *
 * Sunucu 1.21.11'de çalışır, ViaVersion ile eski clientlar bağlanır.
 */
public class DialogProvider {

    private final LAuth plugin;
    private final ConfigManager config;
    private final boolean serverHasDialogApi;
    private final MiniMessage mm = MiniMessage.miniMessage();

    // Minecraft 1.21.5 = protocol 770 (Dialog desteği başladığı sürüm)
    private static final int DIALOG_MIN_PROTOCOL = 770;

    private LoginDialogFactory loginFactory;
    private RegisterDialogFactory registerFactory;
    private RuleDialogFactory ruleFactory;
    private EmailVerificationDialogFactory emailVerificationFactory;

    public DialogProvider(LAuth plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.serverHasDialogApi = checkDialogApi();

        if (serverHasDialogApi) {
            loginFactory = new LoginDialogFactory(plugin, config);
            registerFactory = new RegisterDialogFactory(plugin, config);
            ruleFactory = new RuleDialogFactory(plugin, config);
            emailVerificationFactory = new EmailVerificationDialogFactory(plugin, config);
            plugin.getLogger().info("Dialog API mevcut - client sürümüne göre otomatik mod");
        } else {
            plugin.getLogger().info("Dialog API bulunamadı - tüm oyuncular chat moduyla giriş yapacak");
        }
    }

    private boolean checkDialogApi() {
        try {
            Class.forName("io.papermc.paper.dialog.Dialog");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Oyuncunun client sürümü Dialog destekliyor mu?
     */
    public boolean supportsDialog(Player player) {
        if (!serverHasDialogApi) return false;
        try {
            int protocol = player.getProtocolVersion();
            return protocol >= DIALOG_MIN_PROTOCOL;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isDialogAvailable() {
        return serverHasDialogApi;
    }

    public String getPlayerMode(Player player) {
        return supportsDialog(player) ? "Dialog GUI" : "Chat";
    }

    // ===== Login =====

    public void showLogin(Player player) {
        if (supportsDialog(player)) {
            try { player.showDialog(loginFactory.build(player)); } catch (Exception ignored) {}
        } else {
            sendChatLogin(player, null);
        }
    }

    public void showLogin(Player player, net.kyori.adventure.text.Component errorMsg) {
        if (supportsDialog(player)) {
            try { player.showDialog(loginFactory.build(player, errorMsg)); } catch (Exception ignored) {}
        } else {
            sendChatLogin(player, errorMsg);
        }
    }

    // ===== Register =====

    public void showRegister(Player player) {
        if (supportsDialog(player)) {
            try { player.showDialog(registerFactory.build(player)); } catch (Exception ignored) {}
        } else {
            sendChatRegister(player, null);
        }
    }

    public void showRegister(Player player, net.kyori.adventure.text.Component errorMsg) {
        if (supportsDialog(player)) {
            try { player.showDialog(registerFactory.build(player, errorMsg)); } catch (Exception ignored) {}
        } else {
            sendChatRegister(player, errorMsg);
        }
    }

    public void showEmailVerification(Player player) {
        if (supportsDialog(player)) {
            try { player.showDialog(emailVerificationFactory.build(player)); } catch (Exception ignored) {}
        } else {
            player.sendMessage(mm.deserialize("<color:#FFB3C1>Dogrulama kodunu girin: /maildogrula <kod></color>"));
        }
    }

    public void showEmailVerification(Player player, net.kyori.adventure.text.Component errorMsg) {
        if (supportsDialog(player)) {
            try { player.showDialog(emailVerificationFactory.build(player, errorMsg)); } catch (Exception ignored) {}
        } else {
            player.sendMessage(errorMsg);
            player.sendMessage(mm.deserialize("<color:#FFB3C1>Dogrulama kodunu girin: /maildogrula <kod></color>"));
        }
    }

    // ===== Rules =====

    public void showRules(Player player) {
        if (supportsDialog(player)) {
            try { player.showDialog(ruleFactory.build(player)); } catch (Exception ignored) {}
        } else {
            sendChatRules(player);
        }
    }

    // ===== Close =====

    public void closeDialog(Player player) {
        if (supportsDialog(player)) {
            try {
                var closeDialogMethod = player.getClass().getMethod("closeDialog");
                closeDialogMethod.invoke(player);
            } catch (Throwable ignored) {}
        }
    }

    // ===== Chat Fallback =====

    private void sendChatLogin(Player player, net.kyori.adventure.text.Component error) {
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("  <color:#FF4D6D><bold>ʟᴏᴜʀꜱᴀ</bold></color> <color:#7F1D1D>│</color> <color:#FF6B6B>Giris Yap</color>"));
        player.sendMessage(mm.deserialize(""));
        if (error != null) {
            player.sendMessage(mm.deserialize("  <color:#FF6B6B>!</color> ").append(error));
            player.sendMessage(mm.deserialize(""));
        }
        player.sendMessage(mm.deserialize("  <color:#FFD1D1>Sifrenizi girmek icin yazin:</color>"));
        player.sendMessage(mm.deserialize("  <color:#FF6B6B><bold>/giris <sifreniz></bold></color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
    }

    private void sendChatRegister(Player player, net.kyori.adventure.text.Component error) {
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("  <color:#FF4D6D><bold>ʟᴏᴜʀꜱᴀ</bold></color> <color:#7F1D1D>│</color> <color:#FFB3C1>Kayit Ol</color>"));
        player.sendMessage(mm.deserialize(""));
        if (error != null) {
            player.sendMessage(mm.deserialize("  <color:#FF6B6B>!</color> ").append(error));
            player.sendMessage(mm.deserialize(""));
        }
        player.sendMessage(mm.deserialize("  <color:#FFD1D1>Hesap olusturmak icin yazin:</color>"));
        player.sendMessage(mm.deserialize("  <color:#FF6B6B><bold>/kayit <sifre> <sifre tekrar></bold></color>"));
        player.sendMessage(mm.deserialize(""));
        int min = plugin.getConfig().getInt("auth.min-password-length", 4);
        player.sendMessage(mm.deserialize("  <color:#7F1D1D>Minimum " + min + " karakter</color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
    }

    private void sendChatRules(Player player) {
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("  <color:#FF4D6D><bold>ʟᴏᴜʀꜱᴀ</bold></color> <color:#7F1D1D>│</color> <color:#FFB3C1>Sunucu Kurallari</color>"));
        player.sendMessage(mm.deserialize(""));
        for (String line : config.ruleBodyRaw()) {
            player.sendMessage(mm.deserialize("  " + line));
        }
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("  <color:#FFD1D1>Kurallari kabul edip kayit olmak icin:</color>"));
        player.sendMessage(mm.deserialize("  <color:#FF6B6B><bold>/kayit <sifre> <sifre tekrar></bold></color>"));
        player.sendMessage(mm.deserialize(""));
        player.sendMessage(mm.deserialize("<color:#1A2B6B><st>                                                            </st></color>"));
        player.sendMessage(mm.deserialize(""));
    }

    public LoginDialogFactory getLoginFactory() { return loginFactory; }
    public RegisterDialogFactory getRegisterFactory() { return registerFactory; }
    public RuleDialogFactory getRuleFactory() { return ruleFactory; }
    public EmailVerificationDialogFactory getEmailVerificationFactory() { return emailVerificationFactory; }
}
