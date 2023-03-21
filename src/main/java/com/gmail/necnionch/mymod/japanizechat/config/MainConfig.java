package com.gmail.necnionch.mymod.japanizechat.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class MainConfig {
    private final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public final ForgeConfigSpec.ConfigValue<String> format = builder.define("format", "§f<{player}§f> §f{message}");
    public final ForgeConfigSpec.ConfigValue<String> formatJapanize = builder.define("format-japanize", "{japanized} §6({message}§6)");
    public final ForgeConfigSpec.ConfigValue<Boolean> enableJapanizeDefault = builder.comment("not implemented").define("enable-japanize-default", true);
    public final ForgeConfigSpec config = builder.build();

    public MainConfig() {

    }

}
