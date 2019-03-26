package actions;

import models.User;
import java.util.List;
import play.libs.typedmap.TypedKey;

/**
* Manages objects that can be passed from middleware to controllers
*/
public class ActionState {
   public static final TypedKey<User> USER = TypedKey.create("user");
   public static final TypedKey<List<String>> ROLES = TypedKey.create("roles");
}