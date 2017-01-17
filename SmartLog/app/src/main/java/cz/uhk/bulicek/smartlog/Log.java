package cz.uhk.bulicek.smartlog;

/**
 * Created by bulicek on 16. 1. 2017.
 */

public class Log {
    String _time;
    int _type;

    public Log(String _time, int _type) {
        this._time = _time;
        this._type = _type;
    }

    public int get_type() {
        return _type;
    }

    public void set_type(int _type) {
        this._type = _type;
    }

    public String get_time() {
        return _time;
    }

    public void set_time(String _time) {
        this._time = _time;
    }
}
