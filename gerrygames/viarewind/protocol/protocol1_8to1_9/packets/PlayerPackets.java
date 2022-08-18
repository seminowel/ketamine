package de.gerrygames.viarewind.protocol.protocol1_8to1_9.packets;

import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.minecraft.metadata.types.MetaType1_8;
import com.viaversion.viaversion.api.protocol.Protocol;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.api.type.types.version.Types1_8;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import com.viaversion.viaversion.protocols.protocol1_9to1_8.ClientboundPackets1_9;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.Protocol1_8TO1_9;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BlockPlaceDestroyTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.BossBarStorage;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.Cooldown;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_8to1_9.storage.PlayerPosition;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.UUID;

public class PlayerPackets {
   public static void register(Protocol protocol) {
      protocol.registerClientbound(ClientboundPackets1_9.BOSSBAR, (ClientboundPacketType)null, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               UUID uuid = (UUID)packetWrapper.read(Type.UUID);
               int action = (Integer)packetWrapper.read(Type.VAR_INT);
               BossBarStorage bossBarStorage = (BossBarStorage)packetWrapper.user().get(BossBarStorage.class);
               if (action == 0) {
                  bossBarStorage.add(uuid, ChatUtil.jsonToLegacy((JsonElement)packetWrapper.read(Type.COMPONENT)), (Float)packetWrapper.read(Type.FLOAT));
                  packetWrapper.read(Type.VAR_INT);
                  packetWrapper.read(Type.VAR_INT);
                  packetWrapper.read(Type.UNSIGNED_BYTE);
               } else if (action == 1) {
                  bossBarStorage.remove(uuid);
               } else if (action == 2) {
                  bossBarStorage.updateHealth(uuid, (Float)packetWrapper.read(Type.FLOAT));
               } else if (action == 3) {
                  String title = ChatUtil.jsonToLegacy((JsonElement)packetWrapper.read(Type.COMPONENT));
                  bossBarStorage.updateTitle(uuid, title);
               }

            });
         }
      });
      protocol.cancelClientbound(ClientboundPackets1_9.COOLDOWN);
      protocol.registerClientbound(ClientboundPackets1_9.PLUGIN_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String channel = (String)packetWrapper.get(Type.STRING, 0);
               if (channel.equalsIgnoreCase("MC|TrList")) {
                  packetWrapper.passthrough(Type.INT);
                  short size;
                  if (packetWrapper.isReadable(Type.BYTE, 0)) {
                     size = (Byte)packetWrapper.passthrough(Type.BYTE);
                  } else {
                     size = (Short)packetWrapper.passthrough(Type.UNSIGNED_BYTE);
                  }

                  for(int i = 0; i < size; ++i) {
                     packetWrapper.write(Type.ITEM, ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM)));
                     packetWrapper.write(Type.ITEM, ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM)));
                     boolean has3Items = (Boolean)packetWrapper.passthrough(Type.BOOLEAN);
                     if (has3Items) {
                        packetWrapper.write(Type.ITEM, ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM)));
                     }

                     packetWrapper.passthrough(Type.BOOLEAN);
                     packetWrapper.passthrough(Type.INT);
                     packetWrapper.passthrough(Type.INT);
                  }
               } else if (channel.equalsIgnoreCase("MC|BOpen")) {
                  packetWrapper.read(Type.VAR_INT);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.GAME_EVENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               int reason = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               if (reason == 3) {
                  ((EntityTracker)packetWrapper.user().get(EntityTracker.class)).setPlayerGamemode(((Float)packetWrapper.get(Type.FLOAT, 0)).intValue());
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.JOIN_GAME, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.STRING);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.setPlayerId((Integer)packetWrapper.get(Type.INT, 0));
               tracker.setPlayerGamemode((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0));
               tracker.getClientEntityTypes().put(tracker.getPlayerId(), EntityType.ENTITY_HUMAN);
            });
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               world.setEnvironment((Byte)packetWrapper.get(Type.BYTE, 0));
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.BYTE);
            this.handler((packetWrapper) -> {
               PlayerPosition pos = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               int teleportId = (Integer)packetWrapper.read(Type.VAR_INT);
               pos.setConfirmId(teleportId);
               byte flags = (Byte)packetWrapper.get(Type.BYTE, 0);
               double x = (Double)packetWrapper.get(Type.DOUBLE, 0);
               double y = (Double)packetWrapper.get(Type.DOUBLE, 1);
               double z = (Double)packetWrapper.get(Type.DOUBLE, 2);
               float yaw = (Float)packetWrapper.get(Type.FLOAT, 0);
               float pitch = (Float)packetWrapper.get(Type.FLOAT, 1);
               packetWrapper.set(Type.BYTE, 0, (byte)0);
               if (flags != 0) {
                  if ((flags & 1) != 0) {
                     x += pos.getPosX();
                     packetWrapper.set(Type.DOUBLE, 0, x);
                  }

                  if ((flags & 2) != 0) {
                     y += pos.getPosY();
                     packetWrapper.set(Type.DOUBLE, 1, y);
                  }

                  if ((flags & 4) != 0) {
                     z += pos.getPosZ();
                     packetWrapper.set(Type.DOUBLE, 2, z);
                  }

                  if ((flags & 8) != 0) {
                     yaw += pos.getYaw();
                     packetWrapper.set(Type.FLOAT, 0, yaw);
                  }

                  if ((flags & 16) != 0) {
                     pitch += pos.getPitch();
                     packetWrapper.set(Type.FLOAT, 1, pitch);
                  }
               }

               pos.setPos(x, y, z);
               pos.setYaw(yaw);
               pos.setPitch(pitch);
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_9.RESPAWN, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               ((EntityTracker)packetWrapper.user().get(EntityTracker.class)).setPlayerGamemode((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 1));
            });
            this.handler((packetWrapper) -> {
               ((BossBarStorage)packetWrapper.user().get(BossBarStorage.class)).updateLocation();
               ((BossBarStorage)packetWrapper.user().get(BossBarStorage.class)).changeWorld();
            });
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               world.setEnvironment((Integer)packetWrapper.get(Type.INT, 0));
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.CHAT_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String msg = (String)packetWrapper.get(Type.STRING, 0);
               if (msg.toLowerCase().startsWith("/offhand")) {
                  packetWrapper.cancel();
                  PacketWrapper swapItems = PacketWrapper.create(19, (ByteBuf)null, packetWrapper.user());
                  swapItems.write(Type.VAR_INT, 6);
                  swapItems.write(Type.POSITION, new Position(0, (short)0, 0));
                  swapItems.write(Type.BYTE, -1);
                  PacketUtil.sendToServer(swapItems, Protocol1_8TO1_9.class, true, true);
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.INTERACT_ENTITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int type = (Integer)packetWrapper.get(Type.VAR_INT, 1);
               if (type == 2) {
                  packetWrapper.passthrough(Type.FLOAT);
                  packetWrapper.passthrough(Type.FLOAT);
                  packetWrapper.passthrough(Type.FLOAT);
               }

               if (type == 2 || type == 0) {
                  packetWrapper.write(Type.VAR_INT, 0);
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_MOVEMENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               int playerId = tracker.getPlayerId();
               if (tracker.isInsideVehicle(playerId)) {
                  packetWrapper.cancel();
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               PlayerPosition pos = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               if (pos.getConfirmId() == -1) {
                  pos.setPos((Double)packetWrapper.get(Type.DOUBLE, 0), (Double)packetWrapper.get(Type.DOUBLE, 1), (Double)packetWrapper.get(Type.DOUBLE, 2));
                  pos.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
               }
            });
            this.handler((packetWrapper) -> {
               ((BossBarStorage)packetWrapper.user().get(BossBarStorage.class)).updateLocation();
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               PlayerPosition pos = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               if (pos.getConfirmId() == -1) {
                  pos.setYaw((Float)packetWrapper.get(Type.FLOAT, 0));
                  pos.setPitch((Float)packetWrapper.get(Type.FLOAT, 1));
                  pos.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
               }
            });
            this.handler((packetWrapper) -> {
               ((BossBarStorage)packetWrapper.user().get(BossBarStorage.class)).updateLocation();
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_POSITION_AND_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               double x = (Double)packetWrapper.get(Type.DOUBLE, 0);
               double y = (Double)packetWrapper.get(Type.DOUBLE, 1);
               double z = (Double)packetWrapper.get(Type.DOUBLE, 2);
               float yaw = (Float)packetWrapper.get(Type.FLOAT, 0);
               float pitch = (Float)packetWrapper.get(Type.FLOAT, 1);
               boolean onGround = (Boolean)packetWrapper.get(Type.BOOLEAN, 0);
               PlayerPosition pos = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               if (pos.getConfirmId() != -1) {
                  if (pos.getPosX() == x && pos.getPosY() == y && pos.getPosZ() == z && pos.getYaw() == yaw && pos.getPitch() == pitch) {
                     PacketWrapper confirmTeleport = packetWrapper.create(0);
                     confirmTeleport.write(Type.VAR_INT, pos.getConfirmId());
                     PacketUtil.sendToServer(confirmTeleport, Protocol1_8TO1_9.class, true, true);
                     pos.setConfirmId(-1);
                  }
               } else {
                  pos.setPos(x, y, z);
                  pos.setYaw(yaw);
                  pos.setPitch(pitch);
                  pos.setOnGround(onGround);
                  ((BossBarStorage)packetWrapper.user().get(BossBarStorage.class)).updateLocation();
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_DIGGING, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.POSITION);
            this.handler((packetWrapper) -> {
               int state = (Integer)packetWrapper.get(Type.VAR_INT, 0);
               if (state == 0) {
                  ((BlockPlaceDestroyTracker)packetWrapper.user().get(BlockPlaceDestroyTracker.class)).setMining(true);
               } else {
                  BlockPlaceDestroyTracker tracker;
                  if (state == 2) {
                     tracker = (BlockPlaceDestroyTracker)packetWrapper.user().get(BlockPlaceDestroyTracker.class);
                     tracker.setMining(false);
                     tracker.setLastMining(System.currentTimeMillis() + 100L);
                     ((Cooldown)packetWrapper.user().get(Cooldown.class)).setLastHit(0L);
                  } else if (state == 1) {
                     tracker = (BlockPlaceDestroyTracker)packetWrapper.user().get(BlockPlaceDestroyTracker.class);
                     tracker.setMining(false);
                     tracker.setLastMining(0L);
                     ((Cooldown)packetWrapper.user().get(Cooldown.class)).hit();
                  }
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.POSITION);
            this.map(Type.BYTE, Type.VAR_INT);
            this.map(Type.ITEM, Type.NOTHING);
            this.create(Type.VAR_INT, 0);
            this.map(Type.BYTE, Type.UNSIGNED_BYTE);
            this.map(Type.BYTE, Type.UNSIGNED_BYTE);
            this.map(Type.BYTE, Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               if ((Integer)packetWrapper.get(Type.VAR_INT, 0) == -1) {
                  packetWrapper.cancel();
                  PacketWrapper useItem = PacketWrapper.create(29, (ByteBuf)null, packetWrapper.user());
                  useItem.write(Type.VAR_INT, 0);
                  PacketUtil.sendToServer(useItem, Protocol1_8TO1_9.class, true, true);
               }

            });
            this.handler((packetWrapper) -> {
               if ((Integer)packetWrapper.get(Type.VAR_INT, 0) != -1) {
                  ((BlockPlaceDestroyTracker)packetWrapper.user().get(BlockPlaceDestroyTracker.class)).place();
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.HELD_ITEM_CHANGE, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               ((Cooldown)packetWrapper.user().get(Cooldown.class)).hit();
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.ANIMATION, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               final PacketWrapper delayedPacket = PacketWrapper.create(26, (ByteBuf)null, packetWrapper.user());
               delayedPacket.write(Type.VAR_INT, 0);
               Protocol1_8TO1_9.TIMER.schedule(new TimerTask() {
                  public void run() {
                     PacketUtil.sendToServer(delayedPacket, Protocol1_8TO1_9.class);
                  }
               }, 5L);
            });
            this.handler((packetWrapper) -> {
               ((BlockPlaceDestroyTracker)packetWrapper.user().get(BlockPlaceDestroyTracker.class)).updateMining();
               ((Cooldown)packetWrapper.user().get(Cooldown.class)).hit();
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.ENTITY_ACTION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.map(Type.VAR_INT);
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int action = (Integer)packetWrapper.get(Type.VAR_INT, 1);
               if (action == 6) {
                  packetWrapper.set(Type.VAR_INT, 1, 7);
               } else if (action == 0) {
                  PlayerPosition pos = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
                  if (!pos.isOnGround()) {
                     PacketWrapper elytra = PacketWrapper.create(20, (ByteBuf)null, packetWrapper.user());
                     elytra.write(Type.VAR_INT, (Integer)packetWrapper.get(Type.VAR_INT, 0));
                     elytra.write(Type.VAR_INT, 8);
                     elytra.write(Type.VAR_INT, 0);
                     PacketUtil.sendToServer(elytra, Protocol1_8TO1_9.class, true, false);
                  }
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.STEER_VEHICLE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.UNSIGNED_BYTE);
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               int playerId = tracker.getPlayerId();
               int vehicle = tracker.getVehicle(playerId);
               if (vehicle != -1 && tracker.getClientEntityTypes().get(vehicle) == EntityType.BOAT) {
                  PacketWrapper steerBoat = PacketWrapper.create(17, (ByteBuf)null, packetWrapper.user());
                  float left = (Float)packetWrapper.get(Type.FLOAT, 0);
                  float forward = (Float)packetWrapper.get(Type.FLOAT, 1);
                  steerBoat.write(Type.BOOLEAN, forward != 0.0F || left < 0.0F);
                  steerBoat.write(Type.BOOLEAN, forward != 0.0F || left > 0.0F);
                  PacketUtil.sendToServer(steerBoat, Protocol1_8TO1_9.class);
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.UPDATE_SIGN, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.POSITION);
            this.handler((packetWrapper) -> {
               for(int i = 0; i < 4; ++i) {
                  packetWrapper.write(Type.STRING, ChatUtil.jsonToLegacy((JsonElement)packetWrapper.read(Type.COMPONENT)));
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.TAB_COMPLETE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               packetWrapper.write(Type.BOOLEAN, false);
            });
            this.map(Type.OPTIONAL_POSITION);
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.CLIENT_SETTINGS, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.map(Type.BYTE);
            this.map(Type.BYTE, Type.VAR_INT);
            this.map(Type.BOOLEAN);
            this.map(Type.UNSIGNED_BYTE);
            this.create(Type.VAR_INT, 1);
            this.handler((packetWrapper) -> {
               short flags = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               PacketWrapper updateSkin = PacketWrapper.create(28, (ByteBuf)null, packetWrapper.user());
               updateSkin.write(Type.VAR_INT, ((EntityTracker)packetWrapper.user().get(EntityTracker.class)).getPlayerId());
               ArrayList metadata = new ArrayList();
               metadata.add(new Metadata(10, MetaType1_8.Byte, (byte)flags));
               updateSkin.write(Types1_8.METADATA_LIST, metadata);
               PacketUtil.sendPacket(updateSkin, Protocol1_8TO1_9.class);
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_8.PLUGIN_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String channel = (String)packetWrapper.get(Type.STRING, 0);
               if (!channel.equalsIgnoreCase("MC|BEdit") && !channel.equalsIgnoreCase("MC|BSign")) {
                  if (channel.equalsIgnoreCase("MC|AdvCdm")) {
                     channel = "MC|AdvCmd";
                     packetWrapper.set(Type.STRING, 0, "MC|AdvCmd");
                  }
               } else {
                  Item book = (Item)packetWrapper.passthrough(Type.ITEM);
                  book.setIdentifier(386);
                  CompoundTag tag = book.tag();
                  if (tag.contains("pages")) {
                     ListTag pages = (ListTag)tag.get("pages");
                     if (pages.size() > ViaRewind.getConfig().getMaxBookPages()) {
                        packetWrapper.user().disconnect("Too many book pages");
                        return;
                     }

                     for(int i = 0; i < pages.size(); ++i) {
                        StringTag page = (StringTag)pages.get(i);
                        String value = page.getValue();
                        if (value.length() > ViaRewind.getConfig().getMaxBookPageSize()) {
                           packetWrapper.user().disconnect("Book page too large");
                           return;
                        }

                        value = ChatUtil.jsonToLegacy(value);
                        page.setValue(value);
                     }
                  }
               }

            });
         }
      });
   }
}
