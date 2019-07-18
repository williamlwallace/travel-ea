const requestTypes = {
    "POST": 1,
    "PUT": 2,
    "DELETE": 3,
}

/**
 * This acts as an interface as too what gets put on the undo redo stack
 */
class UndoRedoReq {
    constructor (undoReq, redoReq) {
        this.undoReq = undoReq;
        this.redoReq = redoReq;
    }
}

/**
 * Acts as an interface for what should be put insto the UndoRedoReq
 */
class ReqData {
    constructor (type, URL, body) {
        this.type = type;
        this.URL = URL;
        this.body = body;
    }
}