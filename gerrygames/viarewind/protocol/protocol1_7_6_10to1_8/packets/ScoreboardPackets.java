package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.util.ChatColorUtil;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Scoreboard;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ScoreboardPackets {
   public static void register(Protocol1_7_6_10TO1_8 protocol) {
      protocol.registerClientbound(ClientboundPackets1_8.SCOREBOARD_OBJECTIVE, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               String name = (String)packetWrapper.passthrough(Type.STRING);
               if (name.length() > 16) {
                  packetWrapper.set(Type.STRING, 0, name = name.substring(0, 16));
               }

               byte mode = (Byte)packetWrapper.read(Type.BYTE);
               Scoreboard scoreboard = (Scoreboard)packetWrapper.user().get(Scoreboard.class);
               String displayName;
               if (mode == 0) {
                  if (scoreboard.objectiveExists(name)) {
                     packetWrapper.cancel();
                     return;
                  }

                  scoreboard.addObjective(name);
               } else if (mode == 1) {
                  if (!scoreboard.objectiveExists(name)) {
                     packetWrapper.cancel();
                     return;
                  }

                  if (scoreboard.getColorIndependentSidebar() != null) {
                     displayName = packetWrapper.user().getProtocolInfo().getUsername();
                     Optional color = scoreboard.getPlayerTeamColor(displayName);
                     if (color.isPresent()) {
                        String sidebar = (String)scoreboard.getColorDependentSidebar().get(color.get());
                        if (name.equals(sidebar)) {
                           PacketWrapper sidebarPacket = PacketWrapper.create(61, (ByteBuf)null, packetWrapper.user());
                           sidebarPacket.write(Type.BYTE, (byte)1);
                           sidebarPacket.write(Type.STRING, scoreboard.getColorIndependentSidebar());
                           PacketUtil.sendPacket(sidebarPacket, Protocol1_7_6_10TO1_8.class);
                        }
                     }
                  }

                  scoreboard.removeObjective(name);
               } else if (mode == 2 && !scoreboard.objectiveExists(name)) {
                  packetWrapper.cancel();
                  return;
               }

               if (mode != 0 && mode != 2) {
                  packetWrapper.write(Type.STRING, "");
               } else {
                  displayName = (String)packetWrapper.passthrough(Type.STRING);
                  if (displayName.length() > 32) {
                     packetWrapper.set(Type.STRING, 1, displayName.substring(0, 32));
                  }

                  packetWrapper.read(Type.STRING);
               }

               packetWrapper.write(Type.BYTE, mode);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.UPDATE_SCORE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.map(Type.VAR_INT, Type.BYTE);
            this.handler((packetWrapper) -> {
               Scoreboard scoreboard = (Scoreboard)packetWrapper.user().get(Scoreboard.class);
               String name = (String)packetWrapper.get(Type.STRING, 0);
               byte mode = (Byte)packetWrapper.get(Type.BYTE, 0);
               if (mode == 1) {
                  name = scoreboard.removeTeamForScore(name);
               } else {
                  name = scoreboard.sendTeamForScore(name);
               }

               if (name.length() > 16) {
                  name = ChatColorUtil.stripColor(name);
                  if (name.length() > 16) {
                     name = name.substring(0, 16);
                  }
               }

               packetWrapper.set(Type.STRING, 0, name);
               String objective = (String)packetWrapper.read(Type.STRING);
               if (objective.length() > 16) {
                  objective = objective.substring(0, 16);
               }

               if (mode != 1) {
                  int score = (Integer)packetWrapper.read(Type.VAR_INT);
                  packetWrapper.write(Type.STRING, objective);
                  packetWrapper.write(Type.INT, score);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.DISPLAY_SCOREBOARD, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.BYTE);
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               byte position = (Byte)packetWrapper.get(Type.BYTE, 0);
               String name = (String)packetWrapper.get(Type.STRING, 0);
               Scoreboard scoreboard = (Scoreboard)packetWrapper.user().get(Scoreboard.class);
               if (position > 2) {
                  byte receiverTeamColor = (byte)(position - 3);
                  scoreboard.getColorDependentSidebar().put(receiverTeamColor, name);
                  String usernamex = packetWrapper.user().getProtocolInfo().getUsername();
                  Optional color = scoreboard.getPlayerTeamColor(usernamex);
                  if (color.isPresent() && (Byte)color.get() == receiverTeamColor) {
                     position = 1;
                  } else {
                     position = -1;
                  }
               } else if (position == 1) {
                  scoreboard.setColorIndependentSidebar(name);
                  String username = packetWrapper.user().getProtocolInfo().getUsername();
                  Optional colorx = scoreboard.getPlayerTeamColor(username);
                  if (colorx.isPresent() && scoreboard.getColorDependentSidebar().containsKey(colorx.get())) {
                     position = -1;
                  }
               }

               if (position == -1) {
                  packetWrapper.cancel();
               } else {
                  packetWrapper.set(Type.BYTE, 0, position);
               }
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.TEAMS, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String team = (String)packetWrapper.get(Type.STRING, 0);
               if (team == null) {
                  packetWrapper.cancel();
               } else {
                  byte mode = (Byte)packetWrapper.passthrough(Type.BYTE);
                  Scoreboard scoreboard = (Scoreboard)packetWrapper.user().get(Scoreboard.class);
                  if (mode != 0 && !scoreboard.teamExists(team)) {
                     packetWrapper.cancel();
                  } else {
                     if (mode == 0 && scoreboard.teamExists(team)) {
                        scoreboard.removeTeam(team);
                        PacketWrapper remove = PacketWrapper.create(62, (ByteBuf)null, packetWrapper.user());
                        remove.write(Type.STRING, team);
                        remove.write(Type.BYTE, (byte)1);
                        PacketUtil.sendPacket(remove, Protocol1_7_6_10TO1_8.class, true, true);
                     }

                     if (mode == 0) {
                        scoreboard.addTeam(team);
                     } else if (mode == 1) {
                        scoreboard.removeTeam(team);
                     }

                     byte color;
                     if (mode == 0 || mode == 2) {
                        packetWrapper.passthrough(Type.STRING);
                        packetWrapper.passthrough(Type.STRING);
                        packetWrapper.passthrough(Type.STRING);
                        packetWrapper.passthrough(Type.BYTE);
                        packetWrapper.read(Type.STRING);
                        color = (Byte)packetWrapper.read(Type.BYTE);
                        if (mode == 2 && (Byte)scoreboard.getTeamColor(team).get() != color) {
                           String username = packetWrapper.user().getProtocolInfo().getUsername();
                           String sidebar = (String)scoreboard.getColorDependentSidebar().get(color);
                           PacketWrapper sidebarPacket = packetWrapper.create(61);
                           sidebarPacket.write(Type.BYTE, (byte)1);
                           sidebarPacket.write(Type.STRING, sidebar == null ? "" : sidebar);
                           PacketUtil.sendPacket(sidebarPacket, Protocol1_7_6_10TO1_8.class);
                        }

                        scoreboard.setTeamColor(team, color);
                     }

                     if (mode == 0 || mode == 3 || mode == 4) {
                        color = (Byte)scoreboard.getTeamColor(team).get();
                        String[] entries = (String[])packetWrapper.read(Type.STRING_ARRAY);
                        List entryList = new ArrayList();

                        String entry;
                        for(int i = 0; i < entries.length; ++i) {
                           entry = entries[i];
                           String usernamex = packetWrapper.user().getProtocolInfo().getUsername();
                           PacketWrapper sidebarPacketx;
                           if (mode == 4) {
                              if (!scoreboard.isPlayerInTeam(entry, team)) {
                                 continue;
                              }

                              scoreboard.removePlayerFromTeam(entry, team);
                              if (entry.equals(usernamex)) {
                                 sidebarPacketx = packetWrapper.create(61);
                                 sidebarPacketx.write(Type.BYTE, (byte)1);
                                 sidebarPacketx.write(Type.STRING, scoreboard.getColorIndependentSidebar() == null ? "" : scoreboard.getColorIndependentSidebar());
                                 PacketUtil.sendPacket(sidebarPacketx, Protocol1_7_6_10TO1_8.class);
                              }
                           } else {
                              scoreboard.addPlayerToTeam(entry, team);
                              if (entry.equals(usernamex) && scoreboard.getColorDependentSidebar().containsKey(color)) {
                                 sidebarPacketx = packetWrapper.create(61);
                                 sidebarPacketx.write(Type.BYTE, (byte)1);
                                 sidebarPacketx.write(Type.STRING, (String)scoreboard.getColorDependentSidebar().get(color));
                                 PacketUtil.sendPacket(sidebarPacketx, Protocol1_7_6_10TO1_8.class);
                              }
                           }

                           entryList.add(entry);
                        }

                        packetWrapper.write(Type.SHORT, (short)entryList.size());
                        Iterator var15 = entryList.iterator();

                        while(var15.hasNext()) {
                           entry = (String)var15.next();
                           packetWrapper.write(Type.STRING, entry);
                        }
                     }

                  }
               }
            });
         }
      });
   }
}
