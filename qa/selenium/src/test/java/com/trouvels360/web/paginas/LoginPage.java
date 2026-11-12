package com.trouvels360.web.paginas;

import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {
    public LoginPage(WebDriver d) { super(d); }

    public LoginPage abrir() { irA("/auth/login"); visible("login-email"); return this; }

    public void ingresar(String email, String password) {
        escribir("login-email", email);
        escribir("login-password", password);
        click("login-submit");
    }

    public String error() { return texto("login-error"); }
}
