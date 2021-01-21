package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.config.NameMap;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.widget.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class KillTask extends Task
{
	public static final ResourceLocation ZOMBIE = new ResourceLocation("minecraft:zombie");
	public ResourceLocation entity = ZOMBIE;
	public long value = 100L;

	public KillTask(Quest quest)
	{
		super(quest);
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.KILL;
	}

	@Override
	public long getMaxProgress()
	{
		return value;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("entity", entity.toString());
		nbt.putLong("value", value);
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		entity = new ResourceLocation(nbt.getString("entity"));
		value = nbt.getLong("value");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeUtf(entity.toString());
		buffer.writeVarLong(value);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		entity = new ResourceLocation(buffer.readUtf());
		value = buffer.readVarInt();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		List<ResourceLocation> ids = new ArrayList<>();

		for (EntityType type : ForgeRegistries.ENTITIES)
		{
			ids.add(type.getRegistryName());
		}

		config.addEnum("entity", entity, v -> entity = v, NameMap.of(ZOMBIE, ids)
				.nameKey(v -> "entity." + v.getNamespace() + "." + v.getPath())
				.icon(v -> {
					SpawnEggItem item = SpawnEggItem.byId(ForgeRegistries.ENTITIES.getValue(v));
					return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
				})
				.create(), ZOMBIE);

		config.addLong("value", value, v -> value = v, 100L, 1L, Long.MAX_VALUE);
	}

	@Override
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("ftbquests.task.ftbquests.kill.title", getMaxProgressString(), new TranslatableComponent("entity." + entity.getNamespace() + "." + entity.getPath()));
	}

	@Override
	public Icon getAltIcon()
	{
		SpawnEggItem item = SpawnEggItem.byId(ForgeRegistries.ENTITIES.getValue(entity));
		return ItemIcon.getItemIcon(item != null ? item : Items.SPAWNER);
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void onButtonClicked(Button button, boolean canClick)
	{
	}

	public static class Data extends TaskData<KillTask>
	{
		private Data(KillTask task, PlayerData data)
		{
			super(task, data);
		}

		public void kill(LivingEntity entity)
		{
			if (!isComplete() && task.entity.equals(entity.getType().getRegistryName()))
			{
				addProgress(1L);
			}
		}
	}
}