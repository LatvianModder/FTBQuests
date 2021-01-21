package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.Chapter;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.quest.reward.Reward;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageClaimAllRewards extends MessageBase
{
	public MessageClaimAllRewards()
	{
	}

	MessageClaimAllRewards(FriendlyByteBuf buffer)
	{
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		ServerPlayer player = context.getSender();
		PlayerData data = PlayerData.get(player);

		for (Chapter chapter : ServerQuestFile.INSTANCE.chapters)
		{
			for (Quest quest : chapter.quests)
			{
				if (data.isComplete(quest))
				{
					for (Reward reward : quest.rewards)
					{
						if (!reward.getExcludeFromClaimAll())
						{
							data.claimReward(player, reward, true);
						}
					}
				}
			}
		}
	}
}