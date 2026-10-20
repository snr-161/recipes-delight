package fr.snrcloud.recipesdelight.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record NearbyContainerContentsPayload(BlockPos pos, List<Entry> entries) implements CustomPacketPayload {
	public static final Identifier ID = Identifier.fromNamespaceAndPath("recipesdelight", "nearby_container_contents");
	public static final Type<NearbyContainerContentsPayload> TYPE = new Type<>(ID);
	public static final StreamCodec<RegistryFriendlyByteBuf, NearbyContainerContentsPayload> STREAM_CODEC =
			StreamCodec.of(NearbyContainerContentsPayload::write, NearbyContainerContentsPayload::new);

	public NearbyContainerContentsPayload(FriendlyByteBuf buf) {
		this(buf.readBlockPos(), readEntries(buf));
	}

	private static List<Entry> readEntries(FriendlyByteBuf buf) {
		int size = buf.readVarInt();
		List<Entry> entries = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			entries.add(new Entry(buf.readIdentifier(), buf.readVarInt()));
		}
		return entries;
	}

	public static void write(RegistryFriendlyByteBuf buf, NearbyContainerContentsPayload message) {
		buf.writeBlockPos(message.pos());
		buf.writeVarInt(message.entries().size());
		for (Entry entry : message.entries()) {
			buf.writeIdentifier(entry.item());
			buf.writeVarInt(entry.count());
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public record Entry(Identifier item, int count) {
	}
}
