const requestTypes = {
    "POST": 1,
    "PUT": 2,
    "DELETE": 3,
}

/**
 * This acts as an interface as too what gets put on the undo redo stack
 * @param {Object} undoReqData ReqData object for redo request
 * @param {Object} redoReqData ReqData object for undo request
 */
class UndoRedoReq {
    constructor(undoReqData, redoReqData) {
        this.undoReq = undoReq;
        this.redoReq = redoReq;
    }
}

/**
 * Acts as an interface for what should be put insto the UndoRedoReq
 *
 * @param {Number} type type of request represented by requestTypes
 * @param {String} URL address that request is ment to be delivered
 * @param {Object} body body of request if applicable
 */
class ReqData {
    constructor(type, URL, body={}) {
        this.type = type;
        this.URL = URL;
        this.body = body;
    }
}

/**
 * Stack for request data
 */
class ReqStack {
    constructor() {
        this.#stack = [];
    }

    /**
     * pushes data onto stack
     * 
     * @param {Object} undoRedoReq UndoRedoReq object to be pushed onto stack
     */
    push(undoRedoReq) {
        this.#stack.push(undoRedoReq);
    }

    /**
     * pops an object of the top of the stack
     */
     pop() {
         //Maybe we will do stuff here?
         return this.#stack.pop();
     }
}