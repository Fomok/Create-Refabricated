package com.simibubi.create.foundation.data.recipe;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.world.item.Items;

public class EmptyingRecipeGen extends ProcessingRecipeGen {

	/*
	 * potion/water bottles are handled internally
	 */

	GeneratedRecipe

		HONEY_BOTTLE = create("honey_bottle", b -> b
			.require(Items.HONEY_BOTTLE)
			.output(AllFluids.HONEY.get(), 250)
			.output(Items.GLASS_BOTTLE)),

		BUILDERS_TEA = create("builders_tea", b -> b
			.require(AllItems.BUILDERS_TEA.get())
			.output(AllFluids.TEA.get(), 250)
			.output(Items.GLASS_BOTTLE))

	;

	public EmptyingRecipeGen(FabricDataGenerator p_i48262_1_) {
		super(p_i48262_1_);
	}

	@Override
	protected AllRecipeTypes getRecipeType() {
		return AllRecipeTypes.EMPTYING;
	}

}
