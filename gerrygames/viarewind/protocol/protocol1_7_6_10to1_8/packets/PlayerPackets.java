package de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.packets;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.Position;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types.EntityType;
import com.viaversion.viaversion.api.minecraft.item.Item;
import com.viaversion.viaversion.api.protocol.packet.ClientboundPacketType;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketRemapper;
import com.viaversion.viaversion.api.type.Type;
import com.viaversion.viaversion.libs.gson.JsonElement;
import com.viaversion.viaversion.libs.gson.JsonParser;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.CompoundTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.ListTag;
import com.viaversion.viaversion.libs.opennbt.tag.builtin.StringTag;
import com.viaversion.viaversion.protocols.protocol1_8.ClientboundPackets1_8;
import com.viaversion.viaversion.protocols.protocol1_9_3to1_9_1_2.storage.ClientWorld;
import de.gerrygames.viarewind.ViaRewind;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ClientboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.Protocol1_7_6_10TO1_8;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.ServerboundPackets1_7;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.entityreplacements.ArmorStandReplacement;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.items.ItemRewriter;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.provider.TitleRenderProvider;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.EntityTracker;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.GameProfileStorage;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerAbilities;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.PlayerPosition;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Scoreboard;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.storage.Windows;
import de.gerrygames.viarewind.protocol.protocol1_7_6_10to1_8.types.Types1_7_6_10;
import de.gerrygames.viarewind.replacement.EntityReplacement;
import de.gerrygames.viarewind.utils.ChatUtil;
import de.gerrygames.viarewind.utils.PacketUtil;
import de.gerrygames.viarewind.utils.Utils;
import de.gerrygames.viarewind.utils.math.AABB;
import de.gerrygames.viarewind.utils.math.Ray3d;
import de.gerrygames.viarewind.utils.math.RayTracing;
import de.gerrygames.viarewind.utils.math.Vector3d;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class PlayerPackets {
   public static void register(Protocol1_7_6_10TO1_8 protocol) {
      protocol.registerClientbound(ClientboundPackets1_8.JOIN_GAME, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.STRING);
            this.map(Type.BOOLEAN, Type.NOTHING);
            this.handler((packetWrapper) -> {
               if (ViaRewind.getConfig().isReplaceAdventureMode()) {
                  if ((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0) == 2) {
                     packetWrapper.set(Type.UNSIGNED_BYTE, 0, Short.valueOf((short)0));
                  }

               }
            });
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.setGamemode((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0));
               tracker.setPlayerId((Integer)packetWrapper.get(Type.INT, 0));
               tracker.getClientEntityTypes().put(tracker.getPlayerId(), EntityType.ENTITY_HUMAN);
               tracker.setDimension((Byte)packetWrapper.get(Type.BYTE, 0));
            });
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               world.setEnvironment((Byte)packetWrapper.get(Type.BYTE, 0));
            });
            this.handler((wrapper) -> {
               wrapper.user().put(new Scoreboard(wrapper.user()));
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.CHAT_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.COMPONENT);
            this.handler((packetWrapper) -> {
               int position = (Byte)packetWrapper.read(Type.BYTE);
               if (position == 2) {
                  packetWrapper.cancel();
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.SPAWN_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.INT, position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.UPDATE_HEALTH, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.VAR_INT, Type.SHORT);
            this.map(Type.FLOAT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.RESPAWN, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               if (ViaRewind.getConfig().isReplaceAdventureMode()) {
                  if ((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 1) == 2) {
                     packetWrapper.set(Type.UNSIGNED_BYTE, 1, Short.valueOf((short)0));
                  }

               }
            });
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               tracker.setGamemode((Short)packetWrapper.get(Type.UNSIGNED_BYTE, 1));
               if (tracker.getDimension() != (Integer)packetWrapper.get(Type.INT, 0)) {
                  tracker.setDimension((Integer)packetWrapper.get(Type.INT, 0));
                  tracker.clearEntities();
                  tracker.getClientEntityTypes().put(tracker.getPlayerId(), EntityType.ENTITY_HUMAN);
               }

            });
            this.handler((packetWrapper) -> {
               ClientWorld world = (ClientWorld)packetWrapper.user().get(ClientWorld.class);
               world.setEnvironment((Integer)packetWrapper.get(Type.INT, 0));
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.PLAYER_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               playerPosition.setPositionPacketReceived(true);
               int flags = (Byte)packetWrapper.read(Type.BYTE);
               double y;
               if ((flags & 1) == 1) {
                  y = (Double)packetWrapper.get(Type.DOUBLE, 0);
                  y += playerPosition.getPosX();
                  packetWrapper.set(Type.DOUBLE, 0, y);
               }

               y = (Double)packetWrapper.get(Type.DOUBLE, 1);
               if ((flags & 2) == 2) {
                  y += playerPosition.getPosY();
               }

               playerPosition.setReceivedPosY(y);
               ++y;
               packetWrapper.set(Type.DOUBLE, 1, y);
               if ((flags & 4) == 4) {
                  double z = (Double)packetWrapper.get(Type.DOUBLE, 2);
                  z += playerPosition.getPosZ();
                  packetWrapper.set(Type.DOUBLE, 2, z);
               }

               float pitch;
               if ((flags & 8) == 8) {
                  pitch = (Float)packetWrapper.get(Type.FLOAT, 0);
                  pitch += playerPosition.getYaw();
                  packetWrapper.set(Type.FLOAT, 0, pitch);
               }

               if ((flags & 16) == 16) {
                  pitch = (Float)packetWrapper.get(Type.FLOAT, 1);
                  pitch += playerPosition.getPitch();
                  packetWrapper.set(Type.FLOAT, 1, pitch);
               }

            });
            this.handler((packetWrapper) -> {
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               packetWrapper.write(Type.BOOLEAN, playerPosition.isOnGround());
            });
            this.handler((packetWrapper) -> {
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               if (tracker.getSpectating() != tracker.getPlayerId()) {
                  packetWrapper.cancel();
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.SET_EXPERIENCE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.VAR_INT, Type.SHORT);
            this.map(Type.VAR_INT, Type.SHORT);
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.GAME_EVENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.UNSIGNED_BYTE);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               int mode = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               if (mode == 3) {
                  int gamemode = ((Float)packetWrapper.get(Type.FLOAT, 0)).intValue();
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  if (gamemode == 3 || tracker.getGamemode() == 3) {
                     UUID uuid = packetWrapper.user().getProtocolInfo().getUuid();
                     Item[] equipment;
                     if (gamemode == 3) {
                        GameProfileStorage.GameProfile profile = ((GameProfileStorage)packetWrapper.user().get(GameProfileStorage.class)).get(uuid);
                        equipment = new Item[5];
                        equipment[4] = profile.getSkull();
                     } else {
                        equipment = tracker.getPlayerEquipment(uuid);
                        if (equipment == null) {
                           equipment = new Item[5];
                        }
                     }

                     for(int i = 1; i < 5; ++i) {
                        PacketWrapper setSlot = PacketWrapper.create(47, (ByteBuf)null, packetWrapper.user());
                        setSlot.write(Type.BYTE, (byte)0);
                        setSlot.write(Type.SHORT, (short)(9 - i));
                        setSlot.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[i]);
                        PacketUtil.sendPacket(setSlot, Protocol1_7_6_10TO1_8.class);
                     }
                  }

               }
            });
            this.handler((packetWrapper) -> {
               int mode = (Short)packetWrapper.get(Type.UNSIGNED_BYTE, 0);
               if (mode == 3) {
                  int gamemode = ((Float)packetWrapper.get(Type.FLOAT, 0)).intValue();
                  if (gamemode == 2 && ViaRewind.getConfig().isReplaceAdventureMode()) {
                     gamemode = 0;
                     packetWrapper.set(Type.FLOAT, 0, 0.0F);
                  }

                  ((EntityTracker)packetWrapper.user().get(EntityTracker.class)).setGamemode(gamemode);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.OPEN_SIGN_EDITOR, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               Position position = (Position)packetWrapper.read(Type.POSITION);
               packetWrapper.write(Type.INT, position.getX());
               packetWrapper.write(Type.INT, position.getY());
               packetWrapper.write(Type.INT, position.getZ());
            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.PLAYER_INFO, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               int action = (Integer)packetWrapper.read(Type.VAR_INT);
               int count = (Integer)packetWrapper.read(Type.VAR_INT);
               GameProfileStorage gameProfileStorage = (GameProfileStorage)packetWrapper.user().get(GameProfileStorage.class);

               for(int i = 0; i < count; ++i) {
                  UUID uuid = (UUID)packetWrapper.read(Type.UUID);
                  int entityId;
                  String displayName;
                  GameProfileStorage.GameProfile gameProfile;
                  if (action == 0) {
                     displayName = (String)packetWrapper.read(Type.STRING);
                     gameProfile = gameProfileStorage.get(uuid);
                     if (gameProfile == null) {
                        gameProfile = gameProfileStorage.put(uuid, displayName);
                     }

                     int propertyCount = (Integer)packetWrapper.read(Type.VAR_INT);

                     while(propertyCount-- > 0) {
                        gameProfile.properties.add(new GameProfileStorage.Property((String)packetWrapper.read(Type.STRING), (String)packetWrapper.read(Type.STRING), (Boolean)packetWrapper.read(Type.BOOLEAN) ? (String)packetWrapper.read(Type.STRING) : null));
                     }

                     entityId = (Integer)packetWrapper.read(Type.VAR_INT);
                     int ping = (Integer)packetWrapper.read(Type.VAR_INT);
                     gameProfile.ping = ping;
                     gameProfile.gamemode = entityId;
                     if ((Boolean)packetWrapper.read(Type.BOOLEAN)) {
                        gameProfile.setDisplayName(ChatUtil.jsonToLegacy((JsonElement)packetWrapper.read(Type.COMPONENT)));
                     }

                     PacketWrapper packetxx = PacketWrapper.create(56, (ByteBuf)null, packetWrapper.user());
                     packetxx.write(Type.STRING, gameProfile.name);
                     packetxx.write(Type.BOOLEAN, true);
                     packetxx.write(Type.SHORT, (short)ping);
                     PacketUtil.sendPacket(packetxx, Protocol1_7_6_10TO1_8.class);
                  } else {
                     int gamemode;
                     if (action == 1) {
                        gamemode = (Integer)packetWrapper.read(Type.VAR_INT);
                        gameProfile = gameProfileStorage.get(uuid);
                        if (gameProfile != null && gameProfile.gamemode != gamemode) {
                           if (gamemode == 3 || gameProfile.gamemode == 3) {
                              EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                              entityId = tracker.getPlayerEntityId(uuid);
                              if (entityId != -1) {
                                 Item[] equipment;
                                 if (gamemode == 3) {
                                    equipment = new Item[5];
                                    equipment[4] = gameProfile.getSkull();
                                 } else {
                                    equipment = tracker.getPlayerEquipment(uuid);
                                    if (equipment == null) {
                                       equipment = new Item[5];
                                    }
                                 }

                                 for(short slot = 0; slot < 5; ++slot) {
                                    PacketWrapper equipmentPacket = PacketWrapper.create(4, (ByteBuf)null, packetWrapper.user());
                                    equipmentPacket.write(Type.INT, entityId);
                                    equipmentPacket.write(Type.SHORT, slot);
                                    equipmentPacket.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, equipment[slot]);
                                    PacketUtil.sendPacket(equipmentPacket, Protocol1_7_6_10TO1_8.class);
                                 }
                              }
                           }

                           gameProfile.gamemode = gamemode;
                        }
                     } else if (action == 2) {
                        gamemode = (Integer)packetWrapper.read(Type.VAR_INT);
                        gameProfile = gameProfileStorage.get(uuid);
                        if (gameProfile != null) {
                           gameProfile.ping = gamemode;
                           PacketWrapper packetx = PacketWrapper.create(56, (ByteBuf)null, packetWrapper.user());
                           packetx.write(Type.STRING, gameProfile.name);
                           packetx.write(Type.BOOLEAN, true);
                           packetx.write(Type.SHORT, (short)gamemode);
                           PacketUtil.sendPacket(packetx, Protocol1_7_6_10TO1_8.class);
                        }
                     } else if (action == 3) {
                        displayName = (Boolean)packetWrapper.read(Type.BOOLEAN) ? ChatUtil.jsonToLegacy((JsonElement)packetWrapper.read(Type.COMPONENT)) : null;
                        gameProfile = gameProfileStorage.get(uuid);
                        if (gameProfile != null && (gameProfile.displayName != null || displayName != null) && (gameProfile.displayName == null && displayName != null || gameProfile.displayName != null && displayName == null || !gameProfile.displayName.equals(displayName))) {
                           gameProfile.setDisplayName(displayName);
                        }
                     } else if (action == 4) {
                        GameProfileStorage.GameProfile gameProfilex = gameProfileStorage.remove(uuid);
                        if (gameProfilex != null) {
                           PacketWrapper packet = PacketWrapper.create(56, (ByteBuf)null, packetWrapper.user());
                           packet.write(Type.STRING, gameProfilex.name);
                           packet.write(Type.BOOLEAN, false);
                           packet.write(Type.SHORT, (short)gameProfilex.ping);
                           PacketUtil.sendPacket(packet, Protocol1_7_6_10TO1_8.class);
                        }
                     }
                  }
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.PLAYER_ABILITIES, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.BYTE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               byte flags = (Byte)packetWrapper.get(Type.BYTE, 0);
               float flySpeed = (Float)packetWrapper.get(Type.FLOAT, 0);
               float walkSpeed = (Float)packetWrapper.get(Type.FLOAT, 1);
               PlayerAbilities abilities = (PlayerAbilities)packetWrapper.user().get(PlayerAbilities.class);
               abilities.setInvincible((flags & 8) == 8);
               abilities.setAllowFly((flags & 4) == 4);
               abilities.setFlying((flags & 2) == 2);
               abilities.setCreative((flags & 1) == 1);
               abilities.setFlySpeed(flySpeed);
               abilities.setWalkSpeed(walkSpeed);
               if (abilities.isSprinting() && abilities.isFlying()) {
                  packetWrapper.set(Type.FLOAT, 0, abilities.getFlySpeed() * 2.0F);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.PLUGIN_MESSAGE, new PacketRemapper() {
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
                     Item item = ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM));
                     packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);
                     item = ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM));
                     packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);
                     boolean has3Items = (Boolean)packetWrapper.passthrough(Type.BOOLEAN);
                     if (has3Items) {
                        item = ItemRewriter.toClient((Item)packetWrapper.read(Type.ITEM));
                        packetWrapper.write(Types1_7_6_10.COMPRESSED_NBT_ITEM, item);
                     }

                     packetWrapper.passthrough(Type.BOOLEAN);
                     packetWrapper.read(Type.INT);
                     packetWrapper.read(Type.INT);
                  }
               } else if (channel.equalsIgnoreCase("MC|Brand")) {
                  packetWrapper.write(Type.REMAINING_BYTES, ((String)packetWrapper.read(Type.STRING)).getBytes(StandardCharsets.UTF_8));
               }

               packetWrapper.cancel();
               packetWrapper.setId(-1);
               ByteBuf newPacketBuf = Unpooled.buffer();
               packetWrapper.writeToBuffer(newPacketBuf);
               PacketWrapper newWrapper = PacketWrapper.create(63, newPacketBuf, packetWrapper.user());
               newWrapper.passthrough(Type.STRING);
               if (newPacketBuf.readableBytes() <= 32767) {
                  newWrapper.write(Type.SHORT, (short)newPacketBuf.readableBytes());
                  newWrapper.send(Protocol1_7_6_10TO1_8.class);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.CAMERA, (ClientboundPacketType)null, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
               int entityId = (Integer)packetWrapper.read(Type.VAR_INT);
               int spectating = tracker.getSpectating();
               if (spectating != entityId) {
                  tracker.setSpectating(entityId);
               }

            });
         }
      });
      protocol.registerClientbound(ClientboundPackets1_8.TITLE, (ClientboundPacketType)null, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               packetWrapper.cancel();
               TitleRenderProvider titleRenderProvider = (TitleRenderProvider)Via.getManager().getProviders().get(TitleRenderProvider.class);
               if (titleRenderProvider != null) {
                  int action = (Integer)packetWrapper.read(Type.VAR_INT);
                  UUID uuid = Utils.getUUID(packetWrapper.user());
                  switch (action) {
                     case 0:
                        titleRenderProvider.setTitle(uuid, (String)packetWrapper.read(Type.STRING));
                        break;
                     case 1:
                        titleRenderProvider.setSubTitle(uuid, (String)packetWrapper.read(Type.STRING));
                        break;
                     case 2:
                        titleRenderProvider.setTimings(uuid, (Integer)packetWrapper.read(Type.INT), (Integer)packetWrapper.read(Type.INT), (Integer)packetWrapper.read(Type.INT));
                        break;
                     case 3:
                        titleRenderProvider.clear(uuid);
                        break;
                     case 4:
                        titleRenderProvider.reset(uuid);
                  }

               }
            });
         }
      });
      protocol.cancelClientbound(ClientboundPackets1_8.TAB_LIST);
      protocol.registerClientbound(ClientboundPackets1_8.RESOURCE_PACK, ClientboundPackets1_7.PLUGIN_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.create(Type.STRING, "MC|RPack");
            this.handler((packetWrapper) -> {
               ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();

               try {
                  Type.STRING.write(buf, (String)packetWrapper.read(Type.STRING));
                  packetWrapper.write(Type.SHORT_BYTE_ARRAY, (byte[])Type.REMAINING_BYTES.read(buf));
               } finally {
                  buf.release();
               }

            });
            this.map(Type.STRING, Type.NOTHING);
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.CHAT_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.handler((packetWrapper) -> {
               String msg = (String)packetWrapper.get(Type.STRING, 0);
               int gamemode = ((EntityTracker)packetWrapper.user().get(EntityTracker.class)).getGamemode();
               if (gamemode == 3 && msg.toLowerCase().startsWith("/stp ")) {
                  String username = msg.split(" ")[1];
                  GameProfileStorage storage = (GameProfileStorage)packetWrapper.user().get(GameProfileStorage.class);
                  GameProfileStorage.GameProfile profile = storage.get(username, true);
                  if (profile != null && profile.uuid != null) {
                     packetWrapper.cancel();
                     PacketWrapper teleportPacket = PacketWrapper.create(24, (ByteBuf)null, packetWrapper.user());
                     teleportPacket.write(Type.UUID, profile.uuid);
                     PacketUtil.sendToServer(teleportPacket, Protocol1_7_6_10TO1_8.class, true, true);
                  }
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.INTERACT_ENTITY, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT, Type.VAR_INT);
            this.map(Type.BYTE, Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int mode = (Integer)packetWrapper.get(Type.VAR_INT, 1);
               if (mode == 0) {
                  int entityId = (Integer)packetWrapper.get(Type.VAR_INT, 0);
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  EntityReplacement replacement = tracker.getEntityReplacement(entityId);
                  if (replacement instanceof ArmorStandReplacement) {
                     ArmorStandReplacement armorStand = (ArmorStandReplacement)replacement;
                     AABB boundingBox = armorStand.getBoundingBox();
                     PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
                     Vector3d pos = new Vector3d(playerPosition.getPosX(), playerPosition.getPosY() + 1.8, playerPosition.getPosZ());
                     double yaw = Math.toRadians((double)playerPosition.getYaw());
                     double pitch = Math.toRadians((double)playerPosition.getPitch());
                     Vector3d dir = new Vector3d(-Math.cos(pitch) * Math.sin(yaw), -Math.sin(pitch), Math.cos(pitch) * Math.cos(yaw));
                     Ray3d ray = new Ray3d(pos, dir);
                     Vector3d intersection = RayTracing.trace(ray, boundingBox, 5.0);
                     if (intersection != null) {
                        intersection.substract(boundingBox.getMin());
                        int modex = 2;
                        packetWrapper.set(Type.VAR_INT, 1, Integer.valueOf(modex));
                        packetWrapper.write(Type.FLOAT, (float)intersection.getX());
                        packetWrapper.write(Type.FLOAT, (float)intersection.getY());
                        packetWrapper.write(Type.FLOAT, (float)intersection.getZ());
                     }
                  }
               }
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_MOVEMENT, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               playerPosition.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_POSITION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE, Type.NOTHING);
            this.map(Type.DOUBLE);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               double x = (Double)packetWrapper.get(Type.DOUBLE, 0);
               double feetY = (Double)packetWrapper.get(Type.DOUBLE, 1);
               double z = (Double)packetWrapper.get(Type.DOUBLE, 2);
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               if (playerPosition.isPositionPacketReceived()) {
                  playerPosition.setPositionPacketReceived(false);
                  feetY -= 0.01;
                  packetWrapper.set(Type.DOUBLE, 1, feetY);
               }

               playerPosition.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
               playerPosition.setPos(x, feetY, z);
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               playerPosition.setYaw((Float)packetWrapper.get(Type.FLOAT, 0));
               playerPosition.setPitch((Float)packetWrapper.get(Type.FLOAT, 1));
               playerPosition.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_POSITION_AND_ROTATION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE);
            this.map(Type.DOUBLE, Type.NOTHING);
            this.map(Type.DOUBLE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.map(Type.BOOLEAN);
            this.handler((packetWrapper) -> {
               double x = (Double)packetWrapper.get(Type.DOUBLE, 0);
               double feetY = (Double)packetWrapper.get(Type.DOUBLE, 1);
               double z = (Double)packetWrapper.get(Type.DOUBLE, 2);
               float yaw = (Float)packetWrapper.get(Type.FLOAT, 0);
               float pitch = (Float)packetWrapper.get(Type.FLOAT, 1);
               PlayerPosition playerPosition = (PlayerPosition)packetWrapper.user().get(PlayerPosition.class);
               if (playerPosition.isPositionPacketReceived()) {
                  playerPosition.setPositionPacketReceived(false);
                  feetY = playerPosition.getReceivedPosY();
                  packetWrapper.set(Type.DOUBLE, 1, feetY);
               }

               playerPosition.setOnGround((Boolean)packetWrapper.get(Type.BOOLEAN, 0));
               playerPosition.setPos(x, feetY, z);
               playerPosition.setYaw(yaw);
               playerPosition.setPitch(pitch);
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_DIGGING, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int x = (Integer)packetWrapper.read(Type.INT);
               short y = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
               int z = (Integer)packetWrapper.read(Type.INT);
               packetWrapper.write(Type.POSITION, new Position(x, y, z));
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_BLOCK_PLACEMENT, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int x = (Integer)packetWrapper.read(Type.INT);
               short y = (Short)packetWrapper.read(Type.UNSIGNED_BYTE);
               int z = (Integer)packetWrapper.read(Type.INT);
               packetWrapper.write(Type.POSITION, new Position(x, y, z));
               packetWrapper.passthrough(Type.BYTE);
               Item item = (Item)packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
               item = ItemRewriter.toServer(item);
               packetWrapper.write(Type.ITEM, item);

               for(int i = 0; i < 3; ++i) {
                  packetWrapper.passthrough(Type.BYTE);
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.ANIMATION, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int entityId = (Integer)packetWrapper.read(Type.INT);
               int animation = (Byte)packetWrapper.read(Type.BYTE);
               if (animation != 1) {
                  packetWrapper.cancel();
                  switch (animation) {
                     case 3:
                        animation = 2;
                        break;
                     case 104:
                        animation = 0;
                        break;
                     case 105:
                        animation = 1;
                        break;
                     default:
                        return;
                  }

                  PacketWrapper entityAction = PacketWrapper.create(11, (ByteBuf)null, packetWrapper.user());
                  entityAction.write(Type.VAR_INT, entityId);
                  entityAction.write(Type.VAR_INT, Integer.valueOf(animation));
                  entityAction.write(Type.VAR_INT, 0);
                  PacketUtil.sendPacket(entityAction, Protocol1_7_6_10TO1_8.class, true, true);
               }
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.ENTITY_ACTION, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.INT, Type.VAR_INT);
            this.handler((packetWrapper) -> {
               packetWrapper.write(Type.VAR_INT, (Byte)packetWrapper.read(Type.BYTE) - 1);
            });
            this.map(Type.INT, Type.VAR_INT);
            this.handler((packetWrapper) -> {
               int action = (Integer)packetWrapper.get(Type.VAR_INT, 1);
               if (action == 3 || action == 4) {
                  PlayerAbilities abilities = (PlayerAbilities)packetWrapper.user().get(PlayerAbilities.class);
                  abilities.setSprinting(action == 3);
                  PacketWrapper abilitiesPacket = PacketWrapper.create(57, (ByteBuf)null, packetWrapper.user());
                  abilitiesPacket.write(Type.BYTE, abilities.getFlags());
                  abilitiesPacket.write(Type.FLOAT, abilities.isSprinting() ? abilities.getFlySpeed() * 2.0F : abilities.getFlySpeed());
                  abilitiesPacket.write(Type.FLOAT, abilities.getWalkSpeed());
                  PacketUtil.sendPacket(abilitiesPacket, Protocol1_7_6_10TO1_8.class);
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.STEER_VEHICLE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               boolean jump = (Boolean)packetWrapper.read(Type.BOOLEAN);
               boolean unmount = (Boolean)packetWrapper.read(Type.BOOLEAN);
               short flags = 0;
               if (jump) {
                  ++flags;
               }

               if (unmount) {
                  flags = (short)(flags + 2);
               }

               packetWrapper.write(Type.UNSIGNED_BYTE, flags);
               if (unmount) {
                  EntityTracker tracker = (EntityTracker)packetWrapper.user().get(EntityTracker.class);
                  if (tracker.getSpectating() != tracker.getPlayerId()) {
                     PacketWrapper sneakPacket = PacketWrapper.create(11, (ByteBuf)null, packetWrapper.user());
                     sneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
                     sneakPacket.write(Type.VAR_INT, 0);
                     sneakPacket.write(Type.VAR_INT, 0);
                     PacketWrapper unsneakPacket = PacketWrapper.create(11, (ByteBuf)null, packetWrapper.user());
                     unsneakPacket.write(Type.VAR_INT, tracker.getPlayerId());
                     unsneakPacket.write(Type.VAR_INT, 1);
                     unsneakPacket.write(Type.VAR_INT, 0);
                     PacketUtil.sendToServer(sneakPacket, Protocol1_7_6_10TO1_8.class);
                  }
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.UPDATE_SIGN, new PacketRemapper() {
         public void registerMap() {
            this.handler((packetWrapper) -> {
               int x = (Integer)packetWrapper.read(Type.INT);
               short y = (Short)packetWrapper.read(Type.SHORT);
               int z = (Integer)packetWrapper.read(Type.INT);
               packetWrapper.write(Type.POSITION, new Position(x, y, z));

               for(int i = 0; i < 4; ++i) {
                  String line = (String)packetWrapper.read(Type.STRING);
                  line = ChatUtil.legacyToJson(line);
                  packetWrapper.write(Type.COMPONENT, JsonParser.parseString(line));
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLAYER_ABILITIES, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.BYTE);
            this.map(Type.FLOAT);
            this.map(Type.FLOAT);
            this.handler((packetWrapper) -> {
               PlayerAbilities abilities = (PlayerAbilities)packetWrapper.user().get(PlayerAbilities.class);
               if (abilities.isAllowFly()) {
                  byte flags = (Byte)packetWrapper.get(Type.BYTE, 0);
                  abilities.setFlying((flags & 2) == 2);
               }

               packetWrapper.set(Type.FLOAT, 0, abilities.getFlySpeed());
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.TAB_COMPLETE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.create(Type.OPTIONAL_POSITION, (Object)null);
            this.handler((packetWrapper) -> {
               String msg = (String)packetWrapper.get(Type.STRING, 0);
               if (msg.toLowerCase().startsWith("/stp ")) {
                  packetWrapper.cancel();
                  String[] args = msg.split(" ");
                  if (args.length <= 2) {
                     String prefix = args.length == 1 ? "" : args[1];
                     GameProfileStorage storage = (GameProfileStorage)packetWrapper.user().get(GameProfileStorage.class);
                     List profiles = storage.getAllWithPrefix(prefix, true);
                     PacketWrapper tabComplete = PacketWrapper.create(58, (ByteBuf)null, packetWrapper.user());
                     tabComplete.write(Type.VAR_INT, profiles.size());
                     Iterator var7 = profiles.iterator();

                     while(var7.hasNext()) {
                        GameProfileStorage.GameProfile profile = (GameProfileStorage.GameProfile)var7.next();
                        tabComplete.write(Type.STRING, profile.name);
                     }

                     PacketUtil.sendPacket(tabComplete, Protocol1_7_6_10TO1_8.class);
                  }
               }

            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.CLIENT_SETTINGS, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.map(Type.BYTE);
            this.map(Type.BYTE);
            this.map(Type.BOOLEAN);
            this.map(Type.BYTE, Type.NOTHING);
            this.handler((packetWrapper) -> {
               boolean cape = (Boolean)packetWrapper.read(Type.BOOLEAN);
               packetWrapper.write(Type.UNSIGNED_BYTE, (short)(cape ? 127 : 126));
            });
         }
      });
      protocol.registerServerbound(ServerboundPackets1_7.PLUGIN_MESSAGE, new PacketRemapper() {
         public void registerMap() {
            this.map(Type.STRING);
            this.map(Type.SHORT, Type.NOTHING);
            this.handler((packetWrapper) -> {
               switch ((String)packetWrapper.get(Type.STRING, 0)) {
                  case "MC|TrSel":
                     packetWrapper.passthrough(Type.INT);
                     packetWrapper.read(Type.REMAINING_BYTES);
                     break;
                  case "MC|ItemName":
                     byte[] data = (byte[])packetWrapper.read(Type.REMAINING_BYTES);
                     String name = new String(data, StandardCharsets.UTF_8);
                     packetWrapper.write(Type.STRING, name);
                     Windows windows = (Windows)packetWrapper.user().get(Windows.class);
                     PacketWrapper updateCost = PacketWrapper.create(49, (ByteBuf)null, packetWrapper.user());
                     updateCost.write(Type.UNSIGNED_BYTE, windows.anvilId);
                     updateCost.write(Type.SHORT, Short.valueOf((short)0));
                     updateCost.write(Type.SHORT, windows.levelCost);
                     PacketUtil.sendPacket(updateCost, Protocol1_7_6_10TO1_8.class, true, true);
                     break;
                  case "MC|BEdit":
                  case "MC|BSign":
                     Item book = (Item)packetWrapper.read(Types1_7_6_10.COMPRESSED_NBT_ITEM);
                     CompoundTag tag = book.tag();
                     if (tag != null && tag.contains("pages")) {
                        ListTag pages = (ListTag)tag.get("pages");

                        for(int i = 0; i < pages.size(); ++i) {
                           StringTag page = (StringTag)pages.get(i);
                           String value = page.getValue();
                           value = ChatUtil.legacyToJson(value);
                           page.setValue(value);
                        }
                     }

                     packetWrapper.write(Type.ITEM, book);
                     break;
                  case "MC|Brand":
                     packetWrapper.write(Type.STRING, new String((byte[])packetWrapper.read(Type.REMAINING_BYTES), StandardCharsets.UTF_8));
               }

            });
         }
      });
   }
}
