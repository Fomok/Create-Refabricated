package com.simibubi.create.lib.mixin.common;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.simibubi.create.lib.extensions.BlockStateExtensions;
import com.simibubi.create.lib.util.MixinHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbEntityMixin {
	@ModifyVariable(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/block/Block;getFriction()F",
					shift = At.Shift.AFTER
			)
	)
	public float create$setSlipperiness(float g) {
		BlockPos create$pos = new BlockPos(
				MixinHelper.<ExperienceOrb>cast(this).getX(),
				MixinHelper.<ExperienceOrb>cast(this).getY(),
				MixinHelper.<ExperienceOrb>cast(this).getZ()
		);

		return ((BlockStateExtensions) MixinHelper.<ExperienceOrb>cast(this).level.getBlockState(create$pos))
				.create$getSlipperiness(MixinHelper.<ExperienceOrb>cast(this).level, create$pos, MixinHelper.<ExperienceOrb>cast(this)) * 0.98F;
	}

	@ModifyVariable(method = "award", at = @At("STORE"), ordinal = 0, argsOnly = true)
	private static int create$award(int i) {
		return i;//LivingEntityEvents.EXPERIENCE_DROP.invoker().onLivingEntityExperienceDrop(i, this.followingPlayer);
	}
}
