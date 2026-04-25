package com.lauth.config;

import com.lauth.LAuth;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigManager {

    private final LAuth plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private FileConfiguration cfg;

    public ConfigManager(LAuth plugin) {
        this.plugin = plugin;
        this.cfg = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        this.cfg = plugin.getConfig();
    }

    // Diyalog
    public boolean canCloseWithEscape() { return cfg.getBoolean("dialog.canCloseWithEscape", false); }
    public int columns() { return cfg.getInt("dialog.columns", 2); }

    // Giriş
    public Component loginTitle(Player player) {
        String raw = cfg.getString("login.title", "<white>Giriş Yap</white>");
        if (player != null) raw = raw.replace("%player_name%", player.getName());
        return mm.deserialize(raw);
    }
    public List<String> loginBodyRaw() { return cfg.getStringList("login.body"); }
    public int loginWidth() { return cfg.getInt("login.width", 150); }
    public String loginSubmitLabel() {
        var actions = cfg.getMapList("login.actions");
        if (!actions.isEmpty()) {
            Object label = actions.get(0).get("label");
            if (label != null) return label.toString();
        }
        return "<green>Giriş Yap</green>";
    }
    public String loginCancelLabel() { return cfg.getString("login.cancel-label", "<red>İptal</red>"); }

    // Kayıt
    public Component registerTitle(Player player) {
        String raw = cfg.getString("register.title", "<white>Kayıt Ol</white>");
        if (player != null) raw = raw.replace("%player_name%", player.getName());
        return mm.deserialize(raw);
    }
    public List<String> registerBodyRaw() { return cfg.getStringList("register.body"); }
    public int registerWidth() { return cfg.getInt("register.width", 150); }
    public String registerSubmitLabel() {
        var actions = cfg.getMapList("register.actions");
        if (!actions.isEmpty()) {
            Object label = actions.get(0).get("label");
            if (label != null) return label.toString();
        }
        return "<green>Kayıt Ol</green>";
    }
    public String registerCancelLabel() { return cfg.getString("register.cancel-label", "<red>İptal</red>"); }
    public boolean emailInputEnabled() { return cfg.getBoolean("register.email_input.enabled", false); }
    public String emailInputLabel() { return cfg.getString("register.email_input.label", "E-Posta"); }

    // E-posta Doğrulama
    public Component emailVerifyTitle(Player player) {
        String raw = cfg.getString("email-verify.title", "<white>E-posta Dogrulama</white>");
        if (player != null) raw = raw.replace("%player_name%", player.getName());
        return mm.deserialize(raw);
    }
    public List<String> emailVerifyBodyRaw() { return cfg.getStringList("email-verify.body"); }
    public int emailVerifyWidth() { return cfg.getInt("email-verify.width", 180); }
    public String emailVerifyInputLabel() { return cfg.getString("email-verify.input-label", "Kod"); }
    public String emailVerifySubmitLabel() { return cfg.getString("email-verify.submit-label", "<green>Dogrula</green>"); }
    public String emailVerifyCancelLabel() { return cfg.getString("email-verify.cancel-label", "<red>Iptal</red>"); }

    // Kurallar
    public boolean ruleEnabled() { return cfg.getBoolean("rule.enabled", true); }
    public Component ruleTitle() { return mm.deserialize(cfg.getString("rule.title", "<white>Sunucu Kuralları</white>")); }
    public List<String> ruleBodyRaw() { return cfg.getStringList("rule.body"); }
    public String ruleConfirmLabel() { return cfg.getString("rule.confirmButton", "<green>Onayla</green>"); }
    public boolean ruleAgreeEnabled() { return cfg.getBoolean("rule.agree.enabled", true); }
    public String ruleAgreeKey() { return cfg.getString("rule.agree.key", "agree"); }
    public String ruleAgreeLabel() { return cfg.getString("rule.agree.label", "<gray>Kuralları kabul ediyorum</gray>"); }

    // Mesajlar
    public String msg(String path) {
        String value = cfg.getString("messages." + path);
        if (value != null) return value;

        // Eski config dosyaları için geriye uyumluluk
        if (path.startsWith("register.")) {
            String fallback = cfg.getString("messages.register.failed");
            if (fallback != null) return fallback;
        } else if (path.startsWith("login.")) {
            String fallback = cfg.getString("messages.login.wrong_password");
            if (fallback != null) return fallback;
        }
        return "<red>Mesaj bulunamadi: " + path + "</red>";
    }
    public Component msgComponent(String path) { return mm.deserialize(msg(path)); }
    public Component parse(String miniMessageString) { return mm.deserialize(miniMessageString); }
}
