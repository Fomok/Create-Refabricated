package com.simibubi.create.content.logistics.item;

import static com.simibubi.create.foundation.gui.AllGuiTextures.PLAYER_INVENTORY;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.lib.mixin.accessor.SlotAccessor;
import com.simibubi.create.lib.utility.ItemStackUtil;

public class LinkedControllerScreen extends AbstractSimiContainerScreen<LinkedControllerContainer> {

	protected AllGuiTextures background;
	private List<Rect2i> extraAreas = Collections.emptyList();

	private IconButton resetButton;
	private IconButton confirmButton;

	public LinkedControllerScreen(LinkedControllerContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		this.background = AllGuiTextures.LINKED_CONTROLLER;
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height + 4 + PLAYER_INVENTORY.height);
		setWindowOffset(2 + (width % 2 == 0 ? 0 : -1), 0);
		super.init();
		widgets.clear();

		int x = leftPos;
		int y = topPos;

		resetButton = new IconButton(x + background.width - 62, y + background.height - 24, AllIcons.I_TRASH);
		confirmButton = new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);

		widgets.add(resetButton);
		widgets.add(confirmButton);

		extraAreas = ImmutableList.of(
			new Rect2i(x + background.width + 4, y + background.height - 44, 64, 56)
		);
	}

	@Override
	protected void renderWindow(PoseStack ms, int mouseX, int mouseY, float partialTicks) {
		int invX = getLeftOfCentered(PLAYER_INVENTORY.width);
		int invY = topPos + background.height + 4;
		renderPlayerInventory(ms, invX, invY);

		int x = leftPos;
		int y = topPos;

		background.draw(ms, this, x, y);
		font.draw(ms, title, x + 15, y + 4, 0x442000);

		GuiGameElement.of(menu.mainItem)
			.<GuiGameElement.GuiRenderBuilder>at(x + background.width - 4, y + background.height - 56, -200)
			.scale(5)
			.render(ms);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		if (!ItemStackUtil.equals(menu.player.getMainHandItem(), menu.mainItem, false))
			minecraft.player.closeContainer();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button == 0) {
			if (confirmButton.isHovered()) {
				minecraft.player.closeContainer();
				return true;
			}
			if (resetButton.isHovered()) {
				menu.clearContents();
				menu.sendClearPacket();
				return true;
			}
		}

		return mouseClicked;
	}

	@Override
	protected void renderTooltip(PoseStack ms, int x, int y) {
		if (!this.minecraft.player.getInventory().getSelected()
			.isEmpty() || this.hoveredSlot == null || this.hoveredSlot.hasItem()
			|| hoveredSlot.container == menu.playerInventory) {
			super.renderTooltip(ms, x, y);
			return;
		}
		renderTooltip(ms, addToTooltip(new LinkedList<>(), ((SlotAccessor) hoveredSlot).getSlotIndex()), Optional.empty(), x, y); // I think this replacement works?
		// renderWrappedToolTip(ms, addToTooltip(new LinkedList<>(), ((SlotAccessor) hoveredSlot).getSlotIndex()), x, y, font);
	}

	@Override
	public List<Component> getTooltipFromItem(ItemStack stack) {
		List<Component> list = super.getTooltipFromItem(stack);
		if (hoveredSlot.container == menu.playerInventory)
			return list;
		return hoveredSlot != null ? addToTooltip(list, hoveredSlot.index) : list;
	}

	private List<Component> addToTooltip(List<Component> list, int slot) {
		if (slot < 0 || slot >= 12)
			return list;
		list.add(Lang
			.createTranslationTextComponent("linked_controller.frequency_slot_" + ((slot % 2) + 1),
				LinkedControllerClientHandler.getControls()
					.get(slot / 2)
					.getTranslatedKeyMessage()
					.getString())
			.withStyle(ChatFormatting.GOLD));
		return list;
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

}