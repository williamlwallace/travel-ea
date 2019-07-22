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
        this.stack = [];    @SoftDelete
    public Boolean deleted;
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
    sendAndAppend(reqData, inverseHandler) {
        this.resAndInverse(reqData, inverseHandler).then(({sponse, json, inverseData}) => {
            reqData.handler(sponse, json);
            if (sponse.status !== 201 && sponse.status !== 200) return;
            const undoRedoReq = new UndoRedoReq(inverseData, reqData);
            this.undoStack.push(undoRedoReq);
        });
    }

    /**
     * Handles the next undo and will add it to redo stack. Throws error if no undos.
     */
    undo() {
        const undoRedoReq = this.undoStack.pop();
        if (!undoRedoReq) throw "No undos";
        
        this.resAndInverse(undoRedoReq.undoReq).then(({sponse, json, inverseData}) => {
            undoRedoReq.undoReq.handler(sponse, json);
            if (sponse.status !== 201 && sponse.status !== 200) return;
            this.redoStack.push(new UndoRedoReq(undoRedoReq.undoReq, inverseData));
        });
        
    }

    /**
     * Handles the next redo and will add it to the undo stack. Throws error if no redos.
     */sendAndAppend
    redo() {
        const undoRedoReq = this.redoStack.pop();
        if (!undoRedoReq) throw "No redos";
        
        this.resAndInverse(undoRedoReq.redoReq).then(({sponse, json, inverseData}) => {
            undoRedoReq.redoReq.handler(sponse, json);
            if (sponse.status !== 201 && sponse.status !== 200) return;
            this.undoStack.push(new UndoRedoReq(inverseData, undoRedoReq.redoReq));
        });
    }

    /**
     * Generates a response by calling the appropriate fetch method and creates and 
     * inverse data set to be used for undo/redo.
     *
     * @param {Object} reqData ReqData instance that contains data for request to send
     */
    resAndInverse(reqData, inverseHandler) {
        switch(reqData.type) {
            case requestTypes["TOGGLE"]:
                //Delete should toggle so its inverse is itself
                return put(reqData.URL, reqData.body).then(sponse => {
                    return sponse.json().then(json => {
                        return {sponse, json, inverseData: reqData};
                    });
                });
                
            case requestTypes["POST"]:
                //Send a post request with req data and generate a delete toggle
                return post(reqData.URL, reqData.body).then(sponse => {
                    return sponse.json().then(json => {
                        const inverseData = new ReqData(requestTypes['TOGGLE'], `${reqData.URL}/${json}/delete`, inverseHandler);
                        return {sponse, json, inverseData};    
                    });
                });
            case requestTypes["PUT"]:
                break;
            default:
                throw "Request type not found";
        }
    }
}


//Initialise
const undoRedo = new UndoRedo();