const UndoRedo = require('../../public/javascripts/util/undoRedo');

describe('I can mipulate a ReqStack ', () => {
    test('Push to a ReqStack', () => {
        const reqStack = new UndoRedo.ReqStack();
        reqStack.push(new UndoRedo.UndoRedoReq({},{}));
        expect(reqStack.stack.length).toEqual(1);
    });

    test('Pop from a ReqStack', () => {
        const reqStack = new UndoRedo.ReqStack();
        const undoRedoReq = new UndoRedo.UndoRedoReq({},{});
        reqStack.stack = [undoRedoReq];
        expect(reqStack.pop()).toEqual(undoRedoReq);
    });
});

describe('I can send requests and create inverse actions', () => {
    test('I can use a TOGGLE request', async () => {
        const undoRedo = new UndoRedo.UndoRedo();
        const URL = 'https://postman-echo.com/put'; //This doesnt matter as server isnt running
        const type = UndoRedo.requestTypes['TOGGLE'];
        const handler = (status, json) => {
            expect(status).toEqual(200);
            expect(json.data.name).toEqual('Tester');
        }
        const reqData = new UndoRedo.ReqData(type, URL, handler, {name: "Tester"});
        await undoRedo.sendAndAppend(reqData);
        expect(undoRedo.undoStack.stack.length).toEqual(1);
        expect(undoRedo.undoStack.stack[0]).toEqual(new UndoRedo.UndoRedoReq(reqData, reqData));
    });

    test('I can use a CREATE request', async () => {
        const undoRedo = new UndoRedo.UndoRedo();
        const URL = 'https://postman-echo.com/post'; //This doesnt matter as server isnt running
        const type = UndoRedo.requestTypes['CREATE'];
        const handler = (status, json) => {
            expect(status).toEqual(200);
            expect(json.data).toEqual('1');
        }
        const reqData = new UndoRedo.ReqData(type, URL, handler, 1);
        await undoRedo.sendAndAppend(reqData);
        expect(undoRedo.undoStack.stack.length).toEqual(1);
        expect(undoRedo.undoStack.stack[0].redoReq).toEqual(reqData);
        expect(undoRedo.undoStack.stack[0].undoReq.type).toEqual(UndoRedo.requestTypes['TOGGLE']);
    });

    test('I can use a UPDATE request', async () => {
        const undoRedo = new UndoRedo.UndoRedo();
        const URL = 'https://postman-echo.com/put'; //This doesnt matter as server isnt running
        const type = UndoRedo.requestTypes['UPDATE'];
        const handler = (status, json) => {
            expect(status).toEqual(200);
            expect(json.data.name).toEqual('Tester');
        }
        const reqData = new UndoRedo.ReqData(type, URL, handler, {name: "Tester"});
        await undoRedo.sendAndAppend(reqData);
        expect(undoRedo.undoStack.stack.length).toEqual(1);
        expect(undoRedo.undoStack.stack[0].redoReq).toEqual(reqData);
        expect(undoRedo.undoStack.stack[0].undoReq.body).toEqual({ args: {},
            data: { name: 'Tester' },
            files: {},
            form: {},
            headers: {
                'x-forwarded-proto': 'https',
                host: 'postman-echo.com',
                'content-length': '17',
                accept: '*/*',
                'accept-encoding': 'gzip,deflate',
                'content-type': 'application/json',
                'user-agent': 'node-fetch/1.0 (+https://github.com/bitinn/node-fetch)',
                'x-forwarded-port': '443'
            },
            json: { name: 'Tester' },
            url: 'https://postman-echo.com/put' 
        });
    });

});
