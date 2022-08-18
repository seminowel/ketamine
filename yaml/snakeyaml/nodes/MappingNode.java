package org.yaml.snakeyaml.nodes;

import java.util.Iterator;
import java.util.List;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.Mark;

public class MappingNode extends CollectionNode {
   private List value;
   private boolean merged;

   public MappingNode(Tag tag, boolean resolved, List value, Mark startMark, Mark endMark, DumperOptions.FlowStyle flowStyle) {
      super(tag, startMark, endMark, flowStyle);
      this.merged = false;
      if (value == null) {
         throw new NullPointerException("value in a Node is required.");
      } else {
         this.value = value;
         this.resolved = resolved;
      }
   }

   public MappingNode(Tag tag, List value, DumperOptions.FlowStyle flowStyle) {
      this(tag, true, value, (Mark)null, (Mark)null, (DumperOptions.FlowStyle)flowStyle);
   }

   /** @deprecated */
   @Deprecated
   public MappingNode(Tag tag, boolean resolved, List value, Mark startMark, Mark endMark, Boolean flowStyle) {
      this(tag, resolved, value, startMark, endMark, DumperOptions.FlowStyle.fromBoolean(flowStyle));
   }

   /** @deprecated */
   @Deprecated
   public MappingNode(Tag tag, List value, Boolean flowStyle) {
      this(tag, value, DumperOptions.FlowStyle.fromBoolean(flowStyle));
   }

   public NodeId getNodeId() {
      return NodeId.mapping;
   }

   public List getValue() {
      return this.value;
   }

   public void setValue(List mergedValue) {
      this.value = mergedValue;
   }

   public void setOnlyKeyType(Class keyType) {
      Iterator i$ = this.value.iterator();

      while(i$.hasNext()) {
         NodeTuple nodes = (NodeTuple)i$.next();
         nodes.getKeyNode().setType(keyType);
      }

   }

   public void setTypes(Class keyType, Class valueType) {
      Iterator i$ = this.value.iterator();

      while(i$.hasNext()) {
         NodeTuple nodes = (NodeTuple)i$.next();
         nodes.getValueNode().setType(valueType);
         nodes.getKeyNode().setType(keyType);
      }

   }

   public String toString() {
      StringBuilder buf = new StringBuilder();

      for(Iterator i$ = this.getValue().iterator(); i$.hasNext(); buf.append(" }")) {
         NodeTuple node = (NodeTuple)i$.next();
         buf.append("{ key=");
         buf.append(node.getKeyNode());
         buf.append("; value=");
         if (node.getValueNode() instanceof CollectionNode) {
            buf.append(System.identityHashCode(node.getValueNode()));
         } else {
            buf.append(node.toString());
         }
      }

      String values = buf.toString();
      return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", values=" + values + ")>";
   }

   public void setMerged(boolean merged) {
      this.merged = merged;
   }

   public boolean isMerged() {
      return this.merged;
   }
}
