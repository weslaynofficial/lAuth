package com.lauth.dialog;

import com.lauth.LAuth;
import com.lauth.auth.AuthService;
import com.lauth.config.ConfigManager;
import com.lauth.util.EffectUtil;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

public final class DialogCallbacks {

    private DialogCallbacks() {}

    public static DialogActionCallback loginCallback(Plugin plugin, ConfigManager config, LoginDialogFactory loginFactory) {
        return (response, audience) -> {
            if (!(audience instanceof Player player)) return;

            String password = response.getText("password");
            if (password == null || password.isEmpty()) {
                safeShowDialog(player, loginFactory.build(player, config.msgComponent("login.empty_password")));
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                AuthService auth = ((LAuth) plugin).getAuthService();

                if (!auth.isRegistered(player.getName())) {
                    player.sendMessage(config.msgComponent("login.need_register"));
                    safeCloseDialog(player);
                    return;
                }

                if (auth.isIpBlocked(player)) {
                    player.kick(config.parse("<color:#FF6B6B>IP adresiniz geçici olarak engellendi.</color>"));
                    return;
                }

                if (auth.checkPassword(player.getName(), password)) {
                    auth.forceLogin(player);
                    player.removePotionEffect(PotionEffectType.BLINDNESS);
                    safeCloseDialog(player);
                    player.sendMessage(config.msgComponent("login.success"));

                    String lastInfo = auth.getLastLoginInfo(player.getName());
                    if (lastInfo != null) {
                        player.sendMessage(config.parse("<color:#7F1D1D>Son giris: " + lastInfo + "</color>"));
                    }

                    EffectUtil.playEffect(plugin, player, "login-success");
                    log(plugin, "login", player);
                } else {
                    int attempts = auth.incrementAttempts(player);
                    int maxAttempts = auth.getMaxAttempts();
                    log(plugin, "failed-login", player);

                    if (maxAttempts > 0 && attempts >= maxAttempts) {
                        String msg = plugin.getConfig().getString("auth.max-attempts-kick-message",
                                "<red>Çok fazla yanlış deneme!</red>");
                        log(plugin, "kick-attempts", player);
                        player.kick(config.parse(msg));
                        return;
                    }

                    EffectUtil.playEffect(plugin, player, "login-fail");

                    int remaining = auth.getRemainingAttempts(player);
                    if (remaining > 0) {
                        String attemptsMsg = config.msg("login.attempts-left")
                                .replace("%attempts%", String.valueOf(remaining));
                        safeShowDialog(player, loginFactory.build(player, config.parse(
                                config.msg("login.wrong_password") + "<br>" + attemptsMsg)));
                    } else {
                        safeShowDialog(player, loginFactory.build(player, config.msgComponent("login.wrong_password")));
                    }
                }
            });
        };
    }

    public static DialogActionCallback loginCancelCallback(ConfigManager config) {
        return (response, audience) -> {
            if (audience instanceof Player player) {
                player.kick(config.msgComponent("login.cancelled"));
            }
        };
    }

    public static DialogActionCallback registerCallback(Plugin plugin, ConfigManager config, RegisterDialogFactory registerFactory) {
        return (response, audience) -> {
            if (!(audience instanceof Player player)) return;

            String password = response.getText("password");
            String confirmPassword = response.getText("confirm_password");
            String email = response.getText("email");

            if (password == null || password.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
                safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.need_both")));
                return;
            }
            if (email == null || email.isEmpty()) {
                safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.email_required")));
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                LAuth LAuth = (LAuth) plugin;
                AuthService auth = ((LAuth) plugin).getAuthService();
                AuthService.RegisterResult result = auth.startEmailRegistration(player, password, confirmPassword, email);

                switch (result) {
                    case VERIFICATION_PENDING -> {
                        player.sendMessage(config.msgComponent("register.verification_sent"));
                        safeShowDialog(player, LAuth.getDialogProvider().getEmailVerificationFactory().build(player));
                    }
                    case ALREADY_REGISTERED -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.already")));
                    case PASSWORD_MISMATCH -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.mismatch")));
                    case PASSWORD_TOO_SHORT -> {
                        String msg = config.msg("register.too_short").replace("%min%", String.valueOf(auth.getMinPasswordLength()));
                        safeShowDialog(player, registerFactory.build(player, config.parse(msg)));
                    }
                    case PASSWORD_TOO_LONG -> {
                        String msg = config.msg("register.too_long").replace("%max%", String.valueOf(auth.getMaxPasswordLength()));
                        safeShowDialog(player, registerFactory.build(player, config.parse(msg)));
                    }
                    case PASSWORD_INVALID -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.invalid")));
                    case EMAIL_REQUIRED -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.email_required")));
                    case EMAIL_INVALID -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.email_invalid")));
                    case MAIL_SEND_FAILED -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.mail_send_failed")));
                    case IP_LIMIT -> safeShowDialog(player, registerFactory.build(player, config.parse(
                            "<color:#FF6B6B>Bu IP adresinden maksimum hesap sayısına ulaşıldı!</color>")));
                    default -> safeShowDialog(player, registerFactory.build(player, config.msgComponent("register.failed")));
                }
            });
        };
    }

    public static DialogActionCallback emailVerifyCallback(Plugin plugin, ConfigManager config, EmailVerificationDialogFactory verifyFactory) {
        return (response, audience) -> {
            if (!(audience instanceof Player player)) return;
            String code = response.getText("email_code");

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                LAuth LAuth = (LAuth) plugin;
                AuthService auth = LAuth.getAuthService();
                AuthService.VerifyResult result = auth.verifyEmailCode(player, code);

                switch (result) {
                    case SUCCESS -> {
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                        safeCloseDialog(player);
                        player.sendMessage(config.msgComponent("register.success"));
                        EffectUtil.playEffect(plugin, player, "register-success");
                        log(plugin, "register", player);
                    }
                    case INVALID_CODE -> safeShowDialog(player, verifyFactory.build(player, config.msgComponent("register.verify_invalid_code")));
                    case EXPIRED -> safeShowDialog(player, verifyFactory.build(player, config.msgComponent("register.verify_expired")));
                    case NOT_PENDING -> safeShowDialog(player, verifyFactory.build(player, config.msgComponent("register.verify_not_pending")));
                    default -> safeShowDialog(player, verifyFactory.build(player, config.msgComponent("register.failed")));
                }
            });
        };
    }

    public static DialogActionCallback emailVerifyCancelCallback(ConfigManager config) {
        return (response, audience) -> {
            if (audience instanceof Player player) {
                player.kick(config.msgComponent("register.cancelled"));
            }
        };
    }

    public static DialogActionCallback registerCancelCallback(ConfigManager config) {
        return (response, audience) -> {
            if (audience instanceof Player player) {
                player.kick(config.msgComponent("register.cancelled"));
            }
        };
    }

    public static DialogActionCallback ruleConfirmCallback(Plugin plugin, ConfigManager config) {
        return (response, audience) -> {
            if (!(audience instanceof Player player)) return;

            if (config.ruleAgreeEnabled()) {
                Boolean agreed = response.getBoolean(config.ruleAgreeKey());
                if (agreed == null || !agreed) {
                    String msg = plugin.getConfig().getString("rule.decline-kick-message",
                            "<red>Kuralları kabul etmediniz.</red>");
                    log(plugin, "rule-declined", player);
                    player.kick(config.parse(msg));
                    return;
                }
            }

            log(plugin, "rule-accepted", player);
            safeCloseDialog(player);

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                RegisterDialogFactory registerFactory = new RegisterDialogFactory(plugin, config);
                safeShowDialog(player, registerFactory.build(player));
            });
        };
    }

    private static void safeShowDialog(Player player, Object dialog) {
        try {
            player.showDialog((io.papermc.paper.dialog.Dialog) dialog);
        } catch (Throwable ignored) {}
    }

    private static void safeCloseDialog(Player player) {
        try {
            // Reflection ile closeDialog metodunu çağır
            var closeDialogMethod = player.getClass().getMethod("closeDialog");
            closeDialogMethod.invoke(player);
        } catch (Throwable ignored) {}
    }

    private static void log(Plugin plugin, String action, Player player) {
        if (!plugin.getConfig().getBoolean("logging.enabled", true)) return;
        String format = plugin.getConfig().getString("logging." + action, "");
        if (format.isEmpty()) return;
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        String date = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        format = format.replace("%player%", player.getName()).replace("%ip%", ip).replace("%date%", date);
        LAuth LAuth = (LAuth) plugin;
        AuthService auth = LAuth.getAuthService();
        int remaining = auth.getRemainingAttempts(player);
        int max = auth.getMaxAttempts();
        format = format.replace("%attempts%", String.valueOf(max > 0 ? max - remaining : 0));
        plugin.getLogger().info(format);
    }
}
