package fr.max2.annotated.example.client.gui;

import fr.max2.annotated.example.ModTestAnnotated;
import fr.max2.annotated.example.network.packet.SimpleData_Packets;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

public class GuiTest extends Screen
{
    public GuiTest()
    {
        super(new TextComponent("This is a test screen"));
    }

    @Override
    protected void init()
    {
        for (int i = 1; i <= 4; i++)
        {
            final int index = i;
            this.addRenderableWidget(new Button(this.width / 2, this.height / 5 * i, 200, 20, new TextComponent("Send packet " + i), (b) ->
            {
            	ModTestAnnotated.CHANNEL.sendToServer(SimpleData_Packets.sendInt(index));
            }));
        }
    }
}