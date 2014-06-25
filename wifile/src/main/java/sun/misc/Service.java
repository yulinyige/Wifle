/*
00002  * @(#)Service.java     1.13 10/03/23
00003  *
00004  * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
00005  * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
00006  */

package sun.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * 第三方类,可以不管
 */
public final class Service {
    private static final String prefix = "META-INF/services/";

    private Service() {
    }

    private static void fail(Class service, String msg, Throwable cause)

            throws ServiceConfigurationError

    {
        ServiceConfigurationError sce
                = new ServiceConfigurationError(service.getName() + ": " + msg);
        sce.initCause(cause);
        throw sce;

    }


    private static void fail(Class service, String msg)

            throws ServiceConfigurationError


    {
        throw new ServiceConfigurationError(service.getName() + ": " + msg);

    }

    private static void fail(Class service, URL u, int line, String msg)

            throws ServiceConfigurationError

    {
        fail(service, u + ":" + line + ": " + msg);
    }

    private static int parseLine(Class service, URL u, BufferedReader r, int lc,
                                 List names, Set returned)

            throws IOException, ServiceConfigurationError {
        String ln = r.readLine();
        if (ln == null) {
            return -1;

        }
        int ci = ln.indexOf('#');
        if (ci >= 0) ln = ln.substring(0, ci);
        ln = ln.trim();
        int n = ln.length();
        if (n != 0) {
            if ((ln.indexOf(' ') >= 0) || (ln.indexOf('\t') >= 0))
                fail(service, u, lc, "Illegal configuration-file syntax");
            int cp = ln.codePointAt(0);
            if (!Character.isJavaIdentifierStart(cp))
                fail(service, u, lc, "Illegal provider-class name: " + ln);
            for (int i = Character.charCount(cp); i < n; i += Character.charCount(cp)) {
                cp = ln.codePointAt(i);
                if (!Character.isJavaIdentifierPart(cp) && (cp != '.'))
                    fail(service, u, lc, "Illegal provider-class name: " + ln);
            }
            if (!returned.contains(ln)) {
                names.add(ln);
                returned.add(ln);
            }
        }
        return lc + 1;
    }

    private static Iterator parse(Class service, URL u, Set returned)

            throws ServiceConfigurationError

    {
        InputStream in = null;
        BufferedReader r = null;
        ArrayList names = new ArrayList();
        try {
            in = u.openStream();
            r = new BufferedReader(new InputStreamReader(in, "utf-8"));
            int lc = 1;
            while ((lc = parseLine(service, u, r, lc, names, returned)) >= 0) ;

        } catch (IOException x) {
            fail(service, ": " + x);

        } finally {
            try {
                if (r != null) r.close();
                if (in != null) in.close();

            } catch (IOException y) {
                fail(service, ": " + y);

            }

        }
        return names.iterator();

    }


    private static class LazyIterator implements Iterator {
        Class service;
        ClassLoader loader;
        Enumeration configs = null;
        Iterator pending = null;
        Set returned = new TreeSet();
        String nextName = null;

        private LazyIterator(Class service, ClassLoader loader) {
            this.service = service;
            this.loader = loader;
        }

        public boolean hasNext() throws ServiceConfigurationError {
            if (nextName != null) {
                return true;

            }
            if (configs == null) {
                try {
                    String fullName = prefix + service.getName();
                    if (loader == null)
                        configs = ClassLoader.getSystemResources(fullName);
                    else
                        configs = loader.getResources(fullName);

                } catch (IOException x) {
                    fail(service, ": " + x);

                }

            }
            while ((pending == null) || !pending.hasNext()) {
                if (!configs.hasMoreElements()) {
                    return false;

                }
                pending = parse(service, (URL) configs.nextElement(), returned);

            }
            nextName = (String) pending.next();
            return true;

        }

        public Object next() throws ServiceConfigurationError {
            if (!hasNext()) {
                throw new NoSuchElementException();

            }
            String cn = nextName;
            nextName = null;
            try {
                return Class.forName(cn, true, loader).newInstance();

            } catch (ClassNotFoundException x) {
                fail(service,
                        "Provider " + cn + " not found");

            } catch (Exception x) {
                fail(service,
                        "Provider " + cn + " could not be instantiated: " + x,
                        x);

            }
             return null;        /* This cannot happen */

        }

        public void remove() {
             throw new UnsupportedOperationException();
        }

    }

    public static Iterator providers(Class service, ClassLoader loader)

    throws ServiceConfigurationError

    {
         return new LazyIterator(service, loader);
    }

    public static Iterator providers(Class service)

    throws ServiceConfigurationError
    {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         return Service.providers(service, cl);

    }

    public static Iterator installedProviders(Class service)

    throws ServiceConfigurationError

    {
         ClassLoader cl = ClassLoader.getSystemClassLoader();
         ClassLoader prev = null;
         while (cl != null) {
         prev = cl;
         cl = cl.getParent();

    }
         return Service.providers(service, prev);

    }
}