package net.minecraft.src;

import io.github.pixee.security.HostValidator;
import io.github.pixee.security.Urls;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.src.PlayerUsageSnooperThread;

public class PlayerUsageSnooper {

   private Map field_52025_a = new HashMap();
   private final URL field_52024_b;


   public PlayerUsageSnooper(String p_i1300_1_) {
      try {
         this.field_52024_b = Urls.create("http://snoop.minecraft.net/" + p_i1300_1_, Urls.HTTP_PROTOCOLS, HostValidator.DENY_COMMON_INFRASTRUCTURE_TARGETS);
      } catch (MalformedURLException var3) {
         throw new IllegalArgumentException();
      }
   }

   public void func_52022_a(String p_52022_1_, Object p_52022_2_) {
      this.field_52025_a.put(p_52022_1_, p_52022_2_);
   }

   public void func_52021_a() {
      PlayerUsageSnooperThread var1 = new PlayerUsageSnooperThread(this, "reporter");
      var1.setDaemon(true);
      var1.start();
   }

   // $FF: synthetic method
   static URL func_52023_a(PlayerUsageSnooper p_52023_0_) {
      return p_52023_0_.field_52024_b;
   }

   // $FF: synthetic method
   static Map func_52020_b(PlayerUsageSnooper p_52020_0_) {
      return p_52020_0_.field_52025_a;
   }
}
