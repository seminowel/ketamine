package fr.litarvan.openauth.microsoft.model.request;

public class XboxLoginRequest {
   private final Object Properties;
   private final String RelyingParty;
   private final String TokenType;

   public XboxLoginRequest(Object Properties, String RelyingParty, String TokenType) {
      this.Properties = Properties;
      this.RelyingParty = RelyingParty;
      this.TokenType = TokenType;
   }

   public Object getProperties() {
      return this.Properties;
   }

   public String getSiteName() {
      return this.RelyingParty;
   }

   public String getTokenType() {
      return this.TokenType;
   }
}
