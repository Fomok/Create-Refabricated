package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.math.Vector3d;
import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import com.simibubi.create.lib.item.CustomDurabilityBarItem;

import com.simibubi.create.lib.utility.EnchantmentUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class PotatoCannonItem extends ProjectileWeaponItem implements CustomDurabilityBarItem {

	public static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
	public static final int MAX_DAMAGE = 100;

	public PotatoCannonItem(Properties properties) {
		super(properties);
		EnchantmentUtil.addCompat(this, Enchantments.POWER_ARROWS,
				Enchantments.PUNCH_ARROWS,
				Enchantments.FLAMING_ARROWS,
				Enchantments.MOB_LOOTING,
				AllEnchantments.POTATO_RECOVERY.get());
	}

	@Override
	public boolean canAttackBlock(BlockState p_195938_1_, Level p_195938_2_, BlockPos p_195938_3_,
		Player p_195938_4_) {
		return false;
	}

//	@Override
//	public boolean canEnchant(ItemStack stack, Enchantment enchantment) {
//		if (enchantment == Enchantments.POWER_ARROWS)
//			return true;
//		if (enchantment == Enchantments.PUNCH_ARROWS)
//			return true;
//		if (enchantment == Enchantments.FLAMING_ARROWS)
//			return true;
//		if (enchantment == Enchantments.MOB_LOOTING)
//			return true;
//		if (enchantment == AllEnchantments.POTATO_RECOVERY.get())
//			return true;
//		return super.canApplyAtEnchantingTable(stack, enchantment);
//	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

//	@Override
//	public int getItemStackLimit(ItemStack stack) {
//		return 1;
//	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getRGBDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return BackTankUtil.showDurabilityBar(stack, maxUses());
	}

	private int maxUses() {
		return AllConfigs.SERVER.curiosities.maxPotatoCannonShots.get();
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

	public boolean isCannon(ItemStack stack) {
		return stack.getItem() instanceof PotatoCannonItem;
	}

//	@Override
//	public int getMaxDamage(ItemStack stack) {
//		return MAX_DAMAGE;
//	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		return findAmmoInInventory(world, player, stack).map(itemStack -> {
					if (ShootableGadgetItemMethods.shouldSwap(player, stack, hand, this::isCannon))
						return InteractionResultHolder.fail(stack);

					if (world.isClientSide) {
						CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
						return InteractionResultHolder.success(stack);
					}

					Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND,
							new Vec3(.75f, -0.15f, 1.5f));
					Vec3 correction =
							ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND, new Vec3(-.05f, 0, 0))
									.subtract(player.position()
											.add(0, player.getEyeHeight(), 0));

					PotatoCannonProjectileType projectileType = PotatoProjectileTypeManager.getTypeForStack(itemStack)
							.orElse(BuiltinPotatoProjectileTypes.FALLBACK);
					Vec3 lookVec = player.getLookAngle();
					Vec3 motion = lookVec.add(correction)
							.normalize()
							.scale(projectileType.getVelocityMultiplier());

					float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - .5f) / 4f;

					boolean spray = projectileType.getSplit() > 1;
					Vec3 sprayBase = VecHelper.rotate(new Vec3(0, 0.1, 0), 360 * Create.RANDOM.nextFloat(), Axis.Z);
					float sprayChange = 360f / projectileType.getSplit();

					for (int i = 0; i < projectileType.getSplit(); i++) {
						PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
						projectile.setItem(itemStack);
						projectile.setEnchantmentEffectsFromCannon(stack);

						Vec3 splitMotion = motion;
						if (spray) {
							float imperfection = 40 * (Create.RANDOM.nextFloat() - 0.5f);
							Vec3 sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
							splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, motion));
						}

						if (i != 0)
							projectile.recoveryChance = 0;

						projectile.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
						projectile.setDeltaMovement(splitMotion);
						projectile.setOwner(player);
						world.addFreshEntity(projectile);
					}

					if (!player.isCreative()) {
						itemStack.shrink(1);
						if (itemStack.isEmpty())
							player.getInventory().removeItem(itemStack);
					}

					if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
						stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

					Integer cooldown =
							findAmmoInInventory(world, player, stack).flatMap(PotatoProjectileTypeManager::getTypeForStack)
									.map(PotatoCannonProjectileType::getReloadTicks)
									.orElse(10);

					ShootableGadgetItemMethods.applyCooldown(player, stack, hand, this::isCannon, cooldown);
					ShootableGadgetItemMethods.sendPackets(player,
							b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), itemStack, hand, soundPitch, b));
					return InteractionResultHolder.success(stack);
				})
				.orElse(InteractionResultHolder.pass(stack));
	}

//	@Override
//	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
//		return slotChanged || newStack.getItem() != oldStack.getItem();
//	}

	private Optional<ItemStack> findAmmoInInventory(Level world, Player player, ItemStack held) {
		ItemStack findAmmo = player.getProjectile(held);
		return PotatoProjectileTypeManager.getTypeForStack(findAmmo)
			.map($ -> findAmmo);
	}

	@Environment(EnvType.CLIENT)
	public static Optional<ItemStack> getAmmoforPreview(ItemStack cannon) {
		if (AnimationTickHolder.getTicks() % 3 != 0)
			return Optional.of(CLIENT_CURRENT_AMMO)
				.filter(stack -> !stack.isEmpty());

		LocalPlayer player = Minecraft.getInstance().player;
		CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
		if (player == null)
			return Optional.empty();
		ItemStack findAmmo = player.getProjectile(cannon);
		Optional<ItemStack> found = PotatoProjectileTypeManager.getTypeForStack(findAmmo)
			.map($ -> findAmmo);
		found.ifPresent(stack -> CLIENT_CURRENT_AMMO = stack);
		return found;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
		int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
		final float additionalDamageMult = 1 + power * .2f;
		final float additionalKnockback = punch * .5f;
		getAmmoforPreview(stack).ifPresent(ammo -> {
			String _attack = "potato_cannon.ammo.attack_damage";
			String _reload = "potato_cannon.ammo.reload_ticks";
			String _knockback = "potato_cannon.ammo.knockback";

			tooltip.add(new TextComponent(""));
			tooltip.add(new TranslatableComponent(ammo.getDescriptionId()).append(new TextComponent(":"))
				.withStyle(ChatFormatting.GRAY));
			PotatoCannonProjectileType type = PotatoProjectileTypeManager.getTypeForStack(ammo)
				.get();
			TextComponent spacing = new TextComponent(" ");
			ChatFormatting green = ChatFormatting.GREEN;
			ChatFormatting darkGreen = ChatFormatting.DARK_GREEN;

			float damageF = type.getDamage() * additionalDamageMult;
			MutableComponent damage = new TextComponent(
				damageF == Mth.floor(damageF) ? "" + Mth.floor(damageF) : "" + damageF);
			MutableComponent reloadTicks = new TextComponent("" + type.getReloadTicks());
			MutableComponent knockback =
				new TextComponent("" + (type.getKnockback() + additionalKnockback));

			damage = damage.withStyle(additionalDamageMult > 1 ? green : darkGreen);
			knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
			reloadTicks = reloadTicks.withStyle(darkGreen);

			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_attack, damage)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_reload, reloadTicks)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_knockback, knockback)
					.withStyle(darkGreen)));
		});
		super.appendHoverText(stack, world, tooltip, flag);
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return stack -> PotatoProjectileTypeManager.getTypeForStack(stack)
			.isPresent();
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

//	@Override
//	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
//		return true;
//	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}

}