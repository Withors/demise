package net.minecraft.network.play.client;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

public class C12PacketUpdateSign implements Packet<INetHandlerPlayServer> {
    private BlockPos pos;
    private IChatComponent[] lines;

    public C12PacketUpdateSign() {
    }

    public C12PacketUpdateSign(BlockPos pos, IChatComponent[] lines) {
        this.pos = pos;
        this.lines = new IChatComponent[]{lines[0], lines[1], lines[2], lines[3]};
    }

    public void readPacketData(PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.lines = new IChatComponent[4];

        for (int i = 0; i < 4; ++i) {
            String s = buf.readStringFromBuffer(384);
            IChatComponent ichatcomponent = IChatComponent.Serializer.jsonToComponent(s);
            this.lines[i] = ichatcomponent;
        }
    }

    public void writePacketData(PacketBuffer buf) {
        buf.writeBlockPos(this.pos);

        for (int i = 0; i < 4; ++i) {
            IChatComponent ichatcomponent = this.lines[i];
            String s = IChatComponent.Serializer.componentToJson(ichatcomponent);
            buf.writeString(s);
        }
    }

    public void processPacket(INetHandlerPlayServer handler) {
        handler.processUpdateSign(this);
    }

    public BlockPos getPosition() {
        return this.pos;
    }

    public IChatComponent[] getLines() {
        return this.lines;
    }
}
