package net.minecraft.src;

import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.client.Minecraft;

public class ThreadCheckHasPaid extends Thread
{
    final Minecraft mc;

    public ThreadCheckHasPaid(Minecraft par1Minecraft)
    {
        this.mc = par1Minecraft;
    }

    public void run()
    {
        try
        {
            HttpURLConnection var1 = (HttpURLConnection)(Urls.create("https://login.minecraft.net/session?name=" + this.mc.session.username + "&session=" + this.mc.session.sessionId, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS)).openConnection();
            var1.connect();

            if (var1.getResponseCode() == 400 && this == null)
            {
                Minecraft.hasPaidCheckTime = System.currentTimeMillis();
            }

            var1.disconnect();
        }
        catch (Exception var2)
        {
            var2.printStackTrace();
        }
    }
}
