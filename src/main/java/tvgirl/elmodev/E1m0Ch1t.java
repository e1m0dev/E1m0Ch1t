package tvgirl.elmodev;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import tvgirl.elmodev.database.Database;
import tvgirl.elmodev.database.DatabaseManager;
import tvgirl.elmodev.listener.OnPlayerChat;
import tvgirl.elmodev.listener.OnPlayersQuit;
import tvgirl.elmodev.repository.PlayerRepository;
import tvgirl.elmodev.service.PlayerService;
import tvgirl.elmodev.state.AMessageState;
import tvgirl.elmodev.state.ChatState;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class E1m0Ch1t extends JavaPlugin {

    // UTIL
    private HashSet<String> blockedAdd = new HashSet<>();
    private HashSet<String> blockedWord = new HashSet<>();

    // ABS
    private final ConcurrentHashMap<String, ChatState> chatList = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AMessageState> broadcastList = new ConcurrentHashMap<>();

    // DATABASE
    Database database = new Database(getConfig());
    DatabaseManager databaseManager;

    // Repo
    PlayerRepository repo;

    // Service
    PlayerService pService;

    // Event
    PluginManager pM = Bukkit.getPluginManager();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        database.init();
        bootstrap(); // # MAIN

        pService.systemSendAutoBroadcast();
        pService.startGlobalCooldownTask();
    }

    @Override
    public void onDisable() {
        repo.shutdown();
        database.shutdown();
    }

    // ADDITIONAL
    private void registerDatabase() {
        databaseManager.createLogTables();
        databaseManager.createIndexes();
    }

    private void registerEvents() {
        pM.registerEvents(new OnPlayerChat(this, pService), this);
        pM.registerEvents(new OnPlayersQuit(pService), this);
    }

    private void registerChats() {
        ConfigurationSection list = getConfig().getConfigurationSection("Chats");
        for(String chats : list.getKeys(false)) {
            String permission = list.getString(chats + ".permission");
//            String command = list.getString(chats + ".command"); // Защита от null, эт важно, возможно буду добавлять команды
            String prefix = list.getString(chats + ".prefix");
            int cooldown = list.getInt(chats + ".cooldown");
            String color = list.getString(chats + ".color");
            int radius = list.getInt(chats + ".radius");
            String tag = list.getString(chats + ".tag");

            Bukkit.getLogger().info("Загружен конфиг чата " + tag);
            ChatState state = new ChatState(permission, cooldown, "", prefix, radius, color, tag);
            chatList.put(chats, state);
        }
    }

    private void registerAutoMessage() {
        ConfigurationSection list = getConfig().getConfigurationSection("AutoMessage");
        for(String messages : list.getKeys(false)) {
            int cooldown = list.getInt(messages + ".cooldown");
            List<String> stringList = list.getStringList(messages + ".message");
            Bukkit.getLogger().info("Загружен конфиг AutoMessage " + messages);
            AMessageState state = new AMessageState(messages, cooldown, stringList);
            broadcastList.put(messages, state);
        }
    }

    private void registerBlockAdd() {
        ConfigurationSection list = getConfig().getConfigurationSection("AntiAdd");
        if(!getConfig().getBoolean("AntiAdd.enable")) return;

        for(String antiAdd : list.getKeys(false)) {
            List<String> add = list.getStringList(antiAdd);
            for(String s : add) {
                blockedAdd.add(s.toLowerCase()); // Переживать не стоит, это слишком дешево для функции и не требует Stream API, я не собираюсь умничать в коде чтобы потом разбирать руны.
            }
        }
    }

    private void registerBlockWords() {
        ConfigurationSection list = getConfig().getConfigurationSection("AntiWords");
        if(!getConfig().getBoolean("AntiWords.enable")) return;

        for(String bWords : list.getKeys(false)) {
            List<String> add = list.getStringList(bWords);
            for(String s : add) {
                blockedWord.add(s.toLowerCase());
            }
        }
    }

    private void bootstrap() {
        repo = new PlayerRepository(this, database.getSource());
        pService = new PlayerService(this, repo, getConfig());
        databaseManager = new DatabaseManager(database.getSource());

        repo.start();
        registerDatabase();

        registerChats();
        registerEvents();
        registerBlockAdd();
        registerBlockWords();
        registerAutoMessage();
    }

    public ConcurrentHashMap<String, ChatState> getChatList() {
        return chatList;
    }

    public ConcurrentHashMap<String, AMessageState> getBroadcastList() {
        return broadcastList;
    }

    public HashSet<String> getBlockedAdd() {
        return blockedAdd;
    }

    public HashSet<String> getBlockedWord() {
        return blockedWord;
    }
}
