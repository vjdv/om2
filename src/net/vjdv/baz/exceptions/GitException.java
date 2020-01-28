package net.vjdv.baz.exceptions;

/**
 *
 * @author B187926
 */
public class GitException extends RuntimeException {

    public GitException(String msg) {
        super(msg);
    }

    public GitException(Throwable err) {
        super(err);
    }

}
