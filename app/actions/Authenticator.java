// package actions;

// import play.mvc.Http;
// import play.mvc.Result;
// import play.mvc.Security;

// import java.util.concurrent.CompletableFuture;

// import models.User;

// public class Authenticator extends Action.Simple {  
//     public CompletableFuture<Result> call(Http.Context ctx) throws Throwable {
//         String token = getTokenFromHeader(ctx);
//         if (token != null) {
//               User user = User.find.where().eq("authToken", token).findUnique();
//               if (user != null) {
//                   ctx.request().setUsername(user.username);
//                   return delegate.call(ctx);
//               }
//         }
//         Result unauthorized = Results.unauthorized("unauthorized");
//         return F.Promise.pure(unauthorized);
//     }

//     private String getTokenFromHeader(Http.Context ctx) {
//         String[] authTokenHeaderValues = ctx.request().headers().get("X-AUTH-TOKEN");
//         if ((authTokenHeaderValues != null) && (authTokenHeaderValues.length == 1) && (authTokenHeaderValues[0] != null)) {
//             return authTokenHeaderValues[0];
//         }
//         return null;
//     }
// }