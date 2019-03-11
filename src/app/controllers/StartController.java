package controllers;

import models.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.*;

import views.html.*;

import javax.inject.Inject;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains actions to handle HTTP requests on the start page.
 */
public class StartController extends Controller {

    private final Form<AccountData> form;
    private MessagesApi messagesApi;
    private final List<Account> accounts;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;

    @Inject
    /**
     * Used to initialise the account form. messagesApi and also creates a list of example
     * account data to use before a database is established.
     */
    public StartController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(AccountData.class);
        this.messagesApi = messagesApi;
        //Test data for accounts. should be replaced with account data from database.
        this.accounts = com.google.common.collect.Lists.newArrayList(
                new Account("dave@gmail.com", "password"),
                new Account("steve123@live.com", "password"),
                new Account("kitkat123@uclive.ac.nz", "password")
        );
    }

    /**
     * Displays the start page. Called with the / URL and uses a GET request.
     * Takes the user to the home page if they are already logged in.
     *
     * @param request
     * @return displays the start page.
     */
    public Result index(Http.Request request) {
        return request.session()
                .getOptional("connected")
                .map(user -> redirect(routes.ApplicationController.index()))
            .orElseGet(() -> ok(start.render(asScala(accounts), form, request, messagesApi.preferred(request))));
    }

    /**
     * Uses a POST request at /login to validate logging in to an account.
     * Will display error messages if email/password are incorrect.
     * If the account details are in the database. The user will be logged in and taken to
     * the home page.
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    public Result login(Http.Request request) {
        final Form<AccountData> loginForm = form.bindFromRequest(request);

        if (loginForm.hasErrors()) {
            logger.error("errors = {}", loginForm.errors());
            return badRequest();
        } else {
            AccountData data = loginForm.get();
            for(Account account: accounts) {
                if(account.getEmail().equals(data.getEmail())) {
                    //email is in database
                    if(account.getPassword().equals(data.getPassword())) {
                        //Correct credentials. can log in
                        return redirect(routes.ApplicationController.index()).addingToSession(request, "connected", account.email);
                    }else {
                        //Incorrect password
                        return redirect(routes.StartController.index()).flashing("loginError", "Incorrect password!");
                    }
                }
            }
            //email not in database
            return redirect(routes.StartController.index()).flashing("loginError", "Email: " + data.getEmail() + " is not registered!");
        }
    }

    /**
     * Uses a POST request at /signup to validate creating a new account.
     * Will display error messages if email/password are incorrect.
     * If the account details are in the database. The user will be logged in and taken to
     * the home page.
     *
     * @param request
     * @return a http result; a redirect if the user credentials are correct, and a bad request in other cases.
     */
    public Result signUp(Http.Request request) {
        final Form<AccountData> boundForm = form.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            logger.error("errors = {}", boundForm.errors());
            return badRequest();
        } else {
            AccountData data = boundForm.get();

            for(Account account: accounts) {
                if(account.getEmail().equals(data.getEmail())) {
                    //email is already registered, show error message about this
                    return redirect(routes.StartController.index()).flashing("signUpError", "Email: " + data.getEmail() + " is already in use!");
                }
            }

            //Create new account. Should run insert db operation, then redirect to the profile creation page.
            Account newUser = new Account(data.getEmail(), data.getPassword());
            accounts.add(newUser);
            return redirect(routes.ProfileController.index()).addingToSession(request, "connected", newUser.email);
        }

    }


}
