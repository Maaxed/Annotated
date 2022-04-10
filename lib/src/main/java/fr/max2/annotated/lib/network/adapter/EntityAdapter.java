package fr.max2.annotated.lib.network.adapter;

import java.util.UUID;

import fr.max2.annotated.lib.network.util.CommonUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class EntityAdapter
{
	public static final NetworkAdapter<Entity, Integer> EntityAdapter = new DelegatedAdapter<>(Entity::getId,
		(id, ctx) ->
		{
			return CommonUtils.getLevel(ctx).getEntity(id);
		});
	
	public static final NetworkAdapter<Player, UUID> PlayerAdapter = new DelegatedAdapter<>(Player::getUUID,
		(uuid, ctx) ->
		{
			return CommonUtils.getLevel(ctx).getPlayerByUUID(uuid);
		});
}
