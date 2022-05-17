package fr.max2.annotated.test.client.gui;

import fr.max2.annotated.test.ModTestAnnotated;
import fr.max2.annotated.test.network.packet.SimpleData_simpleIntData;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
            	ModTestAnnotated.CHANNEL.sendToServer(new SimpleData_simpleIntData(index));
            }));
        }
    }
}