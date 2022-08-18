package us.myles.ViaVersion.api.protocol;

import com.google.common.base.Preconditions;
import com.viaversion.viaversion.api.protocol.version.VersionRange;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectMap;
import com.viaversion.viaversion.libs.fastutil.ints.Int2ObjectOpenHashMap;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/** @deprecated */
@Deprecated
public class ProtocolVersion {
   private static final Int2ObjectMap versions = new Int2ObjectOpenHashMap();
   private static final List versionList = new ArrayList();
   public static final ProtocolVersion v1_4_6 = register(51, "1.4.6/7", new VersionRange("1.4", 6, 7));
   public static final ProtocolVersion v1_5_1 = register(60, "1.5.1");
   public static final ProtocolVersion v1_5_2 = register(61, "1.5.2");
   public static final ProtocolVersion v_1_6_1 = register(73, "1.6.1");
   public static final ProtocolVersion v_1_6_2 = register(74, "1.6.2");
   public static final ProtocolVersion v_1_6_3 = register(77, "1.6.3");
   public static final ProtocolVersion v_1_6_4 = register(78, "1.6.4");
   public static final ProtocolVersion v1_7_1 = register(4, "1.7-1.7.5", new VersionRange("1.7", 0, 5));
   public static final ProtocolVersion v1_7_6 = register(5, "1.7.6-1.7.10", new VersionRange("1.7", 6, 10));
   public static final ProtocolVersion v1_8 = register(47, "1.8.x");
   public static final ProtocolVersion v1_9 = register(107, "1.9");
   public static final ProtocolVersion v1_9_1 = register(108, "1.9.1");
   public static final ProtocolVersion v1_9_2 = register(109, "1.9.2");
   public static final ProtocolVersion v1_9_3 = register(110, "1.9.3/4", new VersionRange("1.9", 3, 4));
   public static final ProtocolVersion v1_10 = register(210, "1.10.x");
   public static final ProtocolVersion v1_11 = register(315, "1.11");
   public static final ProtocolVersion v1_11_1 = register(316, "1.11.1/2", new VersionRange("1.11", 1, 2));
   public static final ProtocolVersion v1_12 = register(335, "1.12");
   public static final ProtocolVersion v1_12_1 = register(338, "1.12.1");
   public static final ProtocolVersion v1_12_2 = register(340, "1.12.2");
   public static final ProtocolVersion v1_13 = register(393, "1.13");
   public static final ProtocolVersion v1_13_1 = register(401, "1.13.1");
   public static final ProtocolVersion v1_13_2 = register(404, "1.13.2");
   public static final ProtocolVersion v1_14 = register(477, "1.14");
   public static final ProtocolVersion v1_14_1 = register(480, "1.14.1");
   public static final ProtocolVersion v1_14_2 = register(485, "1.14.2");
   public static final ProtocolVersion v1_14_3 = register(490, "1.14.3");
   public static final ProtocolVersion v1_14_4 = register(498, "1.14.4");
   public static final ProtocolVersion v1_15 = register(573, "1.15");
   public static final ProtocolVersion v1_15_1 = register(575, "1.15.1");
   public static final ProtocolVersion v1_15_2 = register(578, "1.15.2");
   public static final ProtocolVersion v1_16 = register(735, "1.16");
   public static final ProtocolVersion v1_16_1 = register(736, "1.16.1");
   public static final ProtocolVersion v1_16_2 = register(751, "1.16.2");
   public static final ProtocolVersion v1_16_3 = register(753, "1.16.3");
   public static final ProtocolVersion v1_16_4 = register(754, "1.16.4/5", new VersionRange("1.16", 4, 5));
   public static final ProtocolVersion v1_17 = register(755, "1.17");
   public static final ProtocolVersion v1_17_1 = register(756, "1.17.1");
   public static final ProtocolVersion v1_18 = register(757, "1.18/1.18.1", new VersionRange("1.18", 0, 1));
   public static final ProtocolVersion v1_18_2 = register(758, "1.18.2");
   public static final ProtocolVersion v1_19 = register(759, "1.19");
   public static final ProtocolVersion unknown = register(-1, "UNKNOWN");
   private final int version;
   private final int snapshotVersion;
   private final String name;
   private final boolean versionWildcard;
   private final Set includedVersions;

   public static ProtocolVersion register(int version, String name) {
      return register(version, -1, name);
   }

   public static ProtocolVersion register(int version, int snapshotVersion, String name) {
      return register(version, snapshotVersion, name, (VersionRange)null);
   }

   public static ProtocolVersion register(int version, String name, @Nullable VersionRange versionRange) {
      return register(version, -1, name, versionRange);
   }

   public static ProtocolVersion register(int version, int snapshotVersion, String name, @Nullable VersionRange versionRange) {
      ProtocolVersion protocol = new ProtocolVersion(version, snapshotVersion, name, versionRange);
      versionList.add(protocol);
      versions.put(protocol.getVersion(), protocol);
      if (protocol.isSnapshot()) {
         versions.put(protocol.getFullSnapshotVersion(), protocol);
      }

      return protocol;
   }

   public static boolean isRegistered(int id) {
      return versions.containsKey(id);
   }

   public static @NonNull ProtocolVersion getProtocol(int id) {
      ProtocolVersion protocolVersion = (ProtocolVersion)versions.get(id);
      return protocolVersion != null ? protocolVersion : new ProtocolVersion(id, "Unknown (" + id + ")");
   }

   public static int getIndex(ProtocolVersion version) {
      return versionList.indexOf(version);
   }

   public static List getProtocols() {
      return Collections.unmodifiableList(new ArrayList(versions.values()));
   }

   public static @Nullable ProtocolVersion getClosest(String protocol) {
      ObjectIterator var1 = versions.values().iterator();

      ProtocolVersion version;
      String name;
      String majorVersion;
      label33:
      do {
         do {
            if (!var1.hasNext()) {
               return null;
            }

            version = (ProtocolVersion)var1.next();
            name = version.getName();
            if (name.equals(protocol)) {
               return version;
            }

            if (version.isVersionWildcard()) {
               majorVersion = name.substring(0, name.length() - 2);
               continue label33;
            }
         } while(!version.isRange() || !version.getIncludedVersions().contains(protocol));

         return version;
      } while(!majorVersion.equals(protocol) && !protocol.startsWith(name.substring(0, name.length() - 1)));

      return version;
   }

   public ProtocolVersion(int version, String name) {
      this(version, -1, name, (VersionRange)null);
   }

   public ProtocolVersion(int version, int snapshotVersion, String name, @Nullable VersionRange versionRange) {
      this.version = version;
      this.snapshotVersion = snapshotVersion;
      this.name = name;
      this.versionWildcard = name.endsWith(".x");
      Preconditions.checkArgument(!this.versionWildcard || versionRange == null, "A version cannot be a wildcard and a range at the same time!");
      if (versionRange != null) {
         this.includedVersions = new LinkedHashSet();

         for(int i = versionRange.rangeFrom(); i <= versionRange.rangeTo(); ++i) {
            if (i == 0) {
               this.includedVersions.add(versionRange.baseVersion());
            }

            this.includedVersions.add(versionRange.baseVersion() + "." + i);
         }
      } else {
         this.includedVersions = Collections.singleton(name);
      }

   }

   public int getVersion() {
      return this.version;
   }

   public int getSnapshotVersion() {
      Preconditions.checkArgument(this.isSnapshot());
      return this.snapshotVersion;
   }

   public int getFullSnapshotVersion() {
      Preconditions.checkArgument(this.isSnapshot());
      return 1073741824 | this.snapshotVersion;
   }

   public int getOriginalVersion() {
      return this.snapshotVersion == -1 ? this.version : 1073741824 | this.snapshotVersion;
   }

   public boolean isKnown() {
      return this.version != -1;
   }

   public boolean isRange() {
      return this.includedVersions.size() != 1;
   }

   public Set getIncludedVersions() {
      return Collections.unmodifiableSet(this.includedVersions);
   }

   public boolean isVersionWildcard() {
      return this.versionWildcard;
   }

   public String getName() {
      return this.name;
   }

   public boolean isSnapshot() {
      return this.snapshotVersion != -1;
   }

   public int getId() {
      return this.version;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ProtocolVersion that = (ProtocolVersion)o;
         return this.version == that.version;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.version;
   }

   public String toString() {
      return String.format("%s (%d)", this.name, this.version);
   }
}
