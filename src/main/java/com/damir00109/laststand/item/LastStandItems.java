package com.damir00109.laststand.item;

import com.damir00109.laststand.LastStand;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LastStandItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LastStand.MOD_ID);

    public static final DeferredItem<Item> BANDAGE = ITEMS.register("bandage",
            () -> new BandageItem(new Item.Properties().stacksTo(4)));

    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(BANDAGE);
        }
    }
}
