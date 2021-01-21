package com.feed_the_beast.ftbquests.quest.task;

import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author LatvianModder
 */
public class DimensionTask extends Task
{
	public ResourceKey<Level> dimension;

	public DimensionTask(Quest quest)
	{
		super(quest);
		dimension = Level.NETHER;
	}

	@Override
	public TaskType getType()
	{
		return FTBQuestsTasks.DIMENSION;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		nbt.putString("dimension", dimension.location().toString());
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("dimension")));
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		buffer.writeResourceLocation(dimension.location());
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addString("dim", dimension.location().toString(), v -> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(v)), "minecraft:the_nether");
	}

	@Override
	public MutableComponent getAltTitle()
	{
		return new TranslatableComponent("ftbquests.task.ftbquests.dimension").append(": ").append(new TextComponent(dimension.location().toString()).withStyle(ChatFormatting.DARK_GREEN));
	}

	@Override
	public int autoSubmitOnPlayerTick()
	{
		return 100;
	}

	@Override
	public TaskData createData(PlayerData data)
	{
		return new Data(this, data);
	}

	public static class Data extends BooleanTaskData<DimensionTask>
	{
		private Data(DimensionTask task, PlayerData data)
		{
			super(task, data);
		}

		@Override
		public boolean canSubmit(ServerPlayer player)
		{
			return !player.isSpectator() && player.level.dimension() == task.dimension;
		}
	}
}