package net.buildtheearth.modules.generator.components.house.menu;

import net.buildtheearth.modules.generator.GeneratorModule;
import net.buildtheearth.modules.generator.model.Settings;
import net.buildtheearth.modules.generator.components.house.HouseFlag;
import net.buildtheearth.modules.generator.components.house.HouseSettings;
import net.buildtheearth.modules.utils.Item;
import net.buildtheearth.modules.utils.MenuItems;
import net.buildtheearth.modules.utils.menus.BlockListMenu;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class BaseColorMenu extends BlockListMenu {

    public static String BASE_TYPE_INV_NAME = "Choose a Base Floor Color";

    public BaseColorMenu(Player player) {
        super(player, BASE_TYPE_INV_NAME, MenuItems.getBlocksByColor());
    }

    @Override
    protected void setItemClickEventsAsync() {
        super.setItemClickEventsAsync();

        // Set click event for next item
        if (canProceed())
            getMenu().getSlot(NEXT_ITEM_SLOT).setClickHandler((clickPlayer, clickInformation) -> {
                Settings settings = GeneratorModule.getInstance().getHouse().getPlayerSettings().get(clickPlayer.getUniqueId());

                if (!(settings instanceof HouseSettings))
                    return;

                HouseSettings houseSettings = (HouseSettings) settings;
                houseSettings.setValue(HouseFlag.BASE_COLOR, Item.createStringFromItemList(selectedMaterials));

                clickPlayer.closeInventory();
                clickPlayer.playSound(clickPlayer.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);

                new AdvancedSettingsMenu(clickPlayer);
            });
    }
}
