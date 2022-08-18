package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage;

import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Type;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Scoreboard extends StoredObject {
   private HashMap teams = new HashMap();
   private HashSet objectives = new HashSet();
   private HashMap scoreTeams = new HashMap();
   private HashMap teamColors = new HashMap();
   private HashSet scoreTeamNames = new HashSet();
   private String colorIndependentSidebar;
   private HashMap colorDependentSidebar = new HashMap();

   public Scoreboard(UserConnection user) {
      super(user);
   }

   public void addPlayerToTeam(String player, String team) {
      ((List)this.teams.computeIfAbsent(team, (key) -> {
         return new ArrayList();
      })).add(player);
   }

   public void setTeamColor(String team, Byte color) {
      this.teamColors.put(team, color);
   }

   public Optional getTeamColor(String team) {
      return Optional.ofNullable((Byte)this.teamColors.get(team));
   }

   public void addTeam(String team) {
      this.teams.computeIfAbsent(team, (key) -> {
         return new ArrayList();
      });
   }

   public void removeTeam(String team) {
      this.teams.remove(team);
      this.scoreTeams.remove(team);
      this.teamColors.remove(team);
   }

   public boolean teamExists(String team) {
      return this.teams.containsKey(team);
   }

   public void removePlayerFromTeam(String player, String team) {
      List teamPlayers = (List)this.teams.get(team);
      if (teamPlayers != null) {
         teamPlayers.remove(player);
      }

   }

   public boolean isPlayerInTeam(String player, String team) {
      List teamPlayers = (List)this.teams.get(team);
      return teamPlayers != null && teamPlayers.contains(player);
   }

   public boolean isPlayerInTeam(String player) {
      Iterator var2 = this.teams.values().iterator();

      List teamPlayers;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         teamPlayers = (List)var2.next();
      } while(!teamPlayers.contains(player));

      return true;
   }

   public Optional getPlayerTeamColor(String player) {
      Optional team = this.getTeam(player);
      return team.isPresent() ? this.getTeamColor((String)team.get()) : Optional.empty();
   }

   public Optional getTeam(String player) {
      Iterator var2 = this.teams.entrySet().iterator();

      Map.Entry entry;
      do {
         if (!var2.hasNext()) {
            return Optional.empty();
         }

         entry = (Map.Entry)var2.next();
      } while(!((List)entry.getValue()).contains(player));

      return Optional.of((String)entry.getKey());
   }

   public void addObjective(String name) {
      this.objectives.add(name);
   }

   public void removeObjective(String name) {
      this.objectives.remove(name);
      this.colorDependentSidebar.values().remove(name);
      if (name.equals(this.colorIndependentSidebar)) {
         this.colorIndependentSidebar = null;
      }

   }

   public boolean objectiveExists(String name) {
      return this.objectives.contains(name);
   }

   public String sendTeamForScore(String score) {
      if (score.length() <= 16) {
         return score;
      } else if (this.scoreTeams.containsKey(score)) {
         return ((ScoreTeam)this.scoreTeams.get(score)).name;
      } else {
         int l = 16;
         int i = Math.min(16, score.length() - 16);

         String name;
         for(name = score.substring(i, i + l); this.scoreTeamNames.contains(name) || this.teams.containsKey(name); name = score.substring(i, i + l)) {
            --i;

            while(score.length() - l - i > 16) {
               --l;
               if (l < 1) {
                  return score;
               }

               i = Math.min(16, score.length() - l);
            }
         }

         String prefix = score.substring(0, i);
         String suffix = i + l >= score.length() ? "" : score.substring(i + l, score.length());
         ScoreTeam scoreTeam = new ScoreTeam(name, prefix, suffix);
         this.scoreTeams.put(score, scoreTeam);
         this.scoreTeamNames.add(name);
         PacketWrapper teamPacket = PacketWrapper.create(62, (ByteBuf)null, this.getUser());
         teamPacket.write(Type.STRING, name);
         teamPacket.write(Type.BYTE, (byte)0);
         teamPacket.write(Type.STRING, "ViaRewind");
         teamPacket.write(Type.STRING, prefix);
         teamPacket.write(Type.STRING, suffix);
         teamPacket.write(Type.BYTE, (byte)0);
         teamPacket.write(Type.SHORT, Short.valueOf((short)1));
         teamPacket.write(Type.STRING, name);
         PacketUtil.sendPacket(teamPacket, Protocol1_7_6_10TO1_8.class, true, true);
         return name;
      }
   }

   public String removeTeamForScore(String score) {
      ScoreTeam scoreTeam = (ScoreTeam)this.scoreTeams.remove(score);
      if (scoreTeam == null) {
         return score;
      } else {
         this.scoreTeamNames.remove(scoreTeam.name);
         PacketWrapper teamPacket = PacketWrapper.create(62, (ByteBuf)null, this.getUser());
         teamPacket.write(Type.STRING, scoreTeam.name);
         teamPacket.write(Type.BYTE, (byte)1);
         PacketUtil.sendPacket(teamPacket, Protocol1_7_6_10TO1_8.class, true, true);
         return scoreTeam.name;
      }
   }

   public String getColorIndependentSidebar() {
      return this.colorIndependentSidebar;
   }

   public HashMap getColorDependentSidebar() {
      return this.colorDependentSidebar;
   }

   public void setColorIndependentSidebar(String colorIndependentSidebar) {
      this.colorIndependentSidebar = colorIndependentSidebar;
   }

   private class ScoreTeam {
      private String prefix;
      private String suffix;
      private String name;

      public ScoreTeam(String name, String prefix, String suffix) {
         this.prefix = prefix;
         this.suffix = suffix;
         this.name = name;
      }
   }
}
