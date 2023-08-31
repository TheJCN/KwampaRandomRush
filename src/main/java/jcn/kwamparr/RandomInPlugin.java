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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class RandomInPlugin {
    private Logger logger;
    private GameManager gameManager;
    private List<Player> playerList;
    private KwampaRR plugin;
    private Clipboard clipboard;
    private List<Material> materialList;
    private String worldName;
    private List<String> structureList;

    public RandomInPlugin(GameManager gameManager, List<Player> playerList, KwampaRR plugin, List<Material> materialList, String worldName, List<String> structureList, Logger logger) {
        this.gameManager = gameManager;
        this.playerList = playerList;
        this.plugin = plugin;
        this.materialList = materialList;
        this.worldName = worldName;
        this.structureList = structureList;
        this.logger = logger;
    }

    public void RandomItem() {
        int ValueOfItems = materialList.size();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameManager.getGameState() == GameState.Active) {
                    for (Player player : playerList) {
                        Random random = new Random();
                        int randomIndex = random.nextInt(ValueOfItems);
                        int chance = random.nextInt(10);
                        if (chance == 1) {
                            player.getInventory().addItem(RandomEnchantment(materialList.get(randomIndex)));
                            return;
                        }
                        player.getInventory().addItem(new ItemStack(materialList.get(randomIndex)));
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

        Random random = new Random();
        int index = random.nextInt(structureList.size());
        File structure = new File(plugin.getDataFolder(), "Structures/" + structureList.get(index) + ".schem");

        ClipboardFormat format = ClipboardFormats.findByFile(structure);
        try (ClipboardReader reader = format.getReader(new FileInputStream(structure))) {
            clipboard = reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        com.sk89q.worldedit.world.World worldEditWorld = BukkitAdapter.adapt(Bukkit.getWorld(worldName));

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
            Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                    .to(BlockVector3.at(x, y, z)).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    public void RandomCommandBlock(Player useCommandBlock, List<Player> playerList){
        Random random = new Random();
        int RandomIndex = random.nextInt(playerList.size());
        Player randomPlayer = playerList.get(RandomIndex);
        Random randoAction = new Random();
        int RandomActionNumber = randoAction.nextInt(6);
        switch (RandomActionNumber) {
            case 1:
                RandomSpawnMob(randomPlayer, useCommandBlock);
                break;
            case 2:
                RandomSpawnLighting(randomPlayer, useCommandBlock);
                break;
            case 3:
                RandomEffectGive(randomPlayer, useCommandBlock);
                break;
            case 4:
                RandomKill(randomPlayer, useCommandBlock);
                break;
            case 5:
                RandomSwap(randomPlayer, useCommandBlock, playerList);
                break;
            case 6:
                RandomSwapInventory(randomPlayer, useCommandBlock, playerList);
                break;
        }
    }

    public void RandomSpawnMob(Player targetPlayer, Player useCommandBlock) {
        List<EntityType> entityType = new ArrayList<>(Arrays.asList(EntityType.values()));
        entityType.remove(EntityType.PLAYER);
        Iterator<EntityType> iterator = entityType.iterator();
        while (iterator.hasNext()) {
            EntityType entityType1 = iterator.next();
            if (entityType1.isSpawnable() && entityType1.isAlive()) {
                iterator.remove();
            }
        }

        Random random = new Random();
        int randomIndex = random.nextInt(entityType.size());

        EntityType randomMonsterType = entityType.get(randomIndex);

        LivingEntity monster = (LivingEntity) targetPlayer.getWorld().spawnEntity(targetPlayer.getLocation(), randomMonsterType);

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " got a random summoned mob.", ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
        }
    }

    public void RandomSpawnLighting(Player targetPlayer, Player useCommandBlock){
        World world = targetPlayer.getWorld();
        Location location = targetPlayer.getLocation();

        world.strikeLightning(location);

        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " got a random light strike", ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
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
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " got a random effect", ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
        }
    }

    public void RandomKill(Player targetPlayer, Player useCommandBlock){
        targetPlayer.damage(100);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " died from command block", ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
        }
    }

    public  void RandomSwap(Player targetPlayer, Player useCommandBlock, List<Player> playerList) {
        Random random = new Random();
        int randomIndex = random.nextInt(playerList.size());
        Player targetPlayer2 = playerList.get(randomIndex);
        Location firsplayer = targetPlayer.getLocation();
        Location secondplayer = targetPlayer2.getLocation();
        targetPlayer.teleport(secondplayer);
        targetPlayer2.teleport(firsplayer);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " swapped places with " + targetPlayer2.getName(), ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
        }
    }

    public  void RandomSwapInventory(Player targetPlayer, Player useCommandBlock, List<Player> playerList){
        Random random = new Random();
        int randomIndex = random.nextInt(playerList.size());
        Player targetPlayer2 = playerList.get(randomIndex);
        ItemStack[] firstinventory = targetPlayer.getInventory().getContents().clone();
        ItemStack[] secondinventory = targetPlayer2.getInventory().getContents().clone();
        targetPlayer.getInventory().clear();
        targetPlayer.getInventory().setContents(secondinventory);
        targetPlayer2.getInventory().clear();
        targetPlayer2.getInventory().setContents(firstinventory);
        for(Player player : Bukkit.getOnlinePlayers()){
            player.sendTitle(ChatColor.GOLD + "Player " + targetPlayer.getName() + " swapped inventory with " + targetPlayer2.getName(), ChatColor.RESET + "Command block was used by: " + useCommandBlock.getName());
        }
    }
}
