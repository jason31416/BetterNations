package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public abstract class RootCommand implements ICommand, IParentCommand, CommandExecutor, TabCompleter {
    public Map<String, ICommand> subCommands = new HashMap<>();
    String name;
    public RootCommand(String name) {
        this.name = name;
    }
//    public static CommandMap getCommandMap(){
//        CommandMap commandMap = null;
//        try{
//            Class targetClass = Class.forName("org.bukkit.craftbukkit.".concat(PlanetLib.packageName)+".CraftServer");
//            Method[] methods = targetClass.getDeclaredMethods();
//            for(Method method : methods){
//                method.setAccessible(true);
//                if(method.getName().equalsIgnoreCase("getCommandMap")){
//                    commandMap = (CommandMap) method.invoke(Bukkit.getServer(),new Object[0]);
//                }
//            }
//        }catch (Exception e){
//            throw new RuntimeException("Failed to get bukkit command map from NMS reflection! Likely caused by an unsupported version of Minecraft!");
//        }
//        if(commandMap==null) throw new RuntimeException("Failed to get bukkit command map from NMS reflection!");
//        return commandMap;
//    }
    public void register(){
        PluginCommand cmd = Bukkit.getPluginCommand(name);
        if(cmd!= null){
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }
//    public boolean execute(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args){
//        return onCommand(sender, args);
//    }
//
//    @NotNull
//    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
//        List<String> result = onTabComplete(sender, args);
//        if(result!= null) return result;
//        return ImmutableList.of();
//    }
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {
        CommandContext context = new CommandContext(Arrays.asList(strings), SimpleSender.of(commandSender), SimplePlayer.of(commandSender), name);
        if(context.args().isEmpty()){
            Message msg = execute(context);
            if(msg!= null) context.sender().sendMessage(msg);
            return true;
        }
        if(subCommands.containsKey(context.getArg(0))){
            ICommand subCommand = subCommands.get(context.getArg(0));
            Message msg = subCommand.execute(context);
            if(msg!= null) context.sender().sendMessage(msg);
        }else{
            context.sender().sendMessage(StaticMessages.UNKNOWN_COMMAND);
        }
        return true;
    }
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] strings){
        if(strings.length == 0){
            return null;
        }
        CommandContext context = new CommandContext(Arrays.asList(strings), SimpleSender.of(commandSender), SimplePlayer.of(commandSender), name);
        return tabComplete(context);
    }
    public void registerSubCommand(String name, ICommand command){
        subCommands.put(name, command);
    }
    @Nullable
    public abstract Message execute(ICommandContext context);
    public List<String> tabComplete(ICommandContext context) {
        if(context.args().size() == 1) {
            List<String> result = new ArrayList<>();
            for (String key : subCommands.keySet()) {
                if (key.startsWith(context.getArg(0))) {
                    result.add(key);
                }
            }
            return result;
        }else if(subCommands.containsKey(context.getArg(0))){
            ICommand subCommand = subCommands.get(context.getArg(0));
            return subCommand.tabComplete(context);
        }else{
            return null;
        }
    }
}
