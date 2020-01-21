package ru.kozyar.alicestation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.*;
import java.util.List;

public class MyCookieStore implements CookieStore, Runnable {

    public CookieStore store;
    public String fileName;


    public MyCookieStore(String fileName) {

        this.fileName = fileName;
        store = new CookieManager().getCookieStore();
        String s = Filer.getFile(fileName);

        if (s.equals("")) {
            s = "[]";
        }

        JSONArray arr = null;
        try {
            arr = new JSONArray(s);
        } catch (JSONException e) {
            //obj = new JSONArray(s).get(0);
            //System.out.println(e);
        }

        try {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject curr = (JSONObject) arr.get(i);
                UriAndCookie uriAndCookie = jsonToCookie(curr);
                add(uriAndCookie.getUri(), uriAndCookie.getCookie());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // add a shutdown hook to write out the in memory cookies
        Runtime.getRuntime().addShutdownHook(new Thread(this));
    }

    public void run() {
        List<HttpCookie> cookies = getCookies();
        List<URI> urIs = getURIs();
        JSONArray arr = new JSONArray();

        for (HttpCookie cookie : cookies) {
            URI uri = null;
            for (URI is : urIs) {
                if (is.toString().contains(cookie.getDomain().substring(1))) {
                    uri = is;
                }
            }
            JSONObject curr = cookieToJson(new UriAndCookie(uri, cookie));
            arr.put(curr);
        }
        Filer.setFile(fileName, arr.toString());
    }

    public void add(URI uri, HttpCookie cookie) {
        store.add(uri, cookie);
    }

    public List<HttpCookie> get(URI uri) {
        return store.get(uri);
    }

    public List<HttpCookie> getCookies() {
        return store.getCookies();
    }

    public List<URI> getURIs() {
        return store.getURIs();
    }

    public boolean remove(URI uri, HttpCookie cookie) {
        return store.remove(uri, cookie);
    }

    public boolean removeAll()  {
        return store.removeAll();
    }


    private JSONObject cookieToJson(UriAndCookie object) {
        HttpCookie cookie = object.getCookie();
        JSONObject obj = new JSONObject();
        try {
            obj.put("comment", cookie.getComment());
            obj.put("value", cookie.getValue());
            obj.put("comment_url", cookie.getCommentURL());
            obj.put("discard", cookie.getDiscard());
            obj.put("domain", cookie.getDomain());
            obj.put("max_age", cookie.getMaxAge());
            obj.put("name", cookie.getName());
            obj.put("path", cookie.getPath());
            obj.put("port_list", cookie.getPortlist());
            obj.put("secure", cookie.getSecure());
            obj.put("version", cookie.getVersion());
            obj.put("uri", object.getUri().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return obj;
    }

    private UriAndCookie jsonToCookie(JSONObject jsonObject) throws JSONException {
        HttpCookie cookie = new HttpCookie((String) jsonObject.get("name"), (String) jsonObject.get("value"));

        try {
            String comment = (String) jsonObject.get("comment");
            if (comment != null)
                cookie.setComment(comment);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String commentUrl = (String) jsonObject.get("comment_url");
            if (commentUrl != null)
                cookie.setCommentURL(commentUrl);
        } catch (JSONException e) {
             e.printStackTrace();
        }

        try {
            Boolean discard = jsonObject.getBoolean("discard");
            if (discard != null)
                cookie.setDiscard(discard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            String domain = jsonObject.getString("domain");
            if (domain != null)
                cookie.setDomain(domain);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Long maxAge = jsonObject.getLong("max_age");
            if (maxAge != null)
                cookie.setMaxAge(maxAge);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
        String path = jsonObject.getString("path");
        if (path != null)
            cookie.setPath(path);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            String portList = jsonObject.getString("port_list");
            if (portList != null)
                cookie.setPortlist(portList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Boolean secure = jsonObject.getBoolean("secure");
            if (secure != null)
                cookie.setSecure(secure);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            Long version = jsonObject.getLong("version");
            if (version != null)
                cookie.setVersion(version.intValue());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            URI uri = new URI(jsonObject.getString("uri"));
            return new UriAndCookie(uri, cookie);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static class UriAndCookie {
        private final URI uri;
        private final HttpCookie cookie;

        public UriAndCookie(URI uri, HttpCookie cookie) {
            this.uri = uri;
            this.cookie = cookie;
        }

        public URI getUri() {
            return uri;
        }

        public HttpCookie getCookie() {
            return cookie;
        }
    }
}