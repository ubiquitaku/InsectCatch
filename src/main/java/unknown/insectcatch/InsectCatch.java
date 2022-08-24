package unknown.insectcatch;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class InsectCatch extends JavaPlugin implements @NotNull Listener {
    //設定ファイルの宣言
    FileConfiguration config;
    //網アイテム
    ItemStack net;
    ItemMeta netmeta;
    //木のマテリアルの名前と下の名前&種類をまとめるMap<木のマテリアル名,Map<虫の名前,確率>>
//    List<String> woods = new ArrayList<>();
    Map<String,Map<String, Integer>> woods = new HashMap<>();
    //それぞれの木ごとに虫の名前と確率を保存
    Map<String,Integer> acacia = new HashMap<>();
    Map<String,Integer> birch = new HashMap<>();
    Map<String,Integer> oak = new HashMap<>();
    Map<String,Integer> dark_oak = new HashMap<>();
    Map<String,Integer> jungle = new HashMap<>();
    Map<String,Integer> spruce = new HashMap<>();
    Map<String,Integer> crimson = new HashMap<>();
    Map<String,Integer> warped = new HashMap<>();
    //全部の虫の完成版を保存Map<虫の名前,虫の完成版>
    Map<String,ItemStack> insects = new HashMap<>();
    //乱数
    Random r = new Random();


    @Override
    public void onEnable() {
        // Plugin startup logic
        //イベントリスナ
        Bukkit.getPluginManager().registerEvents(this,this);
        //config.ymlが存在しない場合作るらしい
        saveDefaultConfig();
        //config.ymlを読み取る
        config = getConfig();
        //網アイテムの作成
        net = new ItemStack(Material.getMaterial(config.getString("item.mat")));
        netmeta = net.getItemMeta();
        netmeta.displayName(Component.text(config.getString("item.name")));
        netmeta.lore(convertLore(config.getString("item.lore")));
        net.setItemMeta(netmeta);

        setPer();
        addWoods();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("icatch")) {
            if (args.length == 0) {
                sender.sendMessage("説明めんど");
                return true;
            }
            if (args[0].equals("get")) {
                Player p = (Player) sender;
                p.getInventory().addItem(net);
            }
        }
        return true;
    }

    //原木を右クリックしたときの処理
    @EventHandler
    public void clicked(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!e.getPlayer().getInventory().getItemInMainHand().getItemMeta().displayName().equals(netmeta.displayName())) {
            return;
        }
        if (!woods.containsKey(e.getClickedBlock().getType().name())) {
            return;
        }
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        item.setAmount(item.getAmount()-1);
        Map<String,Integer> map = woods.get(e.getClickedBlock().getType().name());
        System.out.println(map.toString());
        for (String str : map.keySet()) {
            if (r.nextInt(100000) < map.get(str)) {
                e.getPlayer().getInventory().addItem(insects.get(str));
                e.getPlayer().sendMessage(Component.text("あっ虫！"));
                break;
            }
        }
        Bukkit.broadcast(Component.text("musinasi"));
    }

    //木のマテリアルの一覧を作る
    void addWoods() {
        woods.put("ACACIA_LOG",acacia);
        System.out.println(acacia.toString());
        woods.put("BIRCH_LOG",birch);
        woods.put("OAK_LOG",oak);
        woods.put("DARK_OAK_LOG",dark_oak);
        woods.put("JUNGLE_LOG",jungle);
        woods.put("SPRUCE_LOG",spruce);
        woods.put("CRIMSON_STEM",crimson);
        woods.put("WARPED_STEM",warped);
    }

    //木ごとの確率を保存
    void setPer() {
        acacia = per("ACACIA_LOG");
        birch = per("BIRCH_LOG");
        oak = per("OAK_LOG");
        dark_oak = per("DARK_OAK_LOG");
        jungle = per("JUNGLE_LOG");
        spruce = per("SPRUCE_LOG");
        crimson = per("CRIMSON_STEM");
        warped = per("WARPED_STEM");
    }

    //虫の名前と確率を取り出して保存&insectsにも追加
    Map<String,Integer> per(String name) {
        Map<String,Integer> map = new HashMap<>();
        @Nullable ConfigurationSection con = config.getConfigurationSection("woods."+name);
        for (String st : con.getKeys(false)) {
            map.put(st, (int) (con.getDouble(st+".per")*1000));
            ItemStack item = new ItemStack(Material.getMaterial(con.getString(st+".ins.mat")));
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(con.getString(st+".ins.name")));
            meta.lore(convertLore(con.getString(st+".ins.lore")));
            item.setItemMeta(meta);
            insects.put(st,item);
        }
        return map;
    }

    //componentとかいうののloreが面倒だからここで作る
    List<Component> convertLore(String lore) {
        List<Component> lo = new ArrayList<>();
        if (lore.contains("/")) {
            String[] str = lore.split("/");
            for (String st : str) {
                lo.add(Component.text(st));
            }
        } else {
            lo.add(Component.text(lore));
        }
        return lo;
    }
}
