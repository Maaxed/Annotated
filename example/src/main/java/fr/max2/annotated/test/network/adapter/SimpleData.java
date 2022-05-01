package fr.max2.annotated.test.network.adapter;

import fr.max2.annotated.api.network.NetworkAdaptable;
import fr.max2.annotated.api.network.NetworkSerializable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

@NetworkSerializable
@NetworkAdaptable
public record SimpleData(Entity entity, Player player, int myInt)
{ }
