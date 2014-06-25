package sun.misc;

/**
 * 第三方类,可以不管
 */
public class ServiceConfigurationError extends Error {
    public ServiceConfigurationError(String msg) {
        super(msg);
    }

    public ServiceConfigurationError(Throwable x) {
        super(x);
    }

}