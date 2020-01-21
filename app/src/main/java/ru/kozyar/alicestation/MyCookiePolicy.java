package ru.kozyar.alicestation;

import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;

public class MyCookiePolicy implements CookiePolicy {
    @Override
    public boolean shouldAccept(URI uri, HttpCookie cookie) {
        return true;
    }
}