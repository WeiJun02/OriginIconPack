package com.origin.icon.pack;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.origin.icon.pack.R;import com.origin.icon.pack.util.Utils;

public class LauncherIntents {

    public LauncherIntents(Context context, String key) throws IllegalArgumentException {
        switch (key) {
            case "com.actionlauncher.playstore":
            case "Action":
                ActionLauncher(context);
                break;
            case "org.adw.launcher":
            case "Adw":
                AdwLauncher(context);
                break;
            case "org.adwfreak.launcher":
            case "Adwex":
                AdwEXLauncher(context);
                break;
            case "com.anddoes.launcher":
            case "Apex":
                ApexLauncher(context);
                break;
            case "com.dlto.atom.launcher":
            case "Atom":
                AtomLauncher(context);
                break;
            case "com.tul.aviate":
            case "Aviate":
                AviateLauncher(context);
                break;
            case "org.cyanogenmod.theme.chooser":
            case "org.cyanogenmod.theme.chooser2":
            case "com.cyngn.theme.chooser":
            case "Cmthemeengine":
                CMThemeEngine(context);
                break;
            case "com.gau.go.launcherex":
            case "Go":
                GoLauncher(context);
                break;
            case "com.mobint.hololauncher":
            case "Holo":
                HoloLauncher(context);
                break;
            case "com.mobint.hololauncher.hd":
            case "Holoics":
                HoloLauncherICS(context);
                break;
            case "com.kk.launcher":
            case "KK":
            case "Kk":
                KkLauncher(context);
                break;
            case "com.lge.launcher2":
            case "Lghome":
                LgHomeLauncher(context);
                break;
            case "com.l.launcher":
            case "L":
                LLauncher(context);
                break;
            case "com.powerpoint45.launcher":
            case "Lucid":
                LucidLauncher(context);
                break;
            case "com.jiubang.go.mini.launcher":
            case "Mini":
                MiniLauncher(context);
                break;
            case "com.gtp.nextlauncher":
            case "Next":
                NextLauncher(context);
                break;
            case "com.teslacoilsw.launcher":
            case "Nova":
                NovaLauncher(context);
                break;
            case "com.s.launcher":
            case "S":
                SLauncher(context);
                break;
            case "ginlemon.flowerfree":
            case "Smart":
                SmartLauncher(context);
                break;
            case "ginlemon.flowerpro":
            case "Smartpro":
                SmartLauncherPro(context);
                break;
            case "home.solo.launcher.free":
            case "Solo":
                SoloLauncher(context);
                break;
            case "com.tsf.shell":
            case "Tsf":
                TsfLauncher(context);
                break;
            case "sg.ruqqq.IconThemer":
            case "Uniconpro":
                Unicon(context);
                break;
            default:
                Log.e("No launcher: ", key);
                throw new IllegalArgumentException("Couldn't find method for launcher or package: "
                        + key);
        }
    }

    private void ActionLauncher(Context context) {
        Intent action = context.getPackageManager().getLaunchIntentForPackage("com.actionlauncher" +
                ".playstore");
        action.putExtra("apply_icon_pack", context.getPackageName());
        context.startActivity(action);
    }

    private void AdwLauncher(Context context) {
        Intent intent = new Intent("org.adw.launcher.SET_THEME");
        intent.putExtra("org.adw.launcher.theme.NAME", context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void AdwEXLauncher(Context context) {
        Intent intent = new Intent("org.adwfreak.launcher.SET_THEME");
        intent.putExtra("org.adwfreak.launcher.theme.NAME", context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void ApexLauncher(Context context) {
        Intent intent = new Intent("com.anddoes.launcher.SET_THEME");
        intent.putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void AtomLauncher(Context context) {
        Intent atom = new Intent("com.dlto.atom.launcher.intent.action.ACTION_VIEW_THEME_SETTINGS");
        atom.setPackage("com.dlto.atom.launcher");
        atom.putExtra("packageName", context.getPackageName());
        atom.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(atom);
    }

    private void AviateLauncher(Context context) {
        Intent aviate = new Intent("com.tul.aviate.SET_THEME");
        aviate.setPackage("com.tul.aviate");
        aviate.putExtra("THEME_PACKAGE", context.getPackageName());
        aviate.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(aviate);
    }

    private void CMThemeEngine(Context context) {
        boolean themesAppInstalled = true;
        Intent intent = new Intent("android.intent.action.MAIN");

        if (Utils.INSTANCE.isAppInstalled(context, "org.cyanogenmod.theme.chooser")) {
            intent.setComponent(new ComponentName("org.cyanogenmod.theme.chooser",
                    "org.cyanogenmod.theme.chooser.ChooserActivity"));
        } else if (Utils.INSTANCE.isAppInstalled(context, "org.cyanogenmod.theme.chooser2")) {
            intent.setComponent(new ComponentName("org.cyanogenmod.theme.chooser2",
                    "org.cyanogenmod.theme.chooser2.ChooserActivity"));
        } else if (Utils.INSTANCE.isAppInstalled(context, "com.cyngn.theme.chooser")) {
            intent.setComponent(new ComponentName("com.cyngn.theme.chooser",
                    "com.cyngn.theme.chooser.ChooserActivity"));
        } else {
            themesAppInstalled = false;
        }

        if (themesAppInstalled) {
            intent.putExtra("pkgName", context.getPackageName());
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Impossible to open themes app.",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "Themes app is not installed in this device.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void GoLauncher(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage("com.gau.go" +
                ".launcherex");
        Intent go = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
        go.putExtra("type", 1);
        go.putExtra("pkgname", context.getPackageName());
        context.sendBroadcast(go);
        context.startActivity(intent);
    }

    private void HoloLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.mobint.hololauncher", "com.mobint.hololauncher" +
                ".Settings"));
        context.startActivity(intent);
    }

    private void HoloLauncherICS(Context context) {
        Intent holohdApply = new Intent(Intent.ACTION_MAIN);
        holohdApply.setComponent(new ComponentName("com.mobint.hololauncher.hd", "com.mobint" +
                ".hololauncher.SettingsActivity"));
        context.startActivity(holohdApply);
    }

    private void KkLauncher(Context context) {
        Intent kkApply = new Intent("com.kk.launcher.APPLY_ICON_THEME");
        kkApply.putExtra("com.kk.launcher.theme.EXTRA_PKG", context.getPackageName());
        kkApply.putExtra("com.kk.launcher.theme.EXTRA_NAME", context.getResources().getString(R
                .string.app_name));
        context.startActivity(kkApply);
    }

    private void LgHomeLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.lge.launcher2", "com.lge.launcher2" +
                ".homesettings.HomeSettingsPrefActivity"));
        context.startActivity(intent);
    }

    private void LLauncher(Context context) {
        Intent l = new Intent("com.l.launcher.APPLY_ICON_THEME", null);
        l.putExtra("com.l.launcher.theme.EXTRA_PKG", context.getPackageName());
        context.startActivity(l);
    }

    private void LucidLauncher(Context context) {
        Intent lucidApply = new Intent("com.powerpoint45.action.APPLY_THEME", null);
        lucidApply.putExtra("icontheme", context.getPackageName());
        context.startActivity(lucidApply);
    }

    private void MiniLauncher(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.jiubang.go.mini.launcher", "com.jiubang.go" +
                ".mini.launcher.setting.MiniLauncherSettingActivity"));
        context.startActivity(intent);
    }

    private void NextLauncher(Context context) {
        Intent nextApply = context.getPackageManager().getLaunchIntentForPackage("com.gtp" +
                ".nextlauncher");
        if (nextApply == null) {
            nextApply = context.getPackageManager().getLaunchIntentForPackage("com.gtp" +
                    ".nextlauncher.trial");
        }
        Intent next = new Intent("com.gau.go.launcherex.MyThemes.mythemeaction");
        next.putExtra("type", 1);
        next.putExtra("pkgname", context.getPackageName());
        context.sendBroadcast(next);
        context.startActivity(nextApply);
    }

    private void NovaLauncher(Context context) {
        Intent intent = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
        intent.setPackage("com.teslacoilsw.launcher");
        intent.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
        intent.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE", context
                .getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void SLauncher(Context context) {
        Intent s = new Intent("com.s.launcher.APPLY_ICON_THEME");
        s.putExtra("com.s.launcher.theme.EXTRA_PKG", context.getPackageName());
        s.putExtra("com.s.launcher.theme.EXTRA_NAME", context.getResources().getString(R.string
                .app_name));
        context.startActivity(s);

    }

    private void SmartLauncher(Context context) {
        Intent smartlauncherIntent = new Intent("ginlemon.smartlauncher.setGSLTHEME");
        smartlauncherIntent.putExtra("package", context.getPackageName());
        context.startActivity(smartlauncherIntent);
    }

    private void SmartLauncherPro(Context context) {
        Intent smartlauncherproIntent = new Intent("ginlemon.smartlauncher.setGSLTHEME");
        smartlauncherproIntent.putExtra("package", context.getPackageName());
        context.startActivity(smartlauncherproIntent);
    }

    private void SoloLauncher(Context context) {
        Intent soloApply = context.getPackageManager().getLaunchIntentForPackage("home.solo" +
                ".launcher.free");
        Intent solo = new Intent("home.solo.launcher.free.APPLY_THEME");
        solo.putExtra("EXTRA_PACKAGENAME", context.getPackageName());
        solo.putExtra("EXTRA_THEMENAME", context.getString(R.string.app_name));
        context.sendBroadcast(solo);
        context.startActivity(soloApply);
    }

    private void TsfLauncher(Context context) {
        Intent tsfApply = context.getPackageManager().getLaunchIntentForPackage("com.tsf.shell");
        Intent tsf = new Intent("android.intent.action.MAIN");
        tsf.setComponent(new ComponentName("com.tsf.shell", "com.tsf.shell.ShellActivity"));
        context.sendBroadcast(tsf);
        context.startActivity(tsfApply);
    }

    private void Unicon(Context context) {
        Intent unicon = new Intent("android.intent.action.MAIN");
        unicon.addCategory("android.intent.category.LAUNCHER");
        unicon.setPackage("sg.ruqqq.IconThemer");
        context.startActivity(unicon);
    }

}
