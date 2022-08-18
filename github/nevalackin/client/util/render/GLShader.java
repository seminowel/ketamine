package io.github.nevalackin.client.util.render;

import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL20;

public class GLShader {
   private int program = GL20.glCreateProgram();
   private final Map uniformLocationMap = new HashMap();

   public GLShader(String vertexSource, String fragSource) {
      GL20.glAttachShader(this.program, createShader(vertexSource, 35633));
      GL20.glAttachShader(this.program, createShader(fragSource, 35632));
      GL20.glLinkProgram(this.program);
      int status = GL20.glGetProgrami(this.program, 35714);
      if (status == 0) {
         this.program = -1;
      } else {
         this.setupUniforms();
      }
   }

   private static int createShader(String source, int type) {
      int shader = GL20.glCreateShader(type);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      int status = GL20.glGetShaderi(shader, 35713);
      return status == 0 ? -1 : shader;
   }

   public void use() {
      GL20.glUseProgram(this.program);
      this.updateUniforms();
   }

   public int getProgram() {
      return this.program;
   }

   public void setupUniforms() {
   }

   public void updateUniforms() {
   }

   public void setupUniform(String uniform) {
      this.uniformLocationMap.put(uniform, GL20.glGetUniformLocation(this.program, uniform));
   }

   public int getUniformLocation(String uniform) {
      return (Integer)this.uniformLocationMap.get(uniform);
   }
}
