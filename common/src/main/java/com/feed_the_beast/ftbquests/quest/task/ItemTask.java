package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.gui.CustomToast;
import com.feed_the_beast.ftbquests.gui.quests.ValidItemsScreen;
import com.feed_the_beast.ftbquests.integration.jei.FTBQuestsJEIHelper;
import com.feed_the_beast.ftbquests.item.FTBQuestsItems;
import com.feed_the_beast.ftbquests.item.MissingItem;
import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.Tristate;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.IconAnimation;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.utils.Bits;
import com.feed_the_beast.mods.ftbguilibrary.utils.TooltipList;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import dev.latvian.mods.itemfilters.api.ItemFiltersAPI;
import me.shedaniel.architectury.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public class ItemTask extends Task implements Predicate<ItemStack> {
	public ItemStack item;
	public long count;
	public Tristate consumeItems;
	public Tristate onlyFromCrafting;

	public ItemTask(Quest quest) {
		super(quest);
		item = ItemStack.EMPTY;
		count = 1;
		consumeItems = Tristate.DEFAULT;
		onlyFromCrafting = Tristate.DEFAULT;
	}

	@Override
	public TaskType getType() {
		return TaskTypes.ITEM;
	}

	@Override
	public long getMaxProgress() {
		return count;
	}

	@Override
	public void writeData(CompoundTag nbt) {
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", item);

		if (count > 1) {
			nbt.putLong("count", count);
		}

		consumeItems.write(nbt, "consume_items");
		onlyFromCrafting.write(nbt, "only_from_crafting");
	}

	@Override
	public void readData(CompoundTag nbt) {
		super.readData(nbt);
		item = NBTUtils.read(nbt, "item");
		count = Math.max(nbt.getLong("count"), 1L);
		consumeItems = Tristate.read(nbt, "consume_items");
		onlyFromCrafting = Tristate.read(nbt, "only_from_crafting");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer) {
		super.writeNetData(buffer);
		int flags = 0;
		flags = Bits.setFlag(flags, 1, count > 1L);
		flags = Bits.setFlag(flags, 2, consumeItems != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 4, consumeItems == Tristate.TRUE);
		flags = Bits.setFlag(flags, 8, onlyFromCrafting != Tristate.DEFAULT);
		flags = Bits.setFlag(flags, 16, onlyFromCrafting == Tristate.TRUE);
		//flags = Bits.setFlag(flags, 32, ignoreDamage);
		//flags = Bits.setFlag(flags, 64, nbtMode != NBTMatchingMode.MATCH);
		//flags = Bits.setFlag(flags, 128, nbtMode == NBTMatchingMode.CONTAIN);
		buffer.writeVarInt(flags);

		FTBQuestsNetHandler.writeItemType(buffer, item);

		if (count > 1L) {
			buffer.writeVarLong(count);
		}
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer) {
		super.readNetData(buffer);
		int flags = buffer.readVarInt();

		item = FTBQuestsNetHandler.readItemType(buffer);
		count = Bits.getFlag(flags, 1) ? buffer.readVarLong() : 1L;
		consumeItems = Bits.getFlag(flags, 2) ? Bits.getFlag(flags, 4) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		onlyFromCrafting = Bits.getFlag(flags, 8) ? Bits.getFlag(flags, 16) ? Tristate.TRUE : Tristate.FALSE : Tristate.DEFAULT;
		//ignoreDamage = Bits.getFlag(flags, 32);
		//nbtMode = Bits.getFlag(flags, 64) ? Bits.getFlag(flags, 128) ? NBTMatchingMode.CONTAIN : NBTMatchingMode.IGNORE : NBTMatchingMode.MATCH;
	}

	public List<ItemStack> getValidDisplayItems() {
		List<ItemStack> list = new ArrayList<>();
		ItemFiltersAPI.getDisplayItemStacks(item, list);
		return list;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle() {
		if (count > 1) {
			return new TextComponent(count + "x ").append(item.getHoverName());
		}

		return new TextComponent("").append(item.getHoverName());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon() {
		List<Icon> icons = new ArrayList<>();

		for (ItemStack stack : getValidDisplayItems()) {
			ItemStack copy = stack.copy();
			copy.setCount(1);
			Icon icon = ItemIcon.getItemIcon(copy);

			if (!icon.isEmpty()) {
				icons.add(icon);
			}
		}

		if (icons.isEmpty()) {
			return ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get());
		}

		return IconAnimation.fromList(icons, false);
	}

	@Override
	public boolean test(ItemStack stack) {
		return ItemFiltersAPI.filter(item, stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config) {
		super.getConfig(config);
		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.task.ftbquests.item");
		config.addLong("count", count, v -> count = v, 1, 1, Long.MAX_VALUE);
		config.addEnum("consume_items", consumeItems, v -> consumeItems = v, Tristate.NAME_MAP);
		config.addEnum("only_from_crafting", onlyFromCrafting, v -> onlyFromCrafting = v, Tristate.NAME_MAP);
	}

	@Override
	public boolean consumesResources() {
		return consumeItems.get(quest.chapter.file.defaultTeamConsumeItems);
	}

	@Override
	public boolean canInsertItem() {
		return consumesResources();
	}

	@Override
	public boolean submitItemsOnInventoryChange() {
		return !consumesResources();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void onButtonClicked(Button button, boolean canClick) {
		button.playClickSound();

		List<ItemStack> validItems = getValidDisplayItems();

		if (!consumesResources() && validItems.size() == 1 && Platform.isModLoaded("jei")) {
			showJEIRecipe(validItems.get(0));
		} else if (validItems.isEmpty()) {
			Minecraft.getInstance().getToasts().addToast(new CustomToast(new TextComponent("No valid items!"), ItemIcon.getItemIcon(FTBQuestsItems.MISSING_ITEM.get()), new TextComponent("Report this bug to modpack author!")));
		} else {
			new ValidItemsScreen(this, validItems, canClick).openGui();
		}
	}

	@Environment(EnvType.CLIENT)
	private void showJEIRecipe(ItemStack stack) {
		FTBQuestsJEIHelper.showRecipes(stack);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void addMouseOverText(TooltipList list, @Nullable TaskData data) {
		if (consumesResources()) {
			list.blankLine();
			list.add(new TranslatableComponent("ftbquests.task.click_to_submit").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		} else if (getValidDisplayItems().size() > 1) {
			list.blankLine();
			list.add(new TranslatableComponent("ftbquests.task.ftbquests.item.view_items").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		} else if (Platform.isModLoaded("jei")) {
			list.blankLine();
			list.add(new TranslatableComponent("ftbquests.task.ftbquests.item.click_recipe").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
		}
	}

	@Override
	public TaskData createData(PlayerData data) {
		return new Data(this, data);
	}

	public static class Data extends TaskData<ItemTask> {
		private Data(ItemTask t, PlayerData data) {
			super(t, data);
		}

		public ItemStack insert(ItemStack stack, boolean simulate) {
			if (!isComplete() && task.test(stack)) {
				long add = Math.min(stack.getCount(), task.count - progress);

				if (add > 0L) {
					if (!simulate && data.file.isServerSide()) {
						addProgress(add);
					}

					ItemStack copy = stack.copy();
					copy.setCount((int) (stack.getCount() - add));
					return copy;
				}
			}

			return stack;
		}

		@Override
		public void submitTask(ServerPlayer player, ItemStack item) {
			if (isComplete() || task.item.getItem() instanceof MissingItem || item.getItem() instanceof MissingItem) {
				return;
			}

			if (!task.consumesResources()) {
				if (task.onlyFromCrafting.get(false)) {
					if (item.isEmpty() || !task.test(item)) {
						return;
					}

					long count = Math.min(task.count, item.getCount());

					if (count > progress) {
						setProgress(count);
						return;
					}
				}

				long count = 0;

				for (ItemStack stack : player.inventory.items) {
					if (!stack.isEmpty() && task.test(stack)) {
						count += stack.getCount();
					}
				}

				count = Math.min(task.count, count);

				if (count > progress) {
					setProgress(count);
					return;
				}

				return;
			}

			if (!item.isEmpty()) {
				return;
			}

			boolean changed = false;

			for (int i = 0; i < player.inventory.items.size(); i++) {
				ItemStack stack = player.inventory.items.get(i);
				ItemStack stack1 = insert(stack, false);

				if (stack != stack1) {
					changed = true;
					player.inventory.items.set(i, stack1.isEmpty() ? ItemStack.EMPTY : stack1);
				}
			}

			if (changed) {
				player.inventory.setChanged();
				player.containerMenu.broadcastChanges();
			}
		}
	}
}