package com.simibubi.create.foundation.block.connected;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.block.render.SpriteShifter;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

public class CTSpriteShifter extends SpriteShifter {

	public static CTSpriteShiftEntry getCT(CTType type, ResourceLocation blockTexture, ResourceLocation connectedTexture) {
		// TODO remove
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			return null;
		}

		String key = type.name() + ":" + blockTexture + "->" + connectedTexture;
		if (ENTRY_CACHE.containsKey(key))
			return (CTSpriteShiftEntry) ENTRY_CACHE.get(key);

		CTSpriteShiftEntry entry = create(type);
		entry.set(blockTexture, connectedTexture);
		ENTRY_CACHE.put(key, entry);
		return entry;
	}

	public static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
		return getCT(type, Create.asResource("block/" + blockTextureName), Create.asResource("block/" + connectedTextureName + "_connected"));
	}

	public static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
		return getCT(type, blockTextureName, blockTextureName);
	}

	private static CTSpriteShiftEntry create(CTType type) {
		switch (type) {
		case HORIZONTAL:
			return new CTSpriteShiftEntry.Horizontal(false);
		case HORIZONTAL_KRYPPERS:
			return new CTSpriteShiftEntry.Horizontal(true);
		case OMNIDIRECTIONAL:
			return new CTSpriteShiftEntry.Omnidirectional();
		case VERTICAL:
			return new CTSpriteShiftEntry.Vertical();
		case CROSS:
			return new CTSpriteShiftEntry.Cross();
		case RECTANGLE:
			return new CTSpriteShiftEntry.Rect();
		default:
			return null;
		}
	}

	public enum CTType {
		OMNIDIRECTIONAL, HORIZONTAL, VERTICAL, CROSS, RECTANGLE, HORIZONTAL_KRYPPERS;
	}

}
