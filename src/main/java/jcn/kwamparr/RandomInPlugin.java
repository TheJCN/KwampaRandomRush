package jcn.kwamparr;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomInPlugin {
    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;
    private Clipboard clipboard;
    private FileConfiguration config;
    private List<Material> materials = new ArrayList<>();

    public RandomInPlugin(GameManager gameManager, List<Player> playerList, KwampaRR plugin) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;
    }

    public void RandomItem() {
        config = plugin.getConfig();
        List<String> stringList2 = config.getStringList("ListOfAllItem");
        for (String Item : stringList2) {
            materials.add(Material.valueOf(Item));
        }
        int ValueOfItems = materials.size(); // Вычисляем размер списка materials
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameManager.getGameState() == GameState.Active) {
                    for (Player player : playerList) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(ValueOfItems);
                        int chance = random.nextInt(10);
                        if (chance == 1) {
                            player.getInventory().addItem(RandomEnchantment(materials.get(randomIndex)));

                            return;
                        }
                        player.getInventory().addItem(new ItemStack(materials.get(randomIndex)));
                    }
                } else
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0L, 100L);

    }

    public ItemStack RandomEnchantment(Material material) {
        ItemStack item = new ItemStack(material);

        Enchantment[] enchantments = Enchantment.values();

        Random random = new Random();

        Enchantment enchantment = enchantments[random.nextInt(enchantments.length)];
        int enchantmentLevel = random.nextInt(enchantment.getMaxLevel()) + 1;

        item.addUnsafeEnchantment(enchantment, enchantmentLevel);

        return item;
    }

    public void RandomStructure(Block clickedBlock) {
        Location location = clickedBlock.getLocation();
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        config = plugin.getConfig();
        List<String> structureList = config.getStringList("Structures");
        Random random = new Random();
        int index = random.nextInt(structureList.size());
        File structure = new File(plugin.getDataFolder(), "Structures/" + structureList.get(index) + ".schem");

        ClipboardFormat format = ClipboardFormats.findByFile(structure);
        try (ClipboardReader reader = format.getReader(new FileInputStream(structure))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String worldName = config.getString("WorldName");
        World bukkitWorld = Bukkit.getWorld(worldName);
        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(bukkitWorld);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(x, y, z)).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    public void RandomCommand(Player useCommandBlock, List<Player> playerList){
        Random random = new Random();
        int RandomIndex = random.nextInt(playerList.size());
        Player randomPlayer = playerList.get(RandomIndex);
        Random randoAction = new Random();
        int RandomActionNumber = randoAction.nextInt(4);
        if(RandomActionNumber == 1){
            RandomSpawnMob(randomPlayer, useCommandBlock);
        }
        else if(RandomActionNumber == 2){
            RandomSpawnLighting(randomPlayer, useCommandBlock);
        }
        else if(RandomActionNumber == 3){
            RandomEffectGive(randomPlayer, useCommandBlock);
        }
        else if(RandomActionNumber == 4){
            RandomKill(randomPlayer, useCommandBlock);
        }
    }

    public void RandomSpawnMob(Player targetPlayer, Player useCommandBlock) {
        List<EntityType> entityType = List.of(EntityType.values());
        entityType.remove(EntityType.PLAYER);

        Random random = new Random();
        int randomIndex = random.nextInt(entityType.size());

        EntityType randomMonsterType = entityType.get(randomIndex);

        LivingEntity monster = (LivingEntity) targetPlayer.getWorld().spawnEntity(targetPlayer.getLocation(), randomMonsterType);

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(ChatColor.GOLD + "Игрок " + targetPlayer.getName() + " получил случайный призванное существо", ChatColor.RESET + "В этом виноват: " + useCommandBlock.getName());
        }
    }

    public void RandomSpawnLighting(Player targetPlayer, Player useCommandBlock){
        World world = targetPlayer.getWorld();
        Location location = targetPlayer.getLocation();

        world.strikeLightning(location);

        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Игрок " + targetPlayer.getName() + " получил удар молнии", ChatColor.RESET + "В этом виноват: " + useCommandBlock.getName());
        }
    }

    public void RandomEffectGive(Player targetPlayer, Player useCommandBlock) {
        List<PotionEffectType> potionEffectsType = List.of(PotionEffectType.values());
        Random random = new Random();
        int randomIndex = random.nextInt(potionEffectsType.size());

        PotionEffectType randomEffectType = potionEffectsType.get(randomIndex);
        int randomDuration = random.nextInt(20) + 1;
        int randomAmplifier = random.nextInt(5) + 1;

        targetPlayer.addPotionEffect(new PotionEffect(randomEffectType, randomDuration * 20, randomAmplifier));

        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Игрок " + targetPlayer.getName() + " получил случайный эффект", ChatColor.RESET + "В этом виноват: " + useCommandBlock.getName());
        }
    }

    public void RandomKill(Player targetPlayer, Player useCommandBlock){
        targetPlayer.damage(100);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Игрок " + targetPlayer.getName() + " умер  от командного блока", ChatColor.RESET + "В этом виноват: " + useCommandBlock.getName());
        }

    }
}
