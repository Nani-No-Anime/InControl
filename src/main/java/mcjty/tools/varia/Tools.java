package mcjty.tools.varia;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class Tools {
    public static Pair<Float, ItemStack> parseStackWithFactor(String name, Logger logger) {
        return parseStackWithFactor(name,logger,ItemStack.EMPTY);
    }
    public static Pair<Float, ItemStack> parseStackWithFactor(String name, Logger logger,ItemStack defaultItem) {
        int i = 0;
        while (i < name.length() && (Character.isDigit(name.charAt(i)) || name.charAt(i) == '.')) {
            i++;
        }
        if (i < name.length() && name.charAt(i) == '=') {
            String f = name.substring(0, i);
            float v;
            try {
                v = Float.parseFloat(f);
            } catch (NumberFormatException e) {
                v = 1.0f;
            }
            return Pair.of(v, parseStack(name.substring(i+1), logger,defaultItem));
        }

        return Pair.of(1.0f, parseStack(name, logger,defaultItem));
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(JsonObject obj, Logger logger) {
        return parseStackWithFactor(obj,logger,ItemStack.EMPTY);
    }

    public static Pair<Float, ItemStack> parseStackWithFactor(JsonObject obj, Logger logger, ItemStack defaultItem) {
        float factor = 1.0f;
        if (obj.has("factor")) {
            factor = obj.get("factor").getAsFloat();
        }
        ItemStack stack = parseStack(obj, logger,defaultItem);
        if (stack == null) {
            return null;
        }
        return Pair.of(factor, stack);
    }
    @Nonnull
    public static ItemStack parseStack(String name, Logger logger) {
        return parseStack(name, logger,ItemStack.EMPTY);
    }

    @Nonnull
    public static ItemStack parseStack(String name, Logger logger,ItemStack defaultItem) {
        if (name.contains("/")) {
            String[] split = StringUtils.split(name, "/");
            ItemStack stack = ItemStack.EMPTY;
            if(name.equals("...")){
                stack=defaultItem;
            }else{
                stack = parseStackNoNBT(split[0], logger);
            }

            
            if (stack.isEmpty()) {
                return stack;
            }
            CompoundNBT nbt;
            try {
                nbt = JsonToNBT.getTagFromJson(split[1]);
            } catch (CommandSyntaxException e) {
                logger.log(Level.ERROR, "Error parsing NBT in '" + name + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(nbt);
            return stack;
        } else {
            return parseStackNoNBT(name, logger);
        }
    }
    public static ItemStack parseStack(JsonObject obj, Logger logger) {
        return parseStack(obj, logger,ItemStack.EMPTY);
    }

    @Nullable
    public static ItemStack parseStack(JsonObject obj, Logger logger,ItemStack defaultItem) {

        if (obj.has("empty")) {
            return ItemStack.EMPTY;
        }
        String name = obj.get("item").getAsString();
        ItemStack stack = ItemStack.EMPTY;
        if(name.equals("...")){
            stack=defaultItem;            
        }else{
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                logger.log(Level.ERROR, "Unknown item '" + name + "'!");
                return null;
            }
            stack = new ItemStack(item);
        }

        if (obj.has("damage")) {
            stack.setDamage(obj.get("damage").getAsInt());
        }
        if (obj.has("count")) {
            stack.setCount(obj.get("count").getAsInt());
        }
        if (obj.has("nbt")) {
            String nbt = obj.get("nbt").toString();
            CompoundNBT tag = null;
            try {
                tag = JsonToNBT.getTagFromJson(nbt);
            } catch (CommandSyntaxException e) {
                logger.log(Level.ERROR, "Error parsing json '" + nbt + "'!");
                return ItemStack.EMPTY;
            }
            stack.setTag(tag);
        }
        return stack;
    }

    private static ItemStack parseStackNoNBT(String name, Logger logger) {
        if (name.contains("@")) {
            String[] split = StringUtils.split(name, "@");
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
            if (item == null) {
                return ItemStack.EMPTY;
            }
            int meta = 0;
            try {
                meta = Integer.parseInt(split[1]);
            } catch (NumberFormatException e) {
                logger.log(Level.ERROR, "Unknown item '" + name + "'!");
                return ItemStack.EMPTY;
            }
            // @todo 1.15 Meta? Support properties?
            return new ItemStack(item, 1);
        } else {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item == null) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(item);
        }
    }
}
