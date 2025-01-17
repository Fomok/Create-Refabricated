package com.simibubi.create.lib.mixin.common.accessor;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RedStoneWireBlock.class)
public interface RedStoneWireBlockAccessor {
	@Invoker("shouldConnectTo")
	static boolean create$shouldConnectTo(BlockState blockState, @Nullable Direction side) {
		throw new AssertionError("Mixin application failed!");
	}
}
