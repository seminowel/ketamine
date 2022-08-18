package net.minecraft.client.gui;

import io.github.nevalackin.client.impl.core.KetamineClient;
import io.github.nevalackin.client.impl.ui.account.GuiAccountManager;
import io.github.nevalackin.client.util.render.GLShader;
import java.io.IOException;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class GuiMainMenu extends GuiScreen implements GuiYesNoCallback {
   private long initTime = System.currentTimeMillis();
   private final GLShader czBackgroundShader = new GLShader("#version 120 \n\nvoid main() {\n    gl_Position = gl_Vertex;\n}", "//#  Greetz from DK.... thank you ;\n#ifdef GL_ES\nprecision mediump float;\n#endif\n\n#extension GL_OES_standard_derivatives : enable\n\nuniform float time;\nuniform vec2 mouse;\nuniform vec2 resolution;\n\nconst vec3 red = vec3(0.874, 0.049, 0.077);\nconst vec3 blue = vec3(0.040,0.332,0.6291);\nconst vec3 white = vec3(1, 1, 1);\nvec3 col1;\nconst float PI = 3.1415926535;\nvoid main( void ) {\n\n\tvec2 p = 2.0*( gl_FragCoord.xy / resolution.xy ) -1.0; \n\tp.x *= resolution.x/resolution.y; \n\t\n\tp.x += sin(p.y+time*2.0)*.05;\n\tp.y += sin(p.x*2.0-time*2.0)*.2;\n\t\n\t\n\t\n\tvec2 uv = (gl_FragCoord.xy*2.-resolution.xy)/resolution.y+1.1;\n\t\n\tfloat w = sin((uv.x + uv.y - time * .5 + sin(1.5 * uv.x + 4.5 * uv.y) * PI * .3) * PI * .6); // fake waviness factor\n \n\t\tcol1 = vec3(0.80,0.80,0.0);\n\t\tcol1 = mix(col1, red, smoothstep(.01, .025, uv.y+w*.02));\n\t\t \n\t\tcol1 += w * .2;\n\t\n\tvec3 col = col1; \n\t\n\tif (p.y > 0.0) col = white+w*0.3;\n\t\n\tfloat tw = (1.0 - abs(p.y * resolution.x/resolution.y)) * 0.5;\n\t\n\tif (p.x < tw) col = blue+w*0.3;\n\t\n\t//if(abs(p.x) > 1.60) col = col1;\n\t//if(abs(p.y) > 1.0) col = col1;\n\t\n\tgl_FragColor = vec4(col , 1.0); \n}") {
      public void setupUniforms() {
         this.setupUniform("time");
         this.setupUniform("resolution");
      }

      public void updateUniforms() {
         GL20.glUniform1f(this.getUniformLocation("time"), (float)(System.currentTimeMillis() - GuiMainMenu.this.initTime) / 1000.0F);
         GL20.glUniform2f(this.getUniformLocation("resolution"), (float)GuiMainMenu.this.mc.displayWidth, (float)GuiMainMenu.this.mc.displayHeight);
      }
   };
   private final GLShader backgroundShader = new GLShader("#version 120 \n\nvoid main() {\n    gl_Position = gl_Vertex;\n}", "#ifdef GL_ES\nprecision mediump float;\n#endif\n\n// glslsandbox uniforms\nuniform float time;\nuniform vec2 resolution;\n\n// shadertoy emulation\n#define iTime time\n#define iResolution resolution\n\n// --------[ Original ShaderToy begins here ]---------- //\n#define clamp01(x) clamp(x,0.,1.)\nconst float M_PI = 3.14159265358979323846264338327950288;\n\nconst vec3 WHITE_STAR_COLOR = vec3(1.);\nconst vec3 BLUE_STAR_COLOR = vec3(0.35294117647,0.42352941176,0.67058823529);\nconst vec3 RED_STAR_COLOR = vec3(0.72549019607,0.05098039215,0.30980392156);\nconst vec3 BACKGROUND_BLACK = vec3(0.01176470588);\nconst vec3 MOUNTAIN_COLOR = vec3(0.0862745098,0.1725490196,0.30588235294);\n\nconst float STAR_MAX_SIZE = 0.2;\nconst float STAR_MIN_SIZE = 0.05;\n\nfloat remap01(float a, float b, float t)\n{\n    return clamp01((t-a)/(b-a));\n}\n\nfloat Rand(float i)\n{\n    return fract(sin(i * 23325.) * 35543.);\n}\n\nvec4 Rand4(float i)\n{\n    return fract(sin(i * vec4(23325.,53464.,76543.,12312)) * vec4(35543.,63454.,23454.,87651));\n}\n\nvec2 within(vec2 uv, vec4 rect)\n{\n    return (uv - rect.xy)/(rect.zw-rect.xy);\n}\n\nfloat Star(vec2 uv, float radius, float iradius, float rotation)\n{\n    vec2 st = vec2 (atan(uv.x,uv.y), length(uv));\n    \n    float n = 5.;\n    \n    float a  = st.x + rotation;\n    float sa = M_PI/n;\n    \n    float p = fract(a/(2.0*sa));\n          p = abs(p-0.5)*2.0;\n    \n    float cr = cos(sa)*radius;\n\n    float pa  = p*sa;\n    float cpr = cr/cos(sa-pa);\n\n    float xp = cpr*sin(pa);\n    float yp = cpr*cos(pa);\n    float xi = sin(sa*0.5)*iradius*radius;\n    float yi = cos(sa*0.5)*iradius*radius;\n  \n    float yx = (xi*yp*radius)/(xp*radius-xp*yi+xi*yp);\n\n    float border = yx/cos(pa);\n   \n    return smoothstep(0.,0.1,1. -st.y/border);\n}\n\nvec3 ThreeStar(vec2 uv, float rotation){\n    \n    float radius  = 0.5;\n    float iradius = .5;  \n\n    float redStar   = Star(uv - vec2(.055,-.09), radius, iradius, rotation);\n    float blueStar  = Star(uv - vec2(.0,.0), radius, iradius, rotation);\n  \n    vec3 whiteStarColor = blueStar * redStar * WHITE_STAR_COLOR;\n    vec3 redStarColor   = redStar * RED_STAR_COLOR;\n    vec3 blueStarColor  = blueStar * BLUE_STAR_COLOR;\n      \n    return whiteStarColor + redStarColor + blueStarColor;\n}\n\nvec3 StarSky(vec2 uv){\n    \n    float t = iTime*0.025;\n    float sr = iResolution.x/iResolution.y;\n\n    vec3 color = vec3(0.);\n    \n    const float sc = 15.;\n    const float s = 1./ sc;\n    const float at = 0.01;\n    \n    const float stt = 0.1;\n    const vec2 spading = vec2(0.95,0.9);\n    \n    for(float i = 0.; i < 1.; i +=s)\n    {    \n        float ci = t + i;\n        float fci = fract(ci);\n        float sit = floor(ci);\n    \n        float btout = pow(smoothstep(0.9,1.0,fci),20.);        \n        float ibtout = 1.-btout;\n        \n        float bt = ci * 500.;\n        float h1 = Rand(floor(bt));\n        float h2 = Rand(floor(bt+1.));\n        float bumps = mix(h1, h2, fract(bt))*.1;\n        bumps = pow(bumps,3.) * 10.;\n        float shake = smoothstep(0.9,1.0,fci) * bumps;\n        \n        float btin = pow(smoothstep(0.,0.1,fci),2.);\n        \n        vec4 rm = Rand4(i + sit);\n        \n        float rs = STAR_MIN_SIZE + rm.x * (STAR_MAX_SIZE-STAR_MIN_SIZE) * ibtout;\n        vec2 rp  = vec2((rm.y*2. - 1.)*sr + shake ,rm.z + shake) * spading;\n        \n        rp  = (vec2(sr * 0.9,-0.5) - rp) * btout + rp;\n        \n        vec4 rb  = vec4(rp.x, rp.y, rp.x + rs, rp.y + rs);\n        vec2 st  = within(uv, rb);\n        \n        vec3 ts = ThreeStar(st,M_PI);\n        ts *= ibtout;\n        ts *= btin;\n        color += ts;\n        \n    }\n    \n    return color;\n}\n\nvec3 Background(vec2 uv)\n{ \n    vec3 dc = BACKGROUND_BLACK;\n    vec3 bc = BLUE_STAR_COLOR;\n    vec3 rc = RED_STAR_COLOR;\n    vec3 wc = WHITE_STAR_COLOR;\n    \n    vec3 color = mix (bc, dc, smoothstep(-0.8,-0.1,uv.y));\n    color = mix (rc, color, smoothstep(-1.0,-0.55,uv.y));\n    color = mix (wc, color, smoothstep(-1.4,-0.7,uv.y));\n    return color;\n}\n\nvec3 MountainsOverlay(vec2 uv, vec3 color, float mountainLevel)\n{\n    float pos = uv.x + iTime * 0.05;\n    float bt = pos * 5.;\n    float h1 = Rand(floor(bt));\n    float h2 = Rand(floor(bt+1.));\n    float bumps = mix(h1, h2, fract(bt))*.1;\n    bumps = pow(bumps,3.) * 300.;\n    \n    mountainLevel += bumps;\n    \n    float mask = 1.-smoothstep(mountainLevel,mountainLevel*0.99, uv.y);\n    vec3 mc =  smoothstep(-1.2, mountainLevel+0.1, uv.y) * MOUNTAIN_COLOR;\n    color = mix(color,mc, mask);\n    \n    return color;\n}\n\nvoid mainImage( out vec4 fragColor, in vec2 fragCoord )\n{\n    vec2 uv = (fragCoord*2.-iResolution.xy)/iResolution.y;\n        \n    vec3 color = StarSky(uv) + Background(uv);\n    color = MountainsOverlay(uv,color,-0.9);\n    \n    fragColor = vec4(color,1.0);\n}\n// --------[ Original ShaderToy ends here ]---------- //\n\nvoid main(void)\n{\n    mainImage(gl_FragColor, gl_FragCoord.xy);\n}") {
      public void setupUniforms() {
         this.setupUniform("time");
         this.setupUniform("resolution");
      }

      public void updateUniforms() {
         GL20.glUniform1f(this.getUniformLocation("time"), (float)(System.currentTimeMillis() - GuiMainMenu.this.initTime) / 1000.0F);
         GL20.glUniform2f(this.getUniformLocation("resolution"), (float)GuiMainMenu.this.mc.displayWidth, (float)GuiMainMenu.this.mc.displayHeight);
      }
   };

   public GuiMainMenu() {
      KetamineClient.getInstance().updateDiscordRPC("In Main Menu");
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   protected void keyTyped(char typedChar, int keyCode) throws IOException {
   }

   public void initGui() {
      int j = this.height / 4 + 48;
      this.addSingleplayerMultiplayerButtons(j, 24);
      this.buttonList.add(new GuiButton(0, this.width / 2 - 100, j + 72 + 12 + 12, 98, 20, I18n.format("menu.options")));
      this.buttonList.add(new GuiButton(4, this.width / 2 + 2, j + 72 + 12 + 12, 98, 20, I18n.format("menu.quit")));
      this.initTime = System.currentTimeMillis();
   }

   private void addSingleplayerMultiplayerButtons(int p_73969_1_, int p_73969_2_) {
      this.buttonList.add(new GuiButton(1, this.width / 2 - 100, p_73969_1_, I18n.format("menu.singleplayer")));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 100, p_73969_1_ + p_73969_2_, I18n.format("menu.multiplayer")));
      this.buttonList.add(new GuiButton(3, this.width / 2 - 100, p_73969_1_ + p_73969_2_ * 2, "Alt Manager"));
   }

   protected void actionPerformed(GuiButton button) throws IOException {
      switch (button.id) {
         case 0:
            this.mc.displayGuiScreen(new GuiOptions(this, this.mc.gameSettings));
            break;
         case 1:
            this.mc.displayGuiScreen(new GuiSelectWorld(this));
            break;
         case 2:
            this.mc.displayGuiScreen(new GuiMultiplayer(this));
            break;
         case 3:
            this.mc.displayGuiScreen(new GuiAccountManager(this));
            break;
         case 4:
            this.mc.shutdown();
      }

   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      GL11.glDisable(2884);
      boolean czechMode = this.mc.gameSettings.language.equals("cs_CZ");
      GLShader bgShader = czechMode ? this.czBackgroundShader : this.backgroundShader;
      bgShader.use();
      GL11.glBegin(7);
      GL11.glVertex2i(-1, -1);
      GL11.glVertex2i(-1, 1);
      GL11.glVertex2i(1, 1);
      GL11.glVertex2i(1, -1);
      GL11.glEnd();
      GL20.glUseProgram(0);
      if (czechMode) {
         GL11.glScaled(2.0, 2.0, 1.0);
         this.fontRendererObj.drawString("§LAhoj ty hloupa kurvo!", 2.0, (double)this.height / 2.0 - 9.0 - 2.0, -16777216);
         GL11.glScaled(0.5, 0.5, 1.0);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }
}
