package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * @author LatvianModder
 */
public class MessageDeleteObject extends MessageBase
{
	private final int id;

	MessageDeleteObject(FriendlyByteBuf buffer)
	{
		id = buffer.readVarInt();
	}

	public MessageDeleteObject(int i)
	{
		id = i;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeVarInt(id);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		if (NetUtils.canEdit(context))
		{
			ServerQuestFile.INSTANCE.deleteObject(id);
		}
	}
}