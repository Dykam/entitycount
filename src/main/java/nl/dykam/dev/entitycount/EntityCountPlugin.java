package nl.dykam.dev.entitycount;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class EntityCountPlugin extends JavaPlugin {
    AnalyticsResult results;

    @Override
    public void onEnable() {
        results = new AnalyticsResult();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equals("count")) {
            World world = args.length == 2 ? Bukkit.getWorld(args[1]) : null;
            String typeString = args.length >= 1 ? args[0].toLowerCase() : "both";
            Type type = typeString.equals("entity") ? Type.Entity : typeString.equals("tile") ? Type.TileEntity : Type.Both;
            results.analyze(type, world);
            return true;
        } else if(command.getName().equals("view")) {
            return true;
        }
        return false;
    }

    static class AnalyticsResult {
        List<ChunkResult> resultsByChunk;
        SortedMap<String, Integer> resultsByType;
        public void analyze(Type type, World world) {
            resultsByChunk = new ArrayList<>();
            if(world == null) analyzeWorlds(type);
            else analyzeWorld(type, world);

            analyzeByType();
        }

        private void analyzeByType() {
            resultsByType = new TreeMap<>();
            for (ChunkResult result : resultsByChunk) {
                for (Map.Entry<String, Integer> objectIntegerEntry : result.getCountPerType().entrySet()) {
                    String key = objectIntegerEntry.getKey();
                    Integer value = objectIntegerEntry.getValue();
                    if(resultsByType.containsKey(key)) {
                        value += resultsByType.get(key);
                    }
                    resultsByType.put(key, value);
                }
            }
        }

        private void analyzeWorlds(Type type) {
            for (World world : Bukkit.getWorlds()) {
                analyzeWorld(type, world);
            }
        }

        private void analyzeWorld(Type type, World world) {
            for (Chunk chunk : world.getLoadedChunks()) {
                ChunkResult result = new ChunkResult(chunk);
                result.analyze(type);
                resultsByChunk.add(result);
            }
        }
    }

    private static class ChunkResult {
        private final Chunk chunk;
        int countInChunk = 0;
        HashMap<String, Integer> countPerType = new HashMap<>();

        public ChunkResult(Chunk chunk) {
            this.chunk = chunk;
        }

        public void analyze(Type type) {
            if(type == Type.Both || type == Type.Entity) {
                for (Entity entity : chunk.getEntities()) {
                    String key = ChatColor.AQUA + entity.getType().toString();
                    countPerType.put(key, countPerType.containsKey(key) ? countPerType.get(key) + 1 : 1);
                    countInChunk++;
                }
            }
            if(type == Type.Both || type == Type.TileEntity) {
                for (BlockState blockState : chunk.getTileEntities()) {
                    String key = ChatColor.RED + blockState.getType().toString();
                    countPerType.put(key, countPerType.containsKey(key) ? countPerType.get(key) + 1 : 1);
                    countInChunk++;
                }
            }
        }

        public int getCountInChunk() {
            return countInChunk;
        }

        public HashMap<String, Integer> getCountPerType() {
            return countPerType;
        }
    }
}
