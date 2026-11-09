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
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

import java.util.logging.Level;

public class ElectricSpawnersV2 extends JavaPlugin implements Listener, SlimefunAddon {

    private static final String HEAD_TEXTURE = "db6bd9727abb55d5415265789d4f2984781a343c68dcaf57f554a5e9aa1cd";
    private static ElectricSpawnersV2 instance;
    private static int spawnInterval;
    private static int maxEntityCap;

    @Override
    public void onEnable() {
        instance = this;
        Config cfg = new Config(this);

        spawnInterval = cfg.getInt("spawner-settings.spawnInterval");
        maxEntityCap = cfg.getInt("spawner-settings.maxEntityCap");

        if (spawnInterval < 2) spawnInterval = 2;
        if (maxEntityCap <= 0) maxEntityCap = -1;

        ItemGroup itemGroup = new ItemGroup(new NamespacedKey(this, "electric_spawners"), CustomItemStack.create(SlimefunUtils.getCustomHead(HEAD_TEXTURE), "&9Electric Spawners"));
        Research research = new Research(new NamespacedKey(this, "electric_spawners"), 4820, "Powered Spawners", 30);

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
                    SlimefunUtils.getCustomHead(HEAD_TEXTURE)
                );

                new ElectricSpawner(itemGroup, itemStack, recipe, type, research, 240, 512).register(this);
            }
            catch (IllegalArgumentException _) {
                getLogger().log(Level.WARNING, "An Error has occurred while adding an Electric Spawner for the (possibly outdated or invalid) EntityType \"{0}\"", mob);
            }
            catch (Exception ex) {
                getLogger().log(Level.SEVERE, ex, () -> "An Error has occurred while adding an Electric Spawner for the EntityType \"" + mob + "\"");
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

    public static ElectricSpawnersV2 getInstance() {
        return instance;
    }

    public static int getSpawnInterval() {
        return spawnInterval;
    }

    public static int getMaxEntityCap() {
        return maxEntityCap;
    }
}
