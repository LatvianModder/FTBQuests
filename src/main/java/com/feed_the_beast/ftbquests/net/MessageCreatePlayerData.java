package com.feed_the_beast.ftbquests.net;

import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.PlayerData;
import com.feed_the_beast.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;

/**
 * @author LatvianModder
 */
public class MessageCreatePlayerData extends MessageBase
{
	private final UUID uuid;
	private final String name;

	MessageCreatePlayerData(FriendlyByteBuf buffer)
	{
		uuid = NetUtils.readUUID(buffer);
		name = buffer.readUtf();
	}

	public MessageCreatePlayerData(PlayerData data)
	{
		uuid = data.uuid;
		name = data.name;
	}

	@Override
	public void write(FriendlyByteBuf buffer)
	{
		NetUtils.writeUUID(buffer, uuid);
		buffer.writeUtf(name);
	}

	@Override
	public void handle(NetworkEvent.Context context)
	{
		FTBQuests.NET_PROXY.createPlayerData(uuid, name);
	}
}