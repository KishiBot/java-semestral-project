package app;

import app.Logger.Severity;

public class Log {
    public Severity severity = null;
    public String classTag = null;
    public String message = null;

    public Log(Severity _severity, String _classTag, String _message) {
        severity = _severity;
        classTag = _classTag;
        message = _message;
    }
}
