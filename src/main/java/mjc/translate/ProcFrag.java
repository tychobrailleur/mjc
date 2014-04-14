package mjc.translate;

import mjc.frame.Frame;
import mjc.tree.Stm;

public class ProcFrag {
    private final Stm body;
    private final Frame frame;

    ProcFrag(Stm body, Frame frame) {
        this.body = body;
        this.frame = frame;
    }

    Stm getBody() {
        return body;
    }

    Frame getFrame() {
        return frame;
    }
}