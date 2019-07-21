/**
 * Types of request recognised by undo redo stack.
 */
const requestTypes = {
    "POST": 1,
    "PUT": 2,
    "TOGGLE": 3,
}

/**
 * Acts as an interface for what should be put insto the UndoRedoReq.
 *
 * @param {Number} type type of request represented by requestTypes
 * @param {String} URL address that request is ment to be delivered
 * @param {Object} body body of request if applicable
 */
class ReqData {
    constructor(type, URL, handler, body={}) {
        this.type = type;
        this.URL = URL;
        this.handler = handler;
        this.body = body;
    }
}

/**
 * This acts as an interface as too what gets put on the undo redo stack.
 *
 * @param {Object} undoReqData ReqData object for redo request
 * @param {Object} redoReqData ReqData object for undo request
 */
class UndoRedoReq {
    constructor(undoReqData, redoReqData) {
        this.undoReq = undoReqData;
        this.redoReq = redoReqData;
    }
}

/**
 * Stack for request data.
 */
class ReqStack {
    constructor() {
        this.stack = [];
    }

    /**
     * pushes data onto stack.
     * 
     * @param {Object} undoRedoReq UndoRedoReq object to be pushed onto stack
     */
    push(undoRedoReq) {
        this.stack.push(undoRedoReq);
    }

    /**
     * pops an object of the top of the stack.
     */
     pop() {
         //Maybe we will do stuff here?
         return this.stack.pop();
     }
}

/**
 * This is the main structur for managing undoredo by hacing two stacks.
 */
class UndoRedo {
    constructor() {
        this.undoStack = new ReqStack();
        this.redoStack = new ReqStack();
    }

    /**
     * An intemadatory step for sending requests to the api. 
     * Will store this request as a redoreq and create an inverse undoReq.
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     */
    sendAndAppend(reqData) {
        this.resAndInverse(reqData);
        reqData.handler(this.res);
        this.res.then(sponse => {
            if (sponse.status !== 201 && sponse.status !== 200) return;
            const undoRedoReq = new UndoRedoReq(this.inverseData, reqData);
            this.undoStack.push(undoRedoReq);
        });
    }

    /**
     * Handles the next undo and will add it to redo stack. Throws error if no undos.
     */
    undo() {
        const undoRedoReq = this.undoStack.pop();
        console.log(undoRedoReq);
        if (!undoRedoReq) throw "No undos";
        
        this.resAndInverse(undoRedoReq.undoReq);
        undoRedoReq.undoReq.handler(this.res);
        this.res.then(sponse => {
            if (sponse.status !== 201 && sponse.status !== 200) return;
            this.redoStack.push(new UndoRedoReq(undoRedoReq.undoReq, this.inverseData));
        });
    }

    /**
     * Handles the next redo and will add it to the undo stack. Throws error if no redos.
     */
    redo() {
        const undoRedoReq = this.redoStack.pop();
        if (!undoRedoReq) throw "No redos";
        
        this.resAndInverse(undoRedoReq.redoReq);
        undoRedoReq.redoReq.handler(this.res);
        this.res.then(sponse => {
            if (sponse.status !== 201 && sponse.status !== 200) return;
            this.undoStack.push(new UndoRedoReq(this.inverseData, undoRedoReq.redoReq));
        })
        
    }

    /**
     * Generates a response by calling the appropriate fetch method and creates and 
     * inverse data set to be used for undo/redo.
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     */
    resAndInverse(reqData) {
        switch(reqData.type) {
            case requestTypes["TOGGLE"]:
                //Delete should toggle so its inverse is itself
                this.res = put(reqData.URL, reqData.body);
                this.inverseData = Object.assign({}, reqData);
                console.log(this.inverseData);
                break;
            case requestTypes["POST"]:

                break;
            case requestTypes["PUT"]:
                break;
            default:
                throw "Request type not found";
        }
    }
}


//Initialise
const undoRedo = new UndoRedo();
console.log(undoRedo.undo());