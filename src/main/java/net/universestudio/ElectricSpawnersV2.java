package net.universestudio;

import io.github.thebusybiscuit.slimefun5.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun5.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun5.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun5.api.researches.Research;
import io.github.thebusybiscuit.slimefun5.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun5.implementation.SlimefunItems;
import io.github.thebusybiscuit.slimefun5.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun5.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun5.libraries.keys.NamespacedKey;
import io.github.thebusybiscuit.slimefun5.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun5.utils.SlimefunUtils;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.logging.Level;

public class ElectricSpawnersV2 extends JavaPlugin implements Listener, SlimefunAddon {

    @Override
    public void onEnable() {
        Config cfg = new Config(this);

        String HEAD_TEXTURE = "db6bd9727abb55d5415265789d4f2984781a343c68dcaf57f554a5e9aa1cd";
        ItemGroup itemGroup = new ItemGroup(new NamespacedKey(this, "electric_spawners"), CustomItemStack.create(SlimefunUtils.getCustomHead(HEAD_TEXTURE), "&9Electric Spawners"));
        Research research = new Research(new NamespacedKey(this, "electric_spawners"), 4820, "Powered Spawners", 30);
        org.bukkit.NamespacedKey textureKey = new org.bukkit.NamespacedKey(this, "electric_spawner");

        for (String mob : cfg.getStringList("mobs")) {
            try {
                EntityType type = EntityType.valueOf(mob);

                ItemStack spawnerIngredient = CustomItemStack.create(Material.SPAWNER, "&bReinforced Spawner", "&7Type: &b" + ChatUtils.humanize(type.toString()));
                ItemStack[] recipe = new ItemStack[] {
                    null, SlimefunItems.PLUTONIUM.item(), null,
                    SlimefunItems.ELECTRIC_MOTOR.item(), spawnerIngredient, SlimefunItems.ELECTRIC_MOTOR.item(),
                    SlimefunItems.BLISTERING_INGOT_3.item(), SlimefunItems.LARGE_CAPACITOR.item(), SlimefunItems.BLISTERING_INGOT_3.item()
                };

                SlimefunItemStack itemStack = new SlimefunItemStack(
                    "ELECTRIC_SPAWNER_%S".formatted(mob),
                    SlimefunUtils.getCustomHead("db6bd9727abb55d5415265789d4f2984781a343c68dcaf57f554a5e9aa1cd")
                );
//                SlimefunItemStack itemStack = new SlimefunItemStack(
//                    "ELECTRIC_SPAWNER_%S".formatted(mob),
//                    Material.TINTED_GLASS
//                );
//                itemStack.editMeta(meta -> meta.getPersistentDataContainer().set(textureKey, PersistentDataType.STRING, "true"));
                new ElectricSpawner(itemGroup, itemStack, recipe, type, research, 240, 512).register(this);
            }
            catch (IllegalArgumentException x) {
                getLogger().log(Level.WARNING, "An Error has occured while adding an Electric Spawner for the (possibly outdated or invalid) EntityType \"{0}\"", mob);
            }
            catch (Exception x) {
                getLogger().log(Level.SEVERE, x, () -> "An Error has occurred while adding an Electric Spawner for the EntityType \"" + mob + "\"");
            }
        }

        research.register();
        Slimefun.getItemTranslationService().registerTranslations(this);
    }

    @NonNull
    @Override
    public JavaPlugin getJavaPlugin() {
        return this;
    }

    @Override
    public String getBugTrackerURL() {
        return null;
    }
}
