const requestTypes = {
    "POST": 1,
    "PUT": 2,
    "DELETE": 3,
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
 * Stack for request data
 */
class ReqStack {
    constructor() {
        this.stack = [];
    }

    /**
     * pushes data onto stack
     * 
     * @param {Object} undoRedoReq UndoRedoReq object to be pushed onto stack
     */
    push(undoRedoReq) {
        this.stack.push(undoRedoReq);
    }

    /**
     * pops an object of the top of the stack
     */
     pop() {
         //Maybe we will do stuff here?
         return this.stack.pop();
     }
}

/**
 * This is the main structur for managing undoredo by hacing two stacks
 */
class UndoRedo {
    constructor() {
        this.undo = new ReqStack();
        this.redo = new ReqStack();
    }

    /**
     * An intemadatory step for sending requests to the api. 
     * Will store this request as a redoreq and create an inverse undoReq
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     */
    sendAndAppend(reqData) {
        let response;
        switch(reqData.type) {
            case requestTypes["DELETE"]:
                //Delete should toggle so its inverse is itself
                response = _delete(reqData.URL);
                const undoReq = reqData;
                break;
            case requestTypes["POST"]:

                break;
            case requestTypes["PUT"]:
                break;
            default:
                throw "Request type not found";
        }
        const undoRedoReq = new UndoRedoReq(undoReq, reqData);
        this.undo.push(undoRedoReq);
        return response;
    }

    undo() {
        const undoRedoReq = this.undo.pop();
        return _delete(undoRedoReq.URL)
    }
}
