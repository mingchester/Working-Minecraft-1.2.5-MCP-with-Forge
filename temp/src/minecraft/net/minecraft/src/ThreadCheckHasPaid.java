package net.minecraft.src;

import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.client.Minecraft;

public class ThreadCheckHasPaid extends Thread {

   // $FF: synthetic field
   final Minecraft field_28146_a;


   public ThreadCheckHasPaid(Minecraft p_i360_1_) {
      this.field_28146_a = p_i360_1_;
   }

   public void run() {
      try {
         HttpURLConnection var1 = (HttpURLConnection)(Urls.create("https://login.minecraft.net/session?name=" + this.field_28146_a.field_6320_i.field_1666_b + "&session=" + this.field_28146_a.field_6320_i.field_6543_c, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS)).openConnection();
         var1.connect();
         if(var1.getResponseCode() == 400 && this == null) {
            Minecraft.field_28005_H = System.currentTimeMillis();
         }

         var1.disconnect();
      } catch (Exception var2) {
         var2.printStackTrace();
      }

   }
}
